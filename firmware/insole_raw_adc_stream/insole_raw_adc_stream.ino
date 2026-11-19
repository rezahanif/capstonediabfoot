/*
 * DiabFoot Guard — 5 FSR Raw ADC + IMU + NTC Streamer (BLE + USB CDC)
 * Left Insole Multi-Step Walking Benchmark Firmware
 *
 * Physical GPIO Mapping (arranged for breadboard / wire routing tidiness):
 *   - GPIO 0: Heel   (S5 - Tumit)
 *   - GPIO 1: MTH1   (S2 - Metatarsal 1)
 *   - GPIO 2: Hallux (S1 - Ibu Jari)
 *   - GPIO 3: MTH2   (S3 - Metatarsal 2)
 *   - GPIO 4: MTH4   (S4 - Metatarsal 4)
 *
 * I2C Sensors (SDA = GPIO 8, SCL = GPIO 9):
 *   - MPU6050: Accel (m/s^2), Gyro (rad/s), Temp (C)
 *   - ADS1115: A0 -> NTC Thermistor (Midfoot)
 *
 * BLE Configuration:
 *   - Device Name: "Smart_Insole_Left"
 *   - Service UUID: 4fa2c732-ca9a-4c20-9492-c167df3c942b
 *   - Char UUID:    beb5483e-36e1-4688-b7f5-ea07361b26a8
 *
 * Dual Output (BLE GATT Notification + USB Serial @ 50 Hz):
 *   DATA,<time_ms>,<hallux>,<mth1>,<mth2>,<mth4>,<heel>,<ax>,<ay>,<az>,<gx>,<gy>,<gz>,<temp_c>
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

#define SERVICE_UUID        "4fa2c732-ca9a-4c20-9492-c167df3c942b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define INSOLE_NAME         "Smart_Insole_Left"

// Pin Configuration
const int PIN_HEEL   = 0; // GPIO 0 (ADC1_CH0)
const int PIN_MTH1   = 1; // GPIO 1 (ADC1_CH1)
const int PIN_HALLUX = 2; // GPIO 2 (ADC1_CH2)
const int PIN_MTH2   = 3; // GPIO 3 (ADC1_CH3)
const int PIN_MTH4   = 4; // GPIO 4 (ADC1_CH4)

const int PIN_SDA = 8;
const int PIN_SCL = 9;
const int LED_PIN = 8;

// ADC Configuration
const int ADC_MAX = 4095;
const int OVERSAMPLE_COUNT = 8;

// I2C Objects & State
Adafruit_ADS1115 ads;
bool ads_ok = false;
bool mpu_ok = false;
uint8_t mpu_address = 0x68;

// BLE Server & Characteristic
BLEServer* pServer = nullptr;
BLECharacteristic* pStreamCharacteristic = nullptr;
bool deviceConnected = false;
bool oldDeviceConnected = false;

// Stream Timing (50 Hz = 20 ms)
const unsigned long STREAM_INTERVAL_MS = 20;
unsigned long lastStreamMs = 0;
unsigned long sampleCount = 0;

class MyServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer* server) {
    deviceConnected = true;
    Serial.println("[BLE] Client connected!");
  }

  void onDisconnect(BLEServer* server) {
    deviceConnected = false;
    Serial.println("[BLE] Client disconnected! Restarting advertising...");
    server->getAdvertising()->start();
  }
};

int readOversampledADC(int pin) {
  long total = 0;
  for (int s = 0; s < OVERSAMPLE_COUNT; s++) {
    total += analogRead(pin);
    delayMicroseconds(20);
  }
  return constrain((int)(total / OVERSAMPLE_COUNT), 0, ADC_MAX);
}

bool initMPU() {
  const uint8_t candidateAddresses[] = {0x68, 0x69};
  for (uint8_t i = 0; i < 2; i++) {
    uint8_t addr = candidateAddresses[i];
    Wire.beginTransmission(addr);
    if (Wire.endTransmission() != 0) continue;

    Wire.beginTransmission(addr);
    Wire.write(0x6B);
    Wire.write(0x00);
    if (Wire.endTransmission() != 0) continue;
    delay(30);

    Wire.beginTransmission(addr);
    Wire.write(0x1C);
    Wire.write(0x10);
    Wire.endTransmission();

    Wire.beginTransmission(addr);
    Wire.write(0x1B);
    Wire.write(0x08);
    Wire.endTransmission();

    Wire.beginTransmission(addr);
    Wire.write(0x1A);
    Wire.write(0x03);
    Wire.endTransmission();

    mpu_address = addr;
    return true;
  }
  return false;
}

bool readMPU(float &ax, float &ay, float &az, float &gx, float &gy, float &gz, float &temp_c) {
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
  int16_t raw = ads.readADC_SingleEnded(0);
  float vout = ads.computeVolts(raw);
  if (vout <= 0.005f || vout >= 3.295f) return -999.0f;

  float r_ntc = 10000.0f * ((3.3f / vout) - 1.0f);
  if (r_ntc <= 0) return -999.0f;

  float steinhart = r_ntc / 10000.0f;
  steinhart = log(steinhart);
  steinhart /= 3950.0f;
  steinhart += 1.0f / (25.0f + 273.15f);
  steinhart = 1.0f / steinhart;
  steinhart -= 273.15f;
  return steinhart;
}

void setupBLE() {
  BLEDevice::init(INSOLE_NAME);
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

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
  pAdvertising->setMinPreferred(0x06); // Functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  Serial.printf("[BLE] Advertised as '%s' (UUID: %s)\n", INSOLE_NAME, SERVICE_UUID);
}

void setup() {
  Serial.begin(115200);
  delay(1000); // Allow USB CDC enumeration

  // Setup ADC
  analogReadResolution(12);
  analogSetAttenuation(ADC_11db); // 0 - 3.3V
  pinMode(PIN_HEEL, INPUT);
  pinMode(PIN_MTH1, INPUT);
  pinMode(PIN_HALLUX, INPUT);
  pinMode(PIN_MTH2, INPUT);
  pinMode(PIN_MTH4, INPUT);

  // Setup I2C
  Wire.begin(PIN_SDA, PIN_SCL, 400000);
  delay(50);

  // Init MPU
  mpu_ok = initMPU();

  // Init ADS1115
  if (ads.begin(0x48)) {
    ads_ok = true;
    ads.setGain(GAIN_ONE);
  }

  // Init BLE
  setupBLE();

  Serial.println("==========================================================");
  Serial.println("  DiabFoot Guard — 5 FSR RAW ADC + IMU Streamer (BLE + USB)");
  Serial.println("  Wiring: G0:Heel | G1:MTH1 | G2:Hallux | G3:MTH2 | G4:MTH4");
  Serial.printf("  I2C Devices: MPU6050: %s | ADS1115: %s\n",
                mpu_ok ? "OK" : "NOT FOUND", ads_ok ? "OK" : "NOT FOUND");
  Serial.println("==========================================================");
  Serial.println("HEADER:time_ms,hallux,mth1,mth2,mth4,heel,ax,ay,az,gx,gy,gz,temp_c");
}

void loop() {
  unsigned long now = millis();

  if (now - lastStreamMs >= STREAM_INTERVAL_MS) {
    lastStreamMs = now;

    // 1. Read 5 FSRs raw ADC from their dedicated pins
    int adc_heel   = readOversampledADC(PIN_HEEL);   // GPIO 0
    int adc_mth1   = readOversampledADC(PIN_MTH1);   // GPIO 1
    int adc_hallux = readOversampledADC(PIN_HALLUX); // GPIO 2
    int adc_mth2   = readOversampledADC(PIN_MTH2);   // GPIO 3
    int adc_mth4   = readOversampledADC(PIN_MTH4);   // GPIO 4

    // 2. Read IMU
    float ax = 0, ay = 0, az = 0, gx = 0, gy = 0, gz = 0, mpu_temp = 0;
    if (mpu_ok) {
      readMPU(ax, ay, az, gx, gy, gz, mpu_temp);
    }

    // 3. Read Temp (NTC or MPU fallback)
    float temp_c = mpu_temp;
    if (sampleCount % 50 == 0 && ads_ok) {
      float ntc = readNTCTemp();
      if (ntc > -100.0f) temp_c = ntc;
    }

    sampleCount++;

    // Format packet:
    // DATA,time_ms,hallux,mth1,mth2,mth4,heel,ax,ay,az,gx,gy,gz,temp_c
    char line[128];
    snprintf(line, sizeof(line),
             "DATA,%lu,%d,%d,%d,%d,%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.2f",
             now,
             adc_hallux, adc_mth1, adc_mth2, adc_mth4, adc_heel,
             ax, ay, az, gx, gy, gz,
             temp_c);

    // Stream 1: USB Serial CDC
    Serial.println(line);

    // Stream 2: BLE GATT Notification (if client connected)
    if (deviceConnected && pStreamCharacteristic != nullptr) {
      pStreamCharacteristic->setValue((uint8_t*)line, strlen(line));
      pStreamCharacteristic->notify();
    }
  }

  // Handle BLE disconnect re-advertising
  if (!deviceConnected && oldDeviceConnected) {
    delay(500);
    pServer->startAdvertising();
    Serial.println("[BLE] Restarted advertising");
    oldDeviceConnected = deviceConnected;
  }
  if (deviceConnected && !oldDeviceConnected) {
    oldDeviceConnected = deviceConnected;
  }
}
