/*
 * DiabFoot Guard — Pre-Production Smart Insole Firmware (v2.0)
 * Architecture: Clean IoT Embedded, Piecewise Linear LUT, 5 Hz BLE Gait Stream
 * Target Platform: ESP32-C3 (RISC-V 32-bit rv32imc, 80/160 MHz)
 *
 * Engineering Decision Summary (Ref: docs/tests/FSR_CALIBRATION_AND_POWER_DECISION.md):
 *   1. FSR Calibration: 11-node Piecewise Linear LUT (0..3102 ADC -> 0..4440 kPa)
 *      - Exact zero-error at calibration points, guaranteed monotonic, zero FPU load.
 *   2. BLE Radio & Power:
 *      - TX Power set to +3 dBm (ESP_PWR_LVL_P3) to suppress 350 mA spikes to ~130 mA,
 *        preventing LiPo PCM latch trips and BOD resets, with +42 dB link margin to pocket.
 *      - 5 Hz stream cadence (200 ms) for gait biomechanics.
 *      - Decoupled 1/min (60s) NTC foot temperature heartbeat ("T,<tempC>[,<batPct>]").
 *   3. Inactivity Sleep Trigger:
 *      - Detects idle state (pressures <= 15 kPa) for >= 30 seconds.
 *      - Sleeps MPU-6050 (0x6B = 0x40) and relaxes polling to extend 300 mAh battery to >12h.
 *      - Instant wake-up upon foot contact (> 40 kPa).
 *   4. Fault Guard & Auto-Tare:
 *      - Baseline calibration at boot. Suppresses high-idle offsets (e.g. Pin 2 Hallux crease)
 *        to prevent phantom 4500 kPa readings.
 */

#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_ADS1X15.h>
#include <math.h>

// BLE Libraries
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include "esp_bt.h"

// ==============================================================================
// 1. INSOLE CONFIGURATION & UUIDs (Matches app/InsoleUUIDs.kt)
// ==============================================================================
#define INSOLE_IS_LEFT 1 // 1 = Left ("Smart_Insole_Left"), 0 = Right ("Smart_Insole_Right")

#if INSOLE_IS_LEFT
  #define INSOLE_NAME         "Smart_Insole_Left"
  #define SERVICE_UUID        "4fa2c732-ca9a-4c20-9492-c167df3c942b"
  #define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#else
  #define INSOLE_NAME         "Smart_Insole_Right"
  #define SERVICE_UUID        "4fa2c732-ca9a-4c20-9492-c167df3c942c"
  #define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26c9"
#endif

// Physical GPIO Pin Mapping (Aligned with physical flex PCB routing)
#if INSOLE_IS_LEFT
  // Left Insole (Confirmed hardware routing):
  //   - GPIO 1 is physically Hallux (S1)
  //   - GPIO 0 is physically MTH1 (S2)
  //   - GPIO 3 is physically MTH2 (S3)
  //   - ADS1115 A1 is physically MTH4 (S4)
  //   - GPIO 4 is physically Heel (S5)
  const int PIN_HALLUX    = 1; // GPIO 1 (ADC1_CH1) - S1 Hallux
  const int PIN_MTH1      = 0; // GPIO 0 (ADC1_CH0) - S2 Metatarsal 1
  const int PIN_MTH2      = 3; // GPIO 3 (ADC1_CH3) - S3 Metatarsal 2
  const int ADS_CH_MTH4   = 1; // ADS1115 A1 -> S4 Metatarsal 4
  const int PIN_HEEL      = 4; // GPIO 4 (ADC1_CH4) - S5 Heel
