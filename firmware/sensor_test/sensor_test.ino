#include <Wire.h>
#include <Adafruit_ADS1X15.h>
#include <math.h>

// =====================================================
// PIN CONFIGURATION
// =====================================================
const int PIN_FSR[5] = {0, 1, 2, 3, 4};
const int PIN_SDA = 8;
const int PIN_SCL = 9;

// =====================================================
// OBJECT SENSORS & STATE
// =====================================================
Adafruit_ADS1115 ads;
bool ads_ok = false;
bool mpu_ok = false;
uint8_t mpu_address = 0x68;
uint8_t mpu_whoami = 0;

// MPU Live Data
float ax = 0, ay = 0, az = 0;
float gx = 0, gy = 0, gz = 0;
float mpu_temp = 0;

// =====================================================
// ADC & FSR CONFIGURATION
// =====================================================
const float VCC = 3.3;
const int ADC_MAX = 4095;
const float R_FIXED_FSR = 10000.0;
const float FSR_AREA_M2 = 7.85e-5;

// =====================================================
// NTC CONFIGURATION VIA ADS1115
// =====================================================
const float R_FIXED_NTC = 10000.0;
const float NTC_NOMINAL = 10000.0;
const float TEMP_NOMINAL = 25.0;
const float BETA_COEFFICIENT = 3950.0;

// =====================================================
// FSR READ & CONVERSION
// =====================================================
float readESP32Voltage(int pin, int samples = 20) {
  long totalADC = 0;
  for (int i = 0; i < samples; i++) {
    totalADC += analogRead(pin);
    delay(2);
  }
  float avgADC = totalADC / (float)samples;
  return (avgADC / ADC_MAX) * VCC;
}

float voltageToFSRResistance(float vout) {
  if (vout <= 0.001) return 999999999.0;
  if (vout >= VCC) return 0.0;
  return R_FIXED_FSR * ((VCC / vout) - 1.0);
}

float estimateForceFromFSR(float r_fsr) {
  if (r_fsr <= 0 || r_fsr >= 999999999.0) return 0.0;
  float conductance_uS = 1000000.0 / r_fsr;
  float force_N;
  if (conductance_uS <= 1000.0) {
    force_N = conductance_uS / 80.0;
  } else {
    force_N = (conductance_uS - 1000.0) / 30.0;
  }
  return (force_N < 0) ? 0.0 : force_N;
}

float forceToPressureKPa(float force_N) {
  float pressure_Pa = force_N / FSR_AREA_M2;
  return pressure_Pa / 1000.0;
}

// =====================================================
// NTC READ & CONVERSION
// =====================================================
float readADSVoltage(uint8_t channel, int samples = 10) {
  if (!ads_ok || samples <= 0) return -1.0;
  float totalVoltage = 0;
  for (int i = 0; i < samples; i++) {
    int16_t raw = ads.readADC_SingleEnded(channel);
    totalVoltage += ads.computeVolts(raw);
    delay(5);
  }
  return totalVoltage / samples;
}

float voltageToNTCResistance(float vout) {
  if (vout <= 0.001) return 999999999.0;
  if (vout >= VCC) return 0.0;
  return R_FIXED_NTC * ((VCC / vout) - 1.0);
}

float resistanceToTemperatureC(float r_ntc) {
  if (r_ntc <= 0 || r_ntc >= 999999999.0) return -999.0;
  float steinhart = r_ntc / NTC_NOMINAL;
  steinhart = log(steinhart);
  steinhart /= BETA_COEFFICIENT;
  steinhart += 1.0 / (TEMP_NOMINAL + 273.15);
  steinhart = 1.0 / steinhart;
  steinhart -= 273.15;
  return steinhart;
}

// =====================================================
// I2C DIAGNOSTIC SCANNER
// =====================================================
void scanI2C() {
  Serial.println("--- I2C BUS SCAN ---");
  int count = 0;
  for (uint8_t addr = 1; addr < 127; addr++) {
    Wire.beginTransmission(addr);
    byte err = Wire.endTransmission();
    if (err == 0) {
      Serial.print("[I2C] Device found at 0x");
      if (addr < 16) Serial.print("0");
      Serial.print(addr, HEX);
      if (addr == 0x48) Serial.print(" (ADS1115 ADC)");
      else if (addr == 0x68 || addr == 0x69) Serial.print(" (MPU/ICM IMU)");
      Serial.println();
      count++;
    }
  }
  if (count == 0) {
    Serial.println("[I2C] WARNING: No I2C devices detected on SDA=8, SCL=9!");
  }
  Serial.println("--------------------");
}

