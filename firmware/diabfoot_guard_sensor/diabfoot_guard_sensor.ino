#include <Wire.h>
#include <Adafruit_ADS1X15.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <math.h>

// BLE (v2 streaming contract — app docs/plans/TD3-TD4-PLAN.md Part A4)
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include "esp_bt.h"
#include "esp_pm.h"

// =====================================================
// PIN CONFIGURATION
// =====================================================

// 5 FSR langsung ke ADC internal ESP32-C3
// Pemetaan zona (peta telapak kaki, "Gambar 8.3" — tampak atas telapak):
//   S1/p1 = HALLUX  (ibu jari)
//   S2/p2 = MTH1    (kepala metatarsal pertama)
//   S3/p3 = MTH2    (kepala metatarsal kedua)
//   S4/p4 = MTH4    (kepala metatarsal keempat)
//   S5/p5 = HEEL    (tumit / kalkaneus)
// NTC tunggal berada di regio MIDFOOT (app: site_id "MIDFOOT").
const int PIN_FSR[5] = {0, 1, 2, 3, 4};

// I2C untuk ADS1115 dan MPU6050
const int PIN_SDA = 8;
const int PIN_SCL = 9;

// Kalau GPIO9 mengganggu boot/upload, bisa diganti:
// const int PIN_SDA = 6;
// const int PIN_SCL = 7;

// =====================================================
// BATTERY (TD-13) — opsional, aktif setelah hardware terpasang
// =====================================================
// Ukur VBAT SEBELUM regulator (tegangan rail 3.3V tidak pernah turun).
// Divider: VBAT -- 100k -- [GPIO5] -- 100k -- GND  (ADC lihat 1.5-2.1V)
// GPIO5 = ADC2, aman karena Wi-Fi tidak pernah dipakai (BLE saja).
// 300 mAh @ ~40-60 mA rata-rata (BLE 5 Hz + sensor) = ~4-6 jam streaming.
// Set ke nomor pin GPIO5 setelah divider disolder; -1 = belum terpasang.
#define BATTERY_PIN (-1)
#define BATTERY_READ_PERIOD_MS 30000UL

// =====================================================
// INSOLE SIDE CONFIG — set 0 pada insole KANAN
// =====================================================
#define INSOLE_IS_LEFT 0

#if INSOLE_IS_LEFT
  #define SERVICE_UUID        "4fa2c732-ca9a-4c20-9492-c167df3c942b"
  #define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
  #define INSOLE_NAME         "Smart_Insole_Left"
#else
  #define SERVICE_UUID        "4fa2c732-ca9a-4c20-9492-c167df3c942c"
  #define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26c9"
  #define INSOLE_NAME         "Smart_Insole_Right"
#endif

// Stability tracking across reboots (RTC memory survives soft reset & watchdog)
RTC_DATA_ATTR static uint32_t rtcBootCount = 0;

// =====================================================
// OBJECT SENSOR
// =====================================================

Adafruit_ADS1115 ads;

bool ads_ok = false;
bool mpu_ok = false;
uint8_t mpu_address = 0x68;
uint8_t mpu_whoami = 0;