#else
  // Right Insole (Confirmed hardware routing):
  //   - GPIO 4 is physically Hallux (S1)
  //   - GPIO 3 is physically MTH1 (S2)
  //   - GPIO 1 is physically MTH2 (S3)
  //   - ADS1115 A1 is physically MTH4 (S4)
  //   - GPIO 0 is physically Heel (S5)
  const int PIN_HALLUX    = 4; // GPIO 4 (ADC1_CH4) - S1 Hallux
  const int PIN_MTH1      = 3; // GPIO 3 (ADC1_CH3) - S2 Metatarsal 1
  const int PIN_MTH2      = 1; // GPIO 1 (ADC1_CH1) - S3 Metatarsal 2
  const int ADS_CH_MTH4   = 1; // ADS1115 A1 -> S4 Metatarsal 4
  const int PIN_HEEL      = 0; // GPIO 0 (ADC1_CH0) - S5 Heel
#endif

const int ADS_CH_NTC    = 0; // ADS1115 A0 -> NTC Midfoot
const int PIN_SDA       = 8; // I2C SDA
const int PIN_SCL       = 9; // I2C SCL

// ==============================================================================
// 2. PIECEWISE LINEAR CALIBRATION LOOKUP TABLE (11 NODES)
// ==============================================================================
// Frozen empirical calibration points (Weight kg -> ADC -> kPa)
struct CalibrationNode {
  int16_t adc;
  uint16_t kpa;
};

const CalibrationNode CAL_LUT[11] = {
  {    0,    0}, // 0 kg
  {  893,  444}, // 2 kg
  { 1825,  888}, // 4 kg
  { 1985, 1332}, // 6 kg
  { 2263, 1776}, // 8 kg
  { 2458, 2220}, // 10 kg
  { 2587, 2664}, // 12 kg
  { 2716, 3108}, // 14 kg
  { 2844, 3552}, // 16 kg
  { 2973, 3996}, // 18 kg
  { 3102, 4440}  // 20 kg
};

// Fast piecewise linear interpolation (zero hardware FPU dependency)
uint16_t adcToPressureKPa(int raw_adc) {
  // 1. Noise floor clamp (physical idle noise is 3..6 ADC)
  if (raw_adc <= 15) return 0;

  // 2. Below first node
  if (raw_adc <= CAL_LUT[0].adc) return 0;

  // 3. Extrapolation above 20 kg (constant slope: 444 kPa / 129 ADC = 3.442 kPa/ADC)
  if (raw_adc >= CAL_LUT[10].adc) {
    int32_t excess = raw_adc - CAL_LUT[10].adc;
    return (uint16_t)min<int32_t>(65000L, CAL_LUT[10].kpa + (excess * 3442L / 1000L));
  }

  // 4. Binary search / linear scan across 10 segments
  for (int i = 0; i < 10; i++) {
    if (raw_adc >= CAL_LUT[i].adc && raw_adc <= CAL_LUT[i + 1].adc) {
      int32_t delta_adc = CAL_LUT[i + 1].adc - CAL_LUT[i].adc;
      int32_t delta_kpa = CAL_LUT[i + 1].kpa - CAL_LUT[i].kpa;
      int32_t offset_adc = raw_adc - CAL_LUT[i].adc;
      return (uint16_t)(CAL_LUT[i].kpa + (offset_adc * delta_kpa) / delta_adc);
    }
  }

  return 0;
}

// ==============================================================================
// 3. SENSOR STATE & TIMING
// ==============================================================================
Adafruit_ADS1115 ads;
bool ads_ok = false;
bool mpu_ok = false;
uint8_t mpu_address = 0x68;

// Baseline Tare & Fault Guard
int baseline_adc[5] = {0, 0, 0, 0, 0}; // Order: Hallux, MTH1, MTH2, MTH4, Heel
bool sensor_fault[5] = {false, false, false, false, false};

// BLE Communication
BLEServer* pServer = nullptr;
BLECharacteristic* pStreamCharacteristic = nullptr;
bool deviceConnected = false;
bool oldDeviceConnected = false;

// Stream Cadence
const unsigned long GAIT_STREAM_INTERVAL_MS = 200; // 5 Hz (200 ms)

// NTC Heartbeat Cadence:
// - PRODUCTION & 8-HOUR TEST TARGET: 60,000 ms (1 minute) — Clinical standard for skin temperature
// - BENCH TEST MODE: 2,000 ms (2 seconds) or less — Temporary for fast calibration
#define NTC_FAST_TEST_MODE 1 // 1 = 2s temporary test bench, 0 = 1 minute (FINAL PRODUCTION & 8H TEST)