// =====================================================
// UNIVERSAL MPU/ICM DRIVER (Supports WHO_AM_I 0x68, 0x70, 0x71, 0x74)
// =====================================================
bool initUniversalMPU() {
  const uint8_t candidateAddresses[] = {0x68, 0x69};

  for (uint8_t i = 0; i < 2; i++) {
    uint8_t addr = candidateAddresses[i];

    Wire.beginTransmission(addr);
    if (Wire.endTransmission() != 0) continue;

    // 1. Wake sensor from sleep (PWR_MGMT_1, reg 0x6B = 0x00)
    Wire.beginTransmission(addr);
    Wire.write(0x6B);
    Wire.write(0x00);
    if (Wire.endTransmission() != 0) continue;
    delay(30);

    // 2. Read WHO_AM_I (reg 0x75)
    Wire.beginTransmission(addr);
    Wire.write(0x75);
    Wire.endTransmission(false);
    Wire.requestFrom((uint8_t)addr, (uint8_t)1);
    mpu_whoami = Wire.available() ? Wire.read() : 0x00;

    Serial.print("[MPU] Device responding at 0x");
    Serial.print(addr, HEX);
    Serial.print(" with WHO_AM_I = 0x");
    Serial.print(mpu_whoami, HEX);

    // Identify variant
    if (mpu_whoami == 0x68) Serial.println(" (InvenSense MPU-6050)");
    else if (mpu_whoami == 0x70) Serial.println(" (InvenSense MPU-6500)");
    else if (mpu_whoami == 0x71) Serial.println(" (InvenSense MPU-9250)");
    else if (mpu_whoami == 0x74) Serial.println(" (InvenSense MPU-6515 / ICM-20689)");
    else Serial.println(" (Compatible MPU Clone)");

    // 3. Configure Accel (+/- 8g -> 4096 LSB/g)
    Wire.beginTransmission(addr);
    Wire.write(0x1C);
    Wire.write(0x10); // AFS_SEL = 2 (+/- 8g)
    Wire.endTransmission();

    // 4. Configure Gyro (+/- 500 deg/s -> 65.5 LSB/(deg/s))
    Wire.beginTransmission(addr);
    Wire.write(0x1B);
    Wire.write(0x08); // FS_SEL = 1 (+/- 500 deg/s)
    Wire.endTransmission();

    // 5. Digital Low Pass Filter (~21Hz bandwidth)
    Wire.beginTransmission(addr);
    Wire.write(0x1A);
    Wire.write(0x03);
    Wire.endTransmission();

    mpu_address = addr;
    Serial.print("[MPU] Universal driver successfully configured at 0x");
    Serial.println(mpu_address, HEX);
    return true;
  }

  return false;
}

bool readUniversalMPU() {
  if (!mpu_ok) return false;

  Wire.beginTransmission(mpu_address);
  Wire.write(0x3B); // ACCEL_XOUT_H
  if (Wire.endTransmission(false) != 0) return false;

  if (Wire.requestFrom((uint8_t)mpu_address, (uint8_t)14) != 14) return false;

  int16_t raw_ax = (Wire.read() << 8) | Wire.read();
  int16_t raw_ay = (Wire.read() << 8) | Wire.read();
  int16_t raw_az = (Wire.read() << 8) | Wire.read();
  int16_t raw_t  = (Wire.read() << 8) | Wire.read();
  int16_t raw_gx = (Wire.read() << 8) | Wire.read();
  int16_t raw_gy = (Wire.read() << 8) | Wire.read();
  int16_t raw_gz = (Wire.read() << 8) | Wire.read();

  // Convert to m/s^2 (+/- 8g: 4096 LSB/g, 1g = 9.80665 m/s^2)
  ax = (raw_ax / 4096.0f) * 9.80665f;
  ay = (raw_ay / 4096.0f) * 9.80665f;
  az = (raw_az / 4096.0f) * 9.80665f;

  // Convert to rad/s (+/- 500 deg/s: 65.5 LSB/(deg/s))
  gx = (raw_gx / 65.5f) * (3.14159265f / 180.0f);
  gy = (raw_gy / 65.5f) * (3.14159265f / 180.0f);
  gz = (raw_gz / 65.5f) * (3.14159265f / 180.0f);

  // Temperature formula
  mpu_temp = (raw_t / 340.0f) + 36.53f;

  return true;
}