// Universal MPU Driver (supports WHO_AM_I 0x68, 0x70, 0x71, 0x74)
bool initUniversalMPU() {
  const uint8_t candidateAddresses[] = {0x68, 0x69};
  for (uint8_t i = 0; i < 2; i++) {
    uint8_t addr = candidateAddresses[i];
    Wire.beginTransmission(addr);
    if (Wire.endTransmission() != 0) continue;

    // Wake from sleep (PWR_MGMT_1 = 0x00)
    Wire.beginTransmission(addr);
    Wire.write(0x6B);
    Wire.write(0x00);
    if (Wire.endTransmission() != 0) continue;
    delay(30);

    // Read WHO_AM_I
    Wire.beginTransmission(addr);
    Wire.write(0x75);
    Wire.endTransmission(false);
    Wire.requestFrom((uint8_t)addr, (uint8_t)1);
    mpu_whoami = Wire.available() ? Wire.read() : 0x00;

    Serial.print("[MPU] Device responding at 0x");
    Serial.print(addr, HEX);
    Serial.print(" with WHO_AM_I = 0x");
    Serial.println(mpu_whoami, HEX);

    // Config Accel +/- 8g
    Wire.beginTransmission(addr);
    Wire.write(0x1C);
    Wire.write(0x10);
    Wire.endTransmission();

    // Config Gyro +/- 500 deg/s
    Wire.beginTransmission(addr);
    Wire.write(0x1B);
    Wire.write(0x08);
    Wire.endTransmission();

    // DLPF ~21Hz
    Wire.beginTransmission(addr);
    Wire.write(0x1A);
    Wire.write(0x03);
    Wire.endTransmission();

    mpu_address = addr;
    return true;
  }
  return false;
}

bool readUniversalMPU(float &ax_out, float &ay_out, float &az_out,
                      float &gx_out, float &gy_out, float &gz_out,
                      float &temp_out) {
  if (!mpu_ok) return false;
  Wire.beginTransmission(mpu_address);
  Wire.write(0x3B);
  if (Wire.endTransmission(false) != 0) return false;
  if (Wire.requestFrom((uint8_t)mpu_address, (uint8_t)14) != 14) return false;

  int16_t raw_ax = (Wire.read() << 8) | Wire.read();
  int16_t raw_ay = (Wire.read() << 8) | Wire.read();
  int16_t raw_az = (Wire.read() << 8) | Wire.read();
  int16_t raw_t  = (Wire.read() << 8) | Wire.read();
  int16_t raw_gx = (Wire.read() << 8) | Wire.read();
  int16_t raw_gy = (Wire.read() << 8) | Wire.read();
  int16_t raw_gz = (Wire.read() << 8) | Wire.read();

  ax_out = (raw_ax / 4096.0f) * 9.80665f;
  ay_out = (raw_ay / 4096.0f) * 9.80665f;
  az_out = (raw_az / 4096.0f) * 9.80665f;
  gx_out = (raw_gx / 65.5f) * (3.14159265f / 180.0f);
  gy_out = (raw_gy / 65.5f) * (3.14159265f / 180.0f);
  gz_out = (raw_gz / 65.5f) * (3.14159265f / 180.0f);
  temp_out = (raw_t / 340.0f) + 36.53f;
  return true;
}

// =====================================================
// ADC CONFIGURATION
// =====================================================

const float VCC = 3.3;
const int ADC_MAX = 4095;

// =====================================================
// FSR CONFIGURATION
// =====================================================

// Rangkaian FSR:
// 3.3V -- FSR -- node pembagi -- R 10k -- GND
// node pembagi -- R LPF 10k -- ADC ESP32-C3
// ADC -- C LPF 1uF -- GND

const float R_FIXED_FSR = 10000.0;     // 10k ohm
const float FSR_AREA_M2 = 7.85e-5;     // asumsi area aktif — BELUM KALIBRASI (TD-11)
// TD-11 rencana kalibrasi dua lapis (docs/plans/TD3-TD4-PLAN.md Part D):
//   Lapis 1 (firmware, NVS): LUT 6 titik per FSR dari beban bench
//   (0/2/5/10/20/30 kg) menggantikan estimateForceFromFSR di bawah.
//   Lapis 2 (app): normalisasi berat badan + ukuran kaki per pengguna.
//   Jangan klaim threshold tekanan klinis sebelum Lapis 1 selesai.

// =====================================================
// NTC CONFIGURATION VIA ADS1115
// =====================================================

// Rangkaian NTC:
// 3.3V -- NTC 10k -- A0 ADS1115 -- R fixed 10k -- GND