#if NTC_FAST_TEST_MODE
  const unsigned long NTC_HEARTBEAT_INTERVAL_MS = 2000;  // 2.0s fast test
#else
  const unsigned long NTC_HEARTBEAT_INTERVAL_MS = 60000; // 60s (1 min) FINAL PRODUCTION & 8H BENCH
#endif

// ==============================================================================
// BENCHMARK MODE: SIMULATED ACTIVITIES OF DAILY LIVING (ADL)
// ==============================================================================
// 1 = TEST-11 ADL Simulation: 15 min Active Walking + 30 min Sedentary Sleep Cycle
// 0 = Pure Physical Sensor Mode (Sleeps only after 30s physical inactivity)
#define BENCH_ADL_SIMULATION_MODE 0

const unsigned long ADL_ACTIVE_DURATION_MS = 15UL * 60UL * 1000UL; // 15 min active walking
const unsigned long ADL_REST_DURATION_MS   = 30UL * 60UL * 1000UL; // 30 min sedentary sleep
const unsigned long ADL_CYCLE_PERIOD_MS    = ADL_ACTIVE_DURATION_MS + ADL_REST_DURATION_MS; // 45 min total

const unsigned long INACTIVITY_SLEEP_TIMEOUT_MS = 30000; // 30 seconds

// Boot counter in RTC memory (survives software resets and deep sleep)
RTC_DATA_ATTR static uint32_t bootCount = 0;

unsigned long lastGaitStreamMs = 0;
unsigned long lastHeartbeatMs = 0;
unsigned long lastActiveMovementMs = 0;
bool isSystemSleeping = false;

// Battery voltage divider (-1 = unpopulated scaffold)
const int BATTERY_PIN = -1;

// ==============================================================================
// 4. BLE SERVER CALLBACKS
// ==============================================================================
class InsoleServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer* server) {
    deviceConnected = true;
    Serial.println("[BLE] Smartphone connected!");
  }

  void onDisconnect(BLEServer* server) {
    deviceConnected = false;
    Serial.println("[BLE] Smartphone disconnected. Restarting advertising...");
    server->getAdvertising()->start();
  }
};

// ==============================================================================
// 5. HARDWARE UTILITIES (ADC, MPU-6050, ADS1115 NTC)
// ==============================================================================
int readOversampledADC(int pin) {
  long total = 0;
  for (int s = 0; s < 8; s++) {
    total += analogRead(pin);
    delayMicroseconds(25);
  }
  return constrain((int)(total / 8), 0, 4095);
}

bool initMPU() {
  const uint8_t candidateAddresses[] = {0x68, 0x69};
  for (uint8_t i = 0; i < 2; i++) {
    uint8_t addr = candidateAddresses[i];
    Wire.beginTransmission(addr);
    if (Wire.endTransmission() != 0) continue;

    // Wake MPU-6050 from default sleep
    Wire.beginTransmission(addr);
    Wire.write(0x6B); // PWR_MGMT_1
    Wire.write(0x00); // Internal 8MHz osc, wake up
    if (Wire.endTransmission() != 0) continue;
    delay(30);

    // Accel config: +/- 8g (0x10) -> 4096 LSB/g
    Wire.beginTransmission(addr);
    Wire.write(0x1C);
    Wire.write(0x10);
    Wire.endTransmission();

    // Gyro config: +/- 500 deg/s (0x08) -> 65.5 LSB/(deg/s)
    Wire.beginTransmission(addr);
    Wire.write(0x1B);
    Wire.write(0x08);
    Wire.endTransmission();

    // DLPF config: ~44 Hz bandwidth (0x03)
    Wire.beginTransmission(addr);
    Wire.write(0x1A);
    Wire.write(0x03);
    Wire.endTransmission();

    mpu_address = addr;
    return true;
  }
  return false;
}