// =====================================================
// SETUP
// =====================================================
void setup() {
  Serial.begin(115200);

  // Wait briefly for USB CDC connection
  unsigned long startWait = millis();
  while (!Serial && (millis() - startWait < 2000)) {
    delay(10);
  }

  Serial.println();
  Serial.println("============================================");
  Serial.println("DIABFOOT GUARD SENSOR TEST (UNIVERSAL MPU)");
  Serial.println("ESP32-C3 + 5 FSR + ADS1115 NTC + MPU6050/ICM");
  Serial.println("============================================");

  analogReadResolution(12);
  Wire.begin(PIN_SDA, PIN_SCL, 100000);
  Wire.setTimeOut(100);
  delay(100);

  // Scan bus
  scanI2C();

  // Init ADS1115
  if (ads.begin(0x48, &Wire)) {
    ads_ok = true;
    ads.setGain(GAIN_ONE);
    Serial.println("[ADS1115] Initialized successfully at 0x48");
  } else {
    ads_ok = false;
    Serial.println("[ADS1115] WARNING: ADS1115 not detected at 0x48");
  }

  // Init MPU with Universal Driver
  mpu_ok = initUniversalMPU();

  Serial.println();
  Serial.println("Format data (31 fields):");
  Serial.println(
    "time_ms,"
    "fsr1_v,fsr1_r,fsr1_forceN,fsr1_pressurekPa,"
    "fsr2_v,fsr2_r,fsr2_forceN,fsr2_pressurekPa,"
    "fsr3_v,fsr3_r,fsr3_forceN,fsr3_pressurekPa,"
    "fsr4_v,fsr4_r,fsr4_forceN,fsr4_pressurekPa,"
    "fsr5_v,fsr5_r,fsr5_forceN,fsr5_pressurekPa,"
    "ntc_v_ads,ntc_r,ntc_tempC,"
    "mpu_status,ax,ay,az,gx,gy,gz,mpu_tempC"
  );
}

// =====================================================
// LOOP
// =====================================================
void loop() {
  Serial.print(millis());
  Serial.print(",");

  // 1. Read 5 FSRs
  for (int i = 0; i < 5; i++) {
    float v_fsr = readESP32Voltage(PIN_FSR[i]);
    float r_fsr = voltageToFSRResistance(v_fsr);
    float force_N = estimateForceFromFSR(r_fsr);
    float pressure_kPa = forceToPressureKPa(force_N);
    Serial.print(v_fsr, 3);
    Serial.print(",");
    Serial.print(r_fsr, 2);
    Serial.print(",");
    Serial.print(force_N, 3);
    Serial.print(",");
    Serial.print(pressure_kPa, 2);
    Serial.print(",");
  }

  // 2. Read NTC
  float v_ntc = readADSVoltage(0);
  float r_ntc = -1.0;
  float temp_ntc = -999.0;
  if (v_ntc >= 0) {
    r_ntc = voltageToNTCResistance(v_ntc);
    temp_ntc = resistanceToTemperatureC(r_ntc);
  }
  Serial.print(v_ntc, 4);
  Serial.print(",");
  Serial.print(r_ntc, 2);
  Serial.print(",");
  Serial.print(temp_ntc, 2);
  Serial.print(",");

  // 3. Read Universal MPU
  if (readUniversalMPU()) {
    Serial.print("OK,");
    Serial.print(ax, 3);
    Serial.print(",");
    Serial.print(ay, 3);
    Serial.print(",");
    Serial.print(az, 3);
    Serial.print(",");
    Serial.print(gx, 3);
    Serial.print(",");
    Serial.print(gy, 3);
    Serial.print(",");
    Serial.print(gz, 3);
    Serial.print(",");
    Serial.println(mpu_temp, 2);
  } else {
    Serial.println("MPU_NOT_FOUND,0,0,0,0,0,0,0");
  }

  delay(500);
}