const float R_FIXED_NTC = 10000.0;       // 10k pada 25 C
const float NTC_NOMINAL = 10000.0;       // 10k pada 25 C
const float TEMP_NOMINAL = 25.0;         // 25 C
const float BETA_COEFFICIENT = 3950.0;   // sesuaikan dengan datasheet NTC

// =====================================================
// STREAM SCHEDULING (TD-12: cadence BLE terpisah dari serial debug)
// =====================================================
// Dua jenis baris dikirim lewat SATU characteristic; app membedakan
// berdasarkan jumlah field:
//   Stream (5 Hz)        : ax,ay,az,gx,gy,gz,p1,p2,p3,p4,p5  (11 field angka)
//     - 5 Hz = kebutuhan minimal FSM deteksi langkah di app (gerbang
//       konfirmasi 100 ms, histeresis langkah; 3-4 sampel per langkah).
//       Path 50 Hz (fitur per-step untuk app dokter) = iterasi berikutnya:
//       firmware menghitung fitur per-langkah on-chip, bukan kirim mentah.
//     - satuan: accel m/s^2, gyro rad/s (Adafruit MPU6050) — app yang
//       mengonversi ke g / deg/s (SensorUnitDetector).
//     - MPU tidak ada -> enam field IMU = 0 (app: imuValid=false,
//       langkah TIDAK dihitung — TD-10a guard).
//   Temperatur (1/menit) : T,<tempC>[,<batPct>]  (2 atau 3 field)
//     - batPct opsional, hanya jika BATTERY_PIN >= 0 (TD-13).
//     - Sentinel NTC -999 TIDAK PERNAH dikirim (NTC mati = diam saja).
// Serial debug tetap format panjang 31 field — DEBUG SAJA, tidak ke BLE.

// =====================================================
// CADENCE & BATCHING CONFIGURATION
// =====================================================
// 20 Hz acquisition (50 ms interval) decouples sensor temporal resolution from BLE transmission.
// Batching 5 frames into one GATT notification flushes every 250 ms (4 Hz over-the-air burst).
// This enables ESP32-C3 2.4 GHz RF modem sleep between connection events, extending 300 mAh battery to > 10 hours.
volatile unsigned long sample_period_ms = 50;     // 20 Hz acquisition (50 ms)
volatile int batch_size = 5;                      // 5 frames per BLE burst (~250 ms)
const unsigned long TEMP_PERIOD_MS    = 2000UL;   // 2 detik
const unsigned long SERIAL_PERIOD_MS  = 500;      // debug saja

static String batchBuffer = "";
static int currentBatchCount = 0;
static unsigned long lastBatchFlushMs = 0;

uint8_t ntc_channel = 0;

// Fast acquisition for 20-50 Hz cadence:
// 1 sample per FSR (~15 us each) leaves full idle budget for FreeRTOS tickless sleep
const int FSR_STREAM_SAMPLES = 1;
const int FSR_SERIAL_SAMPLES = 10;   // debug serial
const int NTC_SAMPLES = 5;

unsigned long lastStreamMs = 0;
unsigned long lastTempMs   = 0;
unsigned long lastBatMs    = 0;
unsigned long lastSerialMs = 0;

// Nilai terakhir untuk serial debug
float dbg_fsr_v[5], dbg_fsr_r[5], dbg_fsr_force[5], dbg_fsr_kpa[5];
float dbg_ax = 0, dbg_ay = 0, dbg_az = 0, dbg_gx = 0, dbg_gy = 0, dbg_gz = 0;
float dbg_mpu_temp = 0;
float dbg_ntc_v = -1.0, dbg_ntc_r = -1.0, dbg_ntc_temp = -999.0;
int   dbg_bat_pct = -1;   // -1 = belum tersedia

// =====================================================
// BLE (v2 contract)
// =====================================================

BLEServer* bleServer = nullptr;
BLECharacteristic* streamCharacteristic = nullptr;
volatile bool bleConnected = false;

class InsoleServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer* s) override {
    bleConnected = true;
    Serial.println("[BLE] Client connected!");
  }
  void onDisconnect(BLEServer* s) override {
    bleConnected = false;
    Serial.println("[BLE] Client disconnected!");
    s->getAdvertising()->start();
  }
};

bool isBleConnected() {
  return bleConnected || (bleServer != nullptr && bleServer->getConnectedCount() > 0);
}

class ConfigCallback : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic* pChar) override {
    String rx = pChar->getValue();
    if (rx.length() > 0) {
      rx.trim();
      if (rx.startsWith("RATE:")) {
        int hz = rx.substring(5).toInt();
        if (hz >= 1 && hz <= 100) {
          if (hz == 20) {
            sample_period_ms = 50;
            batch_size = 5; // 20 Hz acquisition, 4 Hz batched burst
          } else if (hz == 50) {
            sample_period_ms = 20;
            batch_size = 1; // 50 Hz unbatched mode
          } else {
            sample_period_ms = 1000UL / (unsigned long)hz;
            batch_size = 1;
          }
          Serial.printf("[BLE] Rate updated to: %d Hz (period %lu ms, batch %d)\n", hz, sample_period_ms, batch_size);
        }
      } else if (rx.startsWith("BATCH:")) {
        int b = rx.substring(6).toInt();
        if (b >= 1 && b <= 10) {
          batch_size = b;
          Serial.printf("[BLE] Batch size updated to: %d\n", batch_size);
        }
      }
    }
  }
};

void setupBLE() {
  BLEDevice::init(INSOLE_NAME);
  BLEDevice::setMTU(517);

  bleServer = BLEDevice::createServer();
  bleServer->setCallbacks(new InsoleServerCallbacks());

  BLEService* service = bleServer->createService(SERVICE_UUID);

  streamCharacteristic = service->createCharacteristic(
      CHARACTERISTIC_UUID,
      BLECharacteristic::PROPERTY_READ |
      BLECharacteristic::PROPERTY_WRITE |
      BLECharacteristic::PROPERTY_WRITE_NR |
      BLECharacteristic::PROPERTY_NOTIFY |
      BLECharacteristic::PROPERTY_INDICATE
  );

  streamCharacteristic->setCallbacks(new ConfigCallback());

  BLE2902* p2902 = new BLE2902();
  p2902->setAccessPermissions(ESP_GATT_PERM_READ | ESP_GATT_PERM_WRITE);
  streamCharacteristic->addDescriptor(p2902);

  service->start();

  // App memindai dengan ScanFilter pada SERVICE_UUID — WAJIB di-advertise.
  BLEAdvertising* advertising = BLEDevice::getAdvertising();
  advertising->addServiceUUID(SERVICE_UUID);
  advertising->setScanResponse(true);
  advertising->setMinPreferred(0x06); // Fast connection for iOS/macOS (7.5ms)
  advertising->setMaxPreferred(0x10); // 20ms connection interval for 50Hz throughput
  BLEDevice::startAdvertising();
}

void notifyLine(const char* line) {
  if (streamCharacteristic == nullptr) return;
  streamCharacteristic->setValue((uint8_t*)line, strlen(line));
  streamCharacteristic->notify();
}

// =====================================================
// READ ESP32 ADC FOR FSR
// =====================================================

float readESP32Voltage(int pin, int samples = 1) {
  long totalADC = 0;

  for (int i = 0; i < samples; i++) {
    totalADC += analogRead(pin);
    if (samples > 1) esp_rom_delay_us(50);
  }

  float avgADC = totalADC / (float)samples;
  float voltage = (avgADC / ADC_MAX) * VCC;

  return voltage;
}

// =====================================================
// READ ADS1115 FOR NTC
// =====================================================

float readADSVoltage(uint8_t channel, int samples = 10) {
  if (!ads_ok) {
    return -1.0;
  }

  float totalVoltage = 0;

  for (int i = 0; i < samples; i++) {
    int16_t raw = ads.readADC_SingleEnded(channel);
    float voltage = ads.computeVolts(raw);
    totalVoltage += voltage;
    delay(5);
  }

  return totalVoltage / samples;
}