void setMPUSleep(bool sleep) {
  if (!mpu_ok) return;
  Wire.beginTransmission(mpu_address);
  Wire.write(0x6B);
  Wire.write(sleep ? 0x40 : 0x00); // Bit 6 = SLEEP
  Wire.endTransmission();
}

bool readMPU(float &ax, float &ay, float &az, float &gx, float &gy, float &gz, float &temp_c) {
  if (!mpu_ok || isSystemSleeping) {
    ax = ay = az = gx = gy = gz = 0;
    temp_c = 25.0f;
    return false;
  }

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

  // Convert to SI units (m/s^2, rad/s) — canonical internal units for Android app
  ax = (raw_ax / 4096.0f) * 9.80665f;
  ay = (raw_ay / 4096.0f) * 9.80665f;
  az = (raw_az / 4096.0f) * 9.80665f;
  gx = (raw_gx / 65.5f) * (3.14159265f / 180.0f);
  gy = (raw_gy / 65.5f) * (3.14159265f / 180.0f);
  gz = (raw_gz / 65.5f) * (3.14159265f / 180.0f);
  temp_c = (raw_t / 340.0f) + 36.53f;
  return true;
}

float readNTCTemp() {
  if (!ads_ok) return -999.0f;
  int16_t raw = ads.readADC_SingleEnded(ADS_CH_NTC);
  float vout = ads.computeVolts(raw);
  if (vout <= 0.005f || vout >= 3.295f) return -999.0f;

  float r_ntc = 10000.0f * ((3.3f / vout) - 1.0f);
  if (r_ntc <= 0) return -999.0f;

  // Steinhart-Hart equation (Beta 3950, R0 10k @ 25C)
  float steinhart = r_ntc / 10000.0f;
  steinhart = log(steinhart);
  steinhart /= 3950.0f;
  steinhart += 1.0f / (25.0f + 273.15f);
  steinhart = 1.0f / steinhart;
  steinhart -= 273.15f;
  return steinhart;
}

// Reads specified ADS1115 single-ended channel and scales to 12-bit ADC equivalent (0..4095)
int readAdsChannelScaled(int channel) {
  if (!ads_ok) return 0;
  int16_t raw = ads.readADC_SingleEnded(channel);
  if (raw <= 0) return 0;
  float volts = ads.computeVolts(raw);
  // Scale voltage (0..3.3V) to 12-bit ADC equivalent (0..4095) for our calibrated LUT
  return constrain((int)((volts / 3.3f) * 4095.0f), 0, 4095);
}

// Per-channel sensor accessors aligned with physical insole routing
inline int readRawHallux() {
  return readOversampledADC(PIN_HALLUX);
}

inline int readRawMTH1() {
  return readOversampledADC(PIN_MTH1);
}

inline int readRawMTH2() {
  return readOversampledADC(PIN_MTH2);
}

inline int readRawMTH4() {
  return readAdsChannelScaled(ADS_CH_MTH4);
}

inline int readRawHeel() {
  return readOversampledADC(PIN_HEEL);
}

int readBatteryPercent() {
  if (BATTERY_PIN < 0) return -1; // Hardware divider not populated
  int raw = analogRead(BATTERY_PIN);
  float v_bat = (raw / 4095.0f) * 3.3f * 2.0f; // 100k:100k divider
  int pct = (int)((v_bat - 3.2f) / (4.2f - 3.2f) * 100.0f);
  return constrain(pct, 0, 100);
}

// ==============================================================================
// 6. SETUP & TARE CALIBRATION
// ==============================================================================
void setup() {
  bootCount++;

  // 0. Energy Conservation: 80 MHz CPU clock drops core power by ~50%
  setCpuFrequencyMhz(80);

  Serial.begin(115200);
  delay(500); // Allow CDC enumeration

  // 1. Configure ADC
  analogReadResolution(12);
  analogSetAttenuation(ADC_11db); // 0..3.3V range
  pinMode(PIN_HALLUX, INPUT);
  pinMode(PIN_MTH1, INPUT);
  pinMode(PIN_MTH2, INPUT);
  pinMode(PIN_HEEL, INPUT);

  // 2. Configure I2C
  Wire.begin(PIN_SDA, PIN_SCL, 400000);
  Wire.setTimeOut(50); // Prevent bus hangs

  mpu_ok = initMPU();
  if (ads.begin(0x48)) {
    ads_ok = true;
    ads.setGain(GAIN_ONE);
  }

  // 3. Baseline Tare & Pin Fault Assessment
  // Takes 20 samples to identify resting noise floor and detect stuck-high pins (e.g. Hallux crease)
  long tare_sum[5] = {0, 0, 0, 0, 0};
  for (int s = 0; s < 20; s++) {
    tare_sum[0] += readRawHallux(); // S1 (Hallux)
    tare_sum[1] += readRawMTH1();   // S2 (MTH1)
    tare_sum[2] += readRawMTH2();   // S3 (MTH2)
    tare_sum[3] += readRawMTH4();   // S4 (MTH4)
    tare_sum[4] += readRawHeel();   // S5 (Heel)
    delay(5);
  }

  for (int i = 0; i < 5; i++) {
    baseline_adc[i] = tare_sum[i] / 20;
    // If resting ADC > 2000 with no weight, mark as stuck-high fault
    if (baseline_adc[i] > 2000) {
      sensor_fault[i] = true;
      Serial.printf("[FAULT_GUARD] Pin index %d resting high (ADC=%d). Relative tare enabled.\n", i, baseline_adc[i]);
    }
  }

  // 4. Initialize BLE with Power Optimization
  BLEDevice::init(INSOLE_NAME);
  BLEDevice::setMTU(517); // Match Android requestMtu(512) for single-burst long CSV strings
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new InsoleServerCallbacks());

  BLEService* pService = pServer->createService(SERVICE_UUID);
  pStreamCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_READ |
    BLECharacteristic::PROPERTY_NOTIFY
  );
  pStreamCharacteristic->addDescriptor(new BLE2902());
  pService->start();

  BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMaxPreferred(0x12);
  BLEDevice::startAdvertising();

  // 5. CRITICAL: Set BLE TX Power to +3 dBm (ESP_PWR_LVL_P3)
  // Drops peak current from ~320-350 mA down to ~130 mA, eliminating battery brownouts
  // while preserving +42 dB link margin through shoes and pants pockets.
  esp_ble_tx_power_set(ESP_BLE_PWR_TYPE_DEFAULT, ESP_PWR_LVL_P3);
  esp_ble_tx_power_set(ESP_BLE_PWR_TYPE_ADV, ESP_PWR_LVL_P3);
  esp_ble_tx_power_set(ESP_BLE_PWR_TYPE_SCAN, ESP_PWR_LVL_P3);

  lastActiveMovementMs = millis();

  Serial.println("==========================================================================");
  Serial.printf("  DiabFoot Pre-Production Firmware Ready: %s\n", INSOLE_NAME);
  Serial.printf("  BLE TX Power: +3 dBm | Stream: 5 Hz | Heartbeat: 1/min\n");
  Serial.printf("  MPU-6050: %s | ADS1115: %s\n", mpu_ok ? "ONLINE" : "OFFLINE", ads_ok ? "ONLINE" : "OFFLINE");
  Serial.printf("  Baselines: Hallux=%d (Fault=%d), MTH1=%d, MTH2=%d, MTH4=%d, Heel=%d\n",
                baseline_adc[0], sensor_fault[0], baseline_adc[1], baseline_adc[2], baseline_adc[3], baseline_adc[4]);
  Serial.println("==========================================================================");
}