// =====================================================
// FSR CONVERSION
// =====================================================

float voltageToFSRResistance(float vout) {
  // Konfigurasi:
  // 3.3V -- FSR -- ADC -- R_FIXED_FSR -- GND
  //
  // Vout = VCC * Rfixed / (Rfsr + Rfixed)
  // Rfsr = Rfixed * (VCC/Vout - 1)

  if (vout <= 0.001) {
    return 999999999.0;
  }

  return R_FIXED_FSR * ((VCC / vout) - 1.0);
}

float estimateForceFromFSR(float r_fsr) {
  // Estimasi kasar untuk uji awal (TD-11).
  // Ganti dengan interpolasi LUT NVS hasil kalibrasi bench sebelum
  // ada klaim klinis atas nilai kPa.

  if (r_fsr <= 0) {
    return 0;
  }

  float conductance_uS = 1000000.0 / r_fsr;
  float force_N = 0;

  if (conductance_uS <= 1000.0) {
    force_N = conductance_uS / 80.0;
  } else {
    force_N = (conductance_uS - 1000.0) / 30.0;
  }

  if (force_N < 0) {
    force_N = 0;
  }

  return force_N;
}

float forceToPressureKPa(float force_N) {
  float pressure_Pa = force_N / FSR_AREA_M2;
  return pressure_Pa / 1000.0;
}

// =====================================================
// NTC CONVERSION
// =====================================================

float voltageToNTCResistance(float vout) {
  // Konfigurasi:
  // 3.3V -- NTC -- A0 ADS1115 -- R_FIXED_NTC -- GND
  //
  // Vout = VCC * Rfixed / (Rntc + Rfixed)
  // Rntc = Rfixed * (VCC/Vout - 1)

  if (vout <= 0.001) {
    return 999999999.0;
  }

  return R_FIXED_NTC * ((VCC / vout) - 1.0);
}

float resistanceToTemperatureC(float r_ntc) {
  if (r_ntc <= 0) {
    return -999.0;
  }

  float steinhart = r_ntc / NTC_NOMINAL;
  steinhart = log(steinhart);
  steinhart /= BETA_COEFFICIENT;
  steinhart += 1.0 / (TEMP_NOMINAL + 273.15);
  steinhart = 1.0 / steinhart;
  steinhart -= 273.15;

  return steinhart;
}

// =====================================================
// BATTERY (TD-13) — aktif hanya jika BATTERY_PIN terpasang
// =====================================================

int readBatteryPercent() {
  if (BATTERY_PIN < 0) {
    return -1; // hardware belum terpasang — app menampilkan "unavailable"
  }
  long total = 0;
  for (int i = 0; i < 8; i++) {
    total += analogRead(BATTERY_PIN);
    delay(2);
  }
  float adc = total / 8.0f;
  // Divider 100k/100k => VBAT = 2 x Vadc
  float vbat = (adc / ADC_MAX) * VCC * 2.0f;
  // Kurva LiPo 1S (kasar): 4.2V=100%, 3.7V=~50%, 3.0V=0%
  float pct = (vbat - 3.0f) / (4.2f - 3.0f) * 100.0f;
  if (pct > 100) pct = 100;
  if (pct < 0) pct = 0;
  return (int)pct;
}

// =====================================================
// SAMPLE + NOTIFY (v2 contract)
// =====================================================