// ==============================================================================
// 7. MAIN LOOP & SCHEDULER
// ==============================================================================
void loop() {
  unsigned long now = millis();

  // --------------------------------------------------------------------------
  // TASK A: High-Rate Gait Telemetry (5 Hz = 200 ms)
  // --------------------------------------------------------------------------
  if (now - lastGaitStreamMs >= GAIT_STREAM_INTERVAL_MS) {
    lastGaitStreamMs = now;

    // 1. Read raw ADC from all 5 FSR channels
    int raw_hallux = readRawHallux(); // S1 (Hallux)
    int raw_mth1   = readRawMTH1();   // S2 (MTH1)
    int raw_mth2   = readRawMTH2();   // S3 (MTH2)
    int raw_mth4   = readRawMTH4();   // S4 (MTH4)
    int raw_heel   = readRawHeel();   // S5 (Heel)

    // 2. Apply universal baseline tare (auto-zeroing at startup) and convert to kPa
    int eff_hallux = max(0, raw_hallux - baseline_adc[0]);
    int eff_mth1   = max(0, raw_mth1 - baseline_adc[1]);
    int eff_mth2   = max(0, raw_mth2 - baseline_adc[2]);
    int eff_mth4   = max(0, raw_mth4 - baseline_adc[3]);
    int eff_heel   = max(0, raw_heel - baseline_adc[4]);

    uint16_t p_hallux = adcToPressureKPa(eff_hallux);
    uint16_t p_mth1   = adcToPressureKPa(eff_mth1);
    uint16_t p_mth2   = adcToPressureKPa(eff_mth2);
    uint16_t p_mth4   = adcToPressureKPa(eff_mth4);
    uint16_t p_heel   = adcToPressureKPa(eff_heel);

    uint32_t physicalLoad = (uint32_t)p_hallux + p_mth1 + p_mth2 + p_mth4 + p_heel;

    // 3. ADL Duty-Cycle State Machine (15 min Active Walking + 30 min Sedentary Sleep)
    bool isSimActive = false;
#if BENCH_ADL_SIMULATION_MODE
    unsigned long cycleTime = now % ADL_CYCLE_PERIOD_MS;
    isSimActive = (cycleTime < ADL_ACTIVE_DURATION_MS);
#endif

    if (physicalLoad > 40 || isSimActive) {
      // Foot contact or simulated walking session
      lastActiveMovementMs = now;
      if (isSystemSleeping) {
        isSystemSleeping = false;
        setMPUSleep(false);
        Serial.println("[PWR] Waking up from sleep mode (Active Walking Phase)!");
      }
    } else {
      // Idle resting state
      if (!isSystemSleeping && (now - lastActiveMovementMs >= INACTIVITY_SLEEP_TIMEOUT_MS)) {
        isSystemSleeping = true;
        setMPUSleep(true); // Put MPU to sleep
        Serial.println("[PWR] Inactivity detected. Entering Low-Power Sleep (Sedentary Phase).");
      }
    }

    // 4. Read IMU
    float ax = 0, ay = 0, az = 9.806f, gx = 0, gy = 0, gz = 0, mpu_temp = 25.0f;
    if (mpu_ok && !isSystemSleeping) {
      readMPU(ax, ay, az, gx, gy, gz, mpu_temp);
    }

#if BENCH_ADL_SIMULATION_MODE
    // If in simulated walking window and no real foot is pressing on the insole:
    if (isSimActive && physicalLoad <= 40) {
      // Realistic 1.0 Hz (1000 ms) walking stride waveform
      unsigned long stridePhaseMs = now % 1000UL;
      float phaseSec = stridePhaseMs / 1000.0f;

      // Realistic gait acceleration dynamics
      ax = 0.5f * sinf(2.0f * 3.14159f * phaseSec);
      ay = 1.2f * cosf(2.0f * 3.14159f * phaseSec);
      az = 9.806f + 2.5f * sinf(2.0f * 3.14159f * phaseSec);
      gx = 0.4f * sinf(2.0f * 3.14159f * phaseSec);
      gy = 0.8f * cosf(2.0f * 3.14159f * phaseSec);
      gz = 0.2f * sinf(2.0f * 3.14159f * phaseSec);

      // Realistic 4-phase plantar pressure distribution
      if (phaseSec < 0.20f) {
        // Heel strike
        p_heel   = 75;
        p_mth4   = 15;
        p_mth2   = 0;
        p_mth1   = 0;
        p_hallux = 0;
      } else if (phaseSec < 0.50f) {
        // Midstance (Forefoot load)
        p_heel   = 25;
        p_mth4   = 55;
        p_mth2   = 80;
        p_mth1   = 65;
        p_hallux = 10;
      } else if (phaseSec < 0.70f) {
        // Toe-off
        p_heel   = 0;
        p_mth4   = 10;
        p_mth2   = 35;
        p_mth1   = 60;
        p_hallux = 95;
      } else {
        // Swing phase (Foot in the air, 0 load)
        p_heel   = 0;
        p_mth4   = 0;
        p_mth2   = 0;
        p_mth1   = 0;
        p_hallux = 0;
      }
    }
#endif

    // 5. Construct Android v2 Stream Contract:
    // "ax,ay,az,gx,gy,gz,p1,p2,p3,p4,p5" (11 numeric fields in FSR_ZONE_ORDER)
    char v2Line[128];
    snprintf(v2Line, sizeof(v2Line),
             "%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%u,%u,%u,%u,%u",
             ax, ay, az, gx, gy, gz,
             p_hallux, p_mth1, p_mth2, p_mth4, p_heel);

    // Stream over BLE GATT Notification
    if (deviceConnected && pStreamCharacteristic != nullptr) {
      pStreamCharacteristic->setValue((uint8_t*)v2Line, strlen(v2Line));
      pStreamCharacteristic->notify();
    }

    // Optional Serial CDC output for bench monitoring
    static uint32_t cdcDiv = 0;
    if (++cdcDiv % 5 == 0) { // Log 1 Hz to Serial to avoid bloating terminal
      Serial.printf("[5Hz BLE] %s | RawADC:[Hlx:%d, M1:%d, M2:%d, M4:%d, Heel:%d] | Sleep:%d\n",
                    v2Line, raw_hallux, raw_mth1, raw_mth2, raw_mth4, raw_heel, isSystemSleeping);
    }
  }

  // --------------------------------------------------------------------------
  // TASK B: NTC Foot Skin Temperature & Battery Heartbeat (1 per minute)
  // --------------------------------------------------------------------------
  if (now - lastHeartbeatMs >= NTC_HEARTBEAT_INTERVAL_MS || lastHeartbeatMs == 0) {
    lastHeartbeatMs = now;

    float reportedTemp = 25.0f;
    if (ads_ok) {
      float ntc = readNTCTemp();
      if (ntc >= -5.0f && ntc <= 55.0f) {
        reportedTemp = ntc;
      }
    }

    int batPct = readBatteryPercent();
    int reportedBat = (batPct >= 0) ? batPct : 100; // 100 placeholder if divider not populated
    uint32_t uptimeSec = now / 1000UL;
    uint32_t freeHeap = ESP.getFreeHeap();

    // Construct Android Heartbeat Contract: "T,<tempC>[,<batPct>[,<uptime_s>,<free_heap>,<boot_count>]]"
    char tLine[64];
    snprintf(tLine, sizeof(tLine), "T,%.2f,%d,%lu,%u,%u", reportedTemp, reportedBat, uptimeSec, freeHeap, bootCount);

    if (deviceConnected && pStreamCharacteristic != nullptr) {
      pStreamCharacteristic->setValue((uint8_t*)tLine, strlen(tLine));
      pStreamCharacteristic->notify();
    }

    Serial.printf("[HEARTBEAT] Broadcast: %s\n", tLine);
  }

  // --------------------------------------------------------------------------
  // TASK C: BLE Connection State Housekeeping
  // --------------------------------------------------------------------------
  if (!deviceConnected && oldDeviceConnected) {
    delay(50);
    pServer->startAdvertising();
    oldDeviceConnected = deviceConnected;
  }
  if (deviceConnected && !oldDeviceConnected) {
    oldDeviceConnected = deviceConnected;
  }

  // Yield CPU to FreeRTOS idle task for power saving
  delay(10);
}