void sampleAndNotifyStream() {
  float fsrKpa[5];
  float ax = 0, ay = 0, az = 0, gx = 0, gy = 0, gz = 0;

  // 5 FSR — sampling ringan (4 sampel) agar muat di anggaran 200 ms
  for (int i = 0; i < 5; i++) {
    float v_fsr = readESP32Voltage(PIN_FSR[i], FSR_STREAM_SAMPLES);
    float r_fsr = voltageToFSRResistance(v_fsr);
    float force_N = estimateForceFromFSR(r_fsr);
    fsrKpa[i] = forceToPressureKPa(force_N);
  }

  // MPU — jika tidak ada, enam field IMU dikirim 0 (app: imuValid=false)
  if (mpu_ok) {
    readUniversalMPU(ax, ay, az, gx, gy, gz, dbg_mpu_temp);
  }

  // Cache untuk serial debug
  for (int i = 0; i < 5; i++) dbg_fsr_kpa[i] = fsrKpa[i];
  dbg_ax = ax; dbg_ay = ay; dbg_az = az;
  dbg_gx = gx; dbg_gy = gy; dbg_gz = gz;

  // Format single frame
  char line[96];
  snprintf(line, sizeof(line),
           "%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
           ax, ay, az, gx, gy, gz,
           fsrKpa[0], fsrKpa[1], fsrKpa[2], fsrKpa[3], fsrKpa[4]);

  if (batch_size <= 1) {
    line[strlen(line) - 1] = '\0'; // strip newline for unbatched single line
    notifyLine(line);
  } else {
    batchBuffer += line;
    currentBatchCount++;

    unsigned long now = millis();
    if (currentBatchCount >= batch_size || (now - lastBatchFlushMs >= (sample_period_ms * batch_size))) {
      lastBatchFlushMs = now;
      notifyLine(batchBuffer.c_str());
      batchBuffer = "";
      currentBatchCount = 0;
    }
  }
}

void sampleAndNotifyTemp() {
  float v_ntc = readADSVoltage(ntc_channel);   // ADS1115 NTC channel
  float r_ntc = 0;
  float temp_ntc = 0;

  if (v_ntc >= 0) {
    r_ntc = voltageToNTCResistance(v_ntc);
    temp_ntc = resistanceToTemperatureC(r_ntc);
  } else {
    r_ntc = -1;
    temp_ntc = -999;
  }

  // Fallback to internal MPU temperature if NTC thermistor is not wired
  float reportedTemp = (temp_ntc > -900.0) ? temp_ntc : dbg_mpu_temp;
  if (reportedTemp < -5.0 || reportedTemp > 60.0) reportedTemp = 25.0;

  dbg_ntc_v = v_ntc;
  dbg_ntc_r = r_ntc;
  dbg_ntc_temp = temp_ntc;

  // Heartbeat: T,<tempC>,<batPct>,<uptime_s>,<free_heap>,<boot_count>
  char line[64];
  unsigned long uptimeS = millis() / 1000UL;
  uint32_t freeHeap = ESP.getFreeHeap();
  snprintf(line, sizeof(line), "T,%.2f,%d,%lu,%lu,%lu", 
           reportedTemp, dbg_bat_pct, uptimeS, (unsigned long)freeHeap, (unsigned long)rtcBootCount);
  notifyLine(line);
}

void readBattery() {
  int pct = readBatteryPercent();
  dbg_bat_pct = pct; // -1 = tidak tersedia
}

// =====================================================
// SERIAL DEBUG — format panjang 31 field (DEBUG SAJA, tidak ke BLE)
// =====================================================

void printSerialDebug() {
  // Sampling halus khusus debug (20 sampel per FSR)
  for (int i = 0; i < 5; i++) {
    float v_fsr = readESP32Voltage(PIN_FSR[i], FSR_SERIAL_SAMPLES);
    float r_fsr = voltageToFSRResistance(v_fsr);
    float force_N = estimateForceFromFSR(r_fsr);
    float pressure_kPa = forceToPressureKPa(force_N);

    dbg_fsr_v[i] = v_fsr;
    dbg_fsr_r[i] = r_fsr;
    dbg_fsr_force[i] = force_N;
    dbg_fsr_kpa[i] = pressure_kPa;
  }

  if (mpu_ok && !bleConnected) {
    readUniversalMPU(dbg_ax, dbg_ay, dbg_az, dbg_gx, dbg_gy, dbg_gz, dbg_mpu_temp);
  }

  Serial.print(millis());
  Serial.print(",");

  for (int i = 0; i < 5; i++) {
    Serial.print(dbg_fsr_v[i], 3);
    Serial.print(",");
    Serial.print(dbg_fsr_r[i], 2);
    Serial.print(",");
    Serial.print(dbg_fsr_force[i], 3);
    Serial.print(",");
    Serial.print(dbg_fsr_kpa[i], 2);
    Serial.print(",");
  }

  Serial.print(dbg_ntc_v, 4);
  Serial.print(",");
  Serial.print(dbg_ntc_r, 2);
  Serial.print(",");
  Serial.print(dbg_ntc_temp, 2);
  Serial.print(",");

  if (mpu_ok) {
    Serial.print("OK");
    Serial.print(",");
    Serial.print(dbg_ax, 3);
    Serial.print(",");
    Serial.print(dbg_ay, 3);
    Serial.print(",");
    Serial.print(dbg_az, 3);
    Serial.print(",");
    Serial.print(dbg_gx, 3);
    Serial.print(",");
    Serial.print(dbg_gy, 3);
    Serial.print(",");
    Serial.print(dbg_gz, 3);
    Serial.print(",");
    Serial.println(dbg_mpu_temp, 2);
  } else {
    Serial.println("MPU_NOT_FOUND,0,0,0,0,0,0,0");
  }
}

// =====================================================
// I2C SCANNER
// =====================================================

void scanI2C() {
  Serial.println("Scanning I2C...");

  int deviceCount = 0;

  for (byte address = 1; address < 127; address++) {
    Wire.beginTransmission(address);
    byte error = Wire.endTransmission();

    if (error == 0) {
      Serial.print("I2C device ditemukan di alamat 0x");

      if (address < 16) {
        Serial.print("0");
      }

      Serial.println(address, HEX);
      deviceCount++;
    }
  }

  if (deviceCount == 0) {
    Serial.println("Tidak ada perangkat I2C terdeteksi.");
  }

  Serial.println("-----------------------------");
}

// =====================================================
// SETUP
// =====================================================

void setup() {
  rtcBootCount++;
  setCpuFrequencyMhz(80); // 80 MHz reduces CPU draw by >50%, ideal for 8h+ battery bench

  Serial.begin(115200);
  delay(1000);

  Serial.println();
  Serial.println("============================================");
  Serial.println("DIABFOOT GUARD SENSOR TEST");
  Serial.println("ESP32-C3 + 5 FSR + ADS1115 NTC + MPU6050");
  Serial.println(INSOLE_IS_LEFT ? "SOLE: LEFT" : "SOLE: RIGHT");
  Serial.println("============================================");

  analogReadResolution(12);

  if (BATTERY_PIN >= 0) {
    analogSetPinAttenuation(BATTERY_PIN, ADC_11db);
  }

  Wire.begin(PIN_SDA, PIN_SCL);
  Wire.setClock(400000); // 400 kHz Fast Mode for high-cadence 50 Hz reads
  Wire.setTimeOut(50);
  delay(100);

  Serial.println("Pin configuration:");
  Serial.println("FSR1(p1/HALLUX)  = GPIO0");
  Serial.println("FSR2(p2/MTH1)    = GPIO1");
  Serial.println("FSR3(p3/MTH2)    = GPIO2");
  Serial.println("FSR4(p4/MTH4)    = GPIO3");
  Serial.println("FSR5(p5/HEEL)    = GPIO4");
  Serial.println("NTC  = ADS1115 A0 (midfoot)");
  Serial.println("SDA  = GPIO8");
  Serial.println("SCL  = GPIO9");
  if (BATTERY_PIN >= 0) {
    Serial.println("BAT  = GPIO5 (divider 100k/100k dari VBAT)");
  } else {
    Serial.println("BAT  = tidak terpasang (BATTERY_PIN = -1)");
  }
  Serial.println();

  scanI2C();

  // =====================================================
  // INIT ADS1115
  // =====================================================

  if (ads.begin(0x48, &Wire) || ads.begin(0x49, &Wire)) {
    ads_ok = true;
    Serial.println("ADS1115 terdeteksi di alamat I2C");

    ads.setGain(GAIN_ONE);
    ads.setDataRate(RATE_ADS1115_860SPS);
    for (uint8_t ch = 0; ch < 4; ch++) {
      float v = ads.computeVolts(ads.readADC_SingleEnded(ch));
      if (v >= 0.2f && v <= 3.1f) {
        ntc_channel = ch;
        Serial.print("NTC thermistor ditemukan di ADS1115 channel A");
        Serial.println(ch);
        break;
      }
    }
  } else {
    ads_ok = false;
    Serial.println("WARNING: ADS1115 tidak terdeteksi.");
    Serial.println("Cek VDD, GND, SDA, SCL, dan ADDR.");
  }

  // =====================================================
  // INIT MPU (Universal Driver)
  // =====================================================

  mpu_ok = initUniversalMPU();
  if (mpu_ok) {
    Serial.println("MPU/ICM terdeteksi dan berhasil dikonfigurasi.");
  } else {
    Serial.println("WARNING: MPU tidak terdeteksi.");
    Serial.println("Stream BLE tetap jalan: IMU dikirim 0 (app: imuValid=false).");
  }

  // =====================================================
  // INIT BLE (v2 contract)
  // =====================================================
  setupBLE();
  esp_ble_tx_power_set(ESP_BLE_PWR_TYPE_DEFAULT, ESP_PWR_LVL_P3); // +3 dBm for lower peak current
  Serial.println("BLE advertising sebagai " INSOLE_NAME);

  if (BATTERY_PIN >= 0) {
    dbg_bat_pct = readBatteryPercent();
  }

  Serial.println();
  Serial.println("BLE stream (5 Hz) : ax,ay,az,gx,gy,gz,p1,p2,p3,p4,p5");
  Serial.println("BLE temperatur    : T,<tempC>[,<batPct>] (1/menit)");
  Serial.println("Format serial debug (31 field):");
  Serial.println("time_ms,fsr1_v,fsr1_r,fsr1_forceN,fsr1_pressurekPa,fsr2_v,fsr2_r,fsr2_forceN,fsr2_pressurekPa,fsr3_v,fsr3_r,fsr3_forceN,fsr3_pressurekPa,fsr4_v,fsr4_r,fsr4_forceN,fsr4_pressurekPa,fsr5_v,fsr5_r,fsr5_forceN,fsr5_pressurekPa,ntc_v_ads,ntc_r,ntc_tempC,mpu_status,ax,ay,az,gx,gy,gz,mpu_tempC");
}

// =====================================================
// LOOP — millis() scheduler (TD-12: BLE cadence terpisah dari debug)
// =====================================================

void loop() {
  unsigned long now = millis();

  if (now - lastStreamMs >= sample_period_ms) {
    lastStreamMs = now;
    sampleAndNotifyStream();
  }

  if (now - lastTempMs >= TEMP_PERIOD_MS) {
    lastTempMs = now;
    sampleAndNotifyTemp();
  }

  if (BATTERY_PIN >= 0 && now - lastBatMs >= BATTERY_READ_PERIOD_MS) {
    lastBatMs = now;
    readBattery();
  }

  if (now - lastSerialMs >= SERIAL_PERIOD_MS) {
    lastSerialMs = now;
    printSerialDebug();
  }

  // Energy-efficient sleep: yield CPU to FreeRTOS idle task until next tick
  now = millis();
  unsigned long timeToNext = (now - lastStreamMs < sample_period_ms) ? (sample_period_ms - (now - lastStreamMs)) : 0;
  if (timeToNext > 5) {
    delay(timeToNext - 2);
  } else {
    delay(1);
  }
}
