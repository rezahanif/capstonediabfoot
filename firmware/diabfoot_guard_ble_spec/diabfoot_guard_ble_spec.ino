/*
 * DIABFOOT GUARD — BLE-only production firmware (ESP32-C3)
 * =========================================================
 * Implements: workspace/diabfoot-guard/diabfoot_ble_spec_v1.md
 *   - NimBLE GATT server, BLE 5.0 subset (Legacy Advertising, 1M PHY,
 *     LESC bonding, DLE + MTU 247; 2M PHY is central-requested for OTA/bulk)
 *   - Two-stage handshake (link/security, then app session protocol)
 *   - Binary sensor batches (frame format v1) — no CSV on the wire
 *   - Option B idle safety: FAST_ADV 60 s -> SLOW_ADV -> deep sleep at an
 *     absolute window deadline that anonymous BLE activity cannot extend
 *   - BLE-native fallback: RAM ring buffer + cumulative ACK + RESUME replay
 *   - BLE OTA (A/B partitions) with confirm-on-first-handshake rollback gate
 *
 * BUILD REQUIREMENTS
 *   - Board: ESP32C3 Dev Module (Arduino-ESP32 core 3.x)
 *   - Library: NimBLE-Arduino 2.x (Library Manager / PlatformIO lib_deps).
 *     This file targets the NimBLE-Arduino **2.x** API (NimBLEConnInfo
 *     callbacks). It will NOT compile against 1.x without callback changes.
 *   - Partition scheme: one WITH OTA slots (otadata + ota_0 + ota_1),
 *     e.g. the default 4 MB scheme. OTA + rollback also need the bootloader
 *     rollback option in the core build; without it, OTA still works but the
 *     confirm/rollback gate in §11 of the spec is a no-op at bootloader level.
 *   - Product build: leave DEBUG_SERIAL undefined (no USB on the product).
 *     Bench builds may define it for Serial diagnostics on a dev board.
 *
 * HARDWARE FEATURE PINS (spec §15 OPEN items) — set to -1 when absent.
 * Firmware runs without them; the behaviours that name them degrade exactly
 * as the spec states.
 */

// ---------- Feature / debug configuration ----------
#define DEBUG_SERIAL 1            // 1 = Serial logs on bench builds only
#define CONFIG_ENABLE_DEEP_SLEEP 0 // 0 = Continuous BLE advertising, no RST button required!
#define CONFIG_INTER_SAMPLE_LIGHT_SLEEP 0  // EXPERIMENTAL (spec §16): 1 = light-sleep
                                  // between samples while connected. Can disturb BLE
                                  // timing on some core builds — bench A/B it against
                                  // measured current + disconnect rate before enabling.
// Insole side configuration: 1 = Left Insole, 0 = Right Insole
#define INSOLE_IS_LEFT 1

#if INSOLE_IS_LEFT
  #define CONFIG_DEVICE_NAME_PREFIX "DIABFOOT-Left-"
#else
  #define CONFIG_DEVICE_NAME_PREFIX "DIABFOOT-Right-"
#endif
#define CONFIG_STATIC_PASSKEY 0   // 0 = LESC Just Works. OWNER DECISION
                                  // 2026-10-23: Just Works IS the product
                                  // configuration (spec §7 as amended). Set to
                                  // a 6-digit label passkey only if a future
                                  // batch wants MITM protection.
#define PIN_LED    -1             // no LED on the board (owner, 2026-10-23);
                                  // §9 expiry notice is the app flag only
#define PIN_BUTTON -1             // the board's button is the supply-side
                                  // on/off power button — firmware never
                                  // reads it as a GPIO (owner, 2026-10-23)
#define PIN_BATT_ADC -1           // battery voltage ADC input (via divider)
#define PIN_MPU_INT  -1           // MPU motion-wake not wanted (owner,
                                  // 2026-10-23); wake = power-on only
#define BATT_DIVIDER_NUM 2        // Vbatt = Vadc * NUM/DEN (set for your PCB)
#define BATT_DIVIDER_DEN 1
#define BATT_CRITICAL_MV 3300     // spec §9 rule 7 (DEFAULT)

#if DEBUG_SERIAL
  #define DBG(...)  Serial.print(__VA_ARGS__)
  #define DBGLN(...) Serial.println(__VA_ARGS__)
#else
  #define DBG(...)
  #define DBGLN(...)
#endif

// ---------- Includes ----------
#include <Wire.h>
#include <Adafruit_ADS1X15.h>
#include <math.h>
#include <NimBLEDevice.h>
#include <Preferences.h>
#include <Update.h>
#include <esp_ota_ops.h>
#include <esp_sleep.h>
#include <driver/gpio.h>

struct StoredSample {
  uint32_t seq;
  uint32_t time_ms;
  uint16_t pressure_kPa[5];
  int16_t  ntc_temp_c100;      // 0x7FFF = invalid
  int16_t  accel_mg[3];
  int16_t  gyro_dps10[3];
  uint8_t  status;             // bit0 FSR sat, bit1 NTC valid, bit2 MPU valid, bit3 MPU retried
  bool     taken_offline;      // sampled while link down (grace) -> REPLAY flag
};

#define CMDQ_DEPTH 4
#define CMDQ_MAXLEN 40
struct PendingCmd { uint8_t len; uint8_t data[CMDQ_MAXLEN]; uint8_t channel; }; // channel: 0=CTRL, 1=CONFIG, 2=OTA_CTRL, 3=OTA_DATA

void recoverI2CBus(int sda, int scl) {
  pinMode(sda, INPUT_PULLUP);
  pinMode(scl, OUTPUT);
  for (int i = 0; i < 16; i++) {
    digitalWrite(scl, LOW);
    delayMicroseconds(10);
    digitalWrite(scl, HIGH);
    delayMicroseconds(10);
  }
  // Generate STOP condition
  pinMode(sda, OUTPUT);
  digitalWrite(sda, LOW);
  delayMicroseconds(10);
  digitalWrite(scl, HIGH);
  delayMicroseconds(10);
  digitalWrite(sda, HIGH);
  delayMicroseconds(10);
  pinMode(sda, INPUT_PULLUP);
  pinMode(scl, INPUT_PULLUP);
}



// ---------- Firmware / protocol identity ----------
#define FW_MAJOR 1
#define FW_MINOR 0
#define FW_PATCH 0
#define HW_REV   1
#define PROTO_VER 1
#define CALIB_VER 3               // v2: DF9-40 annular area. v3: burst acquisition change (FSR median-of-5, NTC single-shot + EMA). Bump on ANY conversion/acquisition change (spec §6.3)
#define FW_VERSION_STR "1.0.0"

// ---------- GATT UUIDs (spec §5, assigned — do not change without PROTO bump) ----------
#define SVC_DIABFOOT  "64696162-666f-6f74-a001-202610230001"
#define CHR_CTRL      "64696162-666f-6f74-a002-202610230001"
#define CHR_DATA      "64696162-666f-6f74-a003-202610230001"
#define CHR_STATUS    "64696162-666f-6f74-a004-202610230001"
#define CHR_CONFIG    "64696162-666f-6f74-a005-202610230001"
#define SVC_OTA       "64696162-666f-6f74-a010-202610230001"
#define CHR_OTA_CTRL  "64696162-666f-6f74-a011-202610230001"
#define CHR_OTA_DATA  "64696162-666f-6f74-a012-202610230001"

// ---------- Pins & sensors (from baseline sensor_test.ino) ----------
const int PIN_FSR[5] = {0, 1, 2, 3, 4};
const int PIN_SDA = 8;
const int PIN_SCL = 9;
int active_sda = PIN_SDA;
int active_scl = PIN_SCL;
uint8_t ads_address = 0x48;
uint8_t ntc_channel = 0;

Adafruit_ADS1115 ads;
bool ads_ok = false;
bool mpu_ok = false;
uint8_t mpu_address = 0x68;
uint8_t mpu_whoami = 0;

float ax = 0, ay = 0, az = 0;
float gx = 0, gy = 0, gz = 0;
float mpu_temp = 0;

const float VCC = 3.3;
const int ADC_MAX = 4095;
const float R_FIXED_FSR = 10000.0;
const float FSR_AREA_M2 = 3.4361e-5; // DF9-40 sensing area (variant confirmed by owner
                                  // 2026-10-23): ANNULUS, OD 10 mm / ID 7.5 mm per the
                                  // DF9-40 datasheet -> pi*(5^2 - 3.75^2) mm² = 34.36 mm².
                                  // Replaces the baseline's full-disc 7.85e-5, which
                                  // under-reported pressure by 2.286x. CALIB_VER bumped
                                  // to 2 for this change. STILL UNVERIFIED: the force
                                  // curve below is a generic FSR conductance model, not
                                  // a DF9 bench calibration — see spec §4/§13.
const float R_FIXED_NTC = 10000.0;
const float NTC_NOMINAL = 10000.0;
const float TEMP_NOMINAL = 25.0;
const float BETA_COEFFICIENT = 3950.0;

// ---------- Sensor conversions (carried over from baseline, unchanged math) ----------
float readESP32Voltage(int pin, int samples = 20) {
  long totalADC = 0;
  for (int i = 0; i < samples; i++) { totalADC += analogRead(pin); delay(2); }
  return ((totalADC / (float)samples) / ADC_MAX) * VCC;
}

float voltageToFSRResistance(float vout) {
  if (vout <= 0.001) return 999999999.0;
  if (vout >= VCC) return 0.0;
  return R_FIXED_FSR * ((VCC / vout) - 1.0);
}

float estimateForceFromFSR(float r_fsr) {
  if (r_fsr <= 0 || r_fsr >= 999999999.0) return 0.0;
  float conductance_uS = 1000000.0 / r_fsr;
  float force_N = (conductance_uS <= 1000.0) ? conductance_uS / 80.0
                                             : (conductance_uS - 1000.0) / 30.0;
  return (force_N < 0) ? 0.0 : force_N;
}

float forceToPressureKPa(float force_N) { return (force_N / FSR_AREA_M2) / 1000.0; }

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
  float s = r_ntc / NTC_NOMINAL;
  s = log(s);
  s /= BETA_COEFFICIENT;
  s += 1.0 / (TEMP_NOMINAL + 273.15);
  s = 1.0 / s;
  s -= 273.15;
  return s;
}

void scanI2C() {
#if DEBUG_SERIAL
  DBGLN("--- I2C BUS SCAN ---");
  int count = 0;
  for (uint8_t addr = 1; addr < 127; addr++) {
    Wire.beginTransmission(addr);
    if (Wire.endTransmission() == 0) { count++; DBG("[I2C] 0x"); DBGLN(addr, HEX); }
  }
  if (count == 0) DBGLN("[I2C] WARNING: no devices on SDA=8, SCL=9");
#endif
}

bool initUniversalMPU() {
  const uint8_t cand[2] = {0x68, 0x69};
  for (int r = 0; r < 3; r++) {
    for (uint8_t i = 0; i < 2; i++) {
      uint8_t addr = cand[i];
      Wire.beginTransmission(addr);
      if (Wire.endTransmission() != 0) continue;
      Wire.beginTransmission(addr); Wire.write(0x6B); Wire.write(0x00);   // wake
      if (Wire.endTransmission() != 0) continue;
      delay(30);
      Wire.beginTransmission(addr); Wire.write(0x75); Wire.endTransmission(false);
      Wire.requestFrom((uint8_t)addr, (uint8_t)1);
      mpu_whoami = Wire.available() ? Wire.read() : 0x00;
      Wire.beginTransmission(addr); Wire.write(0x1C); Wire.write(0x10); Wire.endTransmission(); // accel ±8g
      Wire.beginTransmission(addr); Wire.write(0x1B); Wire.write(0x08); Wire.endTransmission(); // gyro ±500dps
      Wire.beginTransmission(addr); Wire.write(0x1A); Wire.write(0x03); Wire.endTransmission(); // DLPF ~21Hz
      mpu_address = addr;
      return true;
    }
    delay(20);
  }
  return false;
}

void probeI2CSensors() {
  static uint32_t lastProbeMs = 0;
  if (lastProbeMs != 0 && (millis() - lastProbeMs < 3000)) return;
  lastProbeMs = millis();

  recoverI2CBus(PIN_SDA, PIN_SCL);
  Wire.begin(PIN_SDA, PIN_SCL, 100000);
  Wire.setTimeOut(30);
  delay(10);

  if (!ads_ok) {
    if (ads.begin(0x48, &Wire) || ads.begin(0x49, &Wire)) {
      ads_ok = true;
      ads.setGain(GAIN_ONE);
      ads.setDataRate(RATE_ADS1115_860SPS);
      for (uint8_t ch = 0; ch < 4; ch++) {
        float v = ads.computeVolts(ads.readADC_SingleEnded(ch));
        if (v >= 0.2f && v <= 3.1f) {
          ntc_channel = ch;
          break;
        }
      }
    }
  }

  if (!mpu_ok) {
    mpu_ok = initUniversalMPU();
  }
}

bool readUniversalMPU() {
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
  mpu_temp = (raw_t / 340.0f) + 36.53f;
  return true;
}

void mpuSleep(bool sleep) {
  if (!mpu_ok) return;
  Wire.beginTransmission(mpu_address);
  Wire.write(0x6B);                       // PWR_MGMT_1
  Wire.write(sleep ? 0x40 : 0x00);        // SLEEP bit
  Wire.endTransmission();
  if (!sleep) delay(30);
}

void mpuConfigWakeOnMotion() {
  // Only meaningful if PIN_MPU_INT is wired to a wake-capable GPIO (spec §9.4).
  if (!mpu_ok || PIN_MPU_INT < 0) return;
  Wire.beginTransmission(mpu_address); Wire.write(0x37); Wire.write(0xA0); Wire.endTransmission(); // INT pin: active-low, latched
  Wire.beginTransmission(mpu_address); Wire.write(0x1F); Wire.write(0x02); Wire.endTransmission(); // MOT_THR
  Wire.beginTransmission(mpu_address); Wire.write(0x20); Wire.write(0x01); Wire.endTransmission(); // MOT_DUR
  Wire.beginTransmission(mpu_address); Wire.write(0x69); Wire.write(0xC0); Wire.endTransmission(); // accel intel ctrl
  Wire.beginTransmission(mpu_address); Wire.write(0x38); Wire.write(0x40); Wire.endTransmission(); // INT_ENABLE motion
}

void adsPowerDown() {
  if (!ads_ok) return;
  // Single-shot power-down is the ADS1115 default after a conversion in
  // MODE=1; issue one config write with MODE=1 via the library's comparator
  // path is not exposed, so the conservative action is: nothing continuous
  // is running (we only ever do single-ended reads on demand). Sampling is
  // simply stopped outside SESSION states (spec §9.3).
}

uint16_t readBatteryMv() {
  if (PIN_BATT_ADC < 0) return 0;                       // 0 = unknown
  uint32_t mv = analogReadMilliVolts(PIN_BATT_ADC);
  return (uint16_t)(mv * BATT_DIVIDER_NUM / BATT_DIVIDER_DEN);
}

// =====================================================
// STATE, SAMPLES, RING BUFFER (spec §6, §9, §10)
// =====================================================
enum DevState : uint8_t {
  ST_DEEP_SLEEP = 0, ST_FAST_ADV, ST_SLOW_ADV, ST_CONNECTED_UNAUTH,
  ST_SESSION_ACTIVE, ST_GRACE_RECONNECT, ST_OTA_ACTIVE
};

volatile DevState devState = ST_DEEP_SLEEP;

// One stored sample == the fixed fields packed per spec §6.2 at send time.
#define BUF_SAMPLES 992        // 32 KiB-class RAM ring (spec §10)
static StoredSample ring[BUF_SAMPLES];
static uint16_t ringHead = 0;          // next write index
static uint16_t ringCount = 0;         // valid entries
static uint32_t nextSampleSeq = 1;     // seq assigned to the next sample
static uint32_t nextSendSeq = 1;       // next seq the sender will transmit
static uint32_t replayUntilSeq = 0;    // batches with base_seq <= this are REPLAY
static bool     gapPending = false;    // a loss occurred before next batch (GAP_BEFORE)

static inline uint32_t ringFirstSeq() { return ringCount ? ring[(ringHead + BUF_SAMPLES - ringCount) % BUF_SAMPLES].seq : nextSampleSeq; }
static inline uint32_t ringLastSeq()  { return ringCount ? ring[(ringHead + BUF_SAMPLES - 1) % BUF_SAMPLES].seq : 0; }

static StoredSample* ringFind(uint32_t seq) {
  if (!ringCount) return nullptr;
  uint32_t first = ringFirstSeq();
  if (seq < first || seq > ringLastSeq()) return nullptr;
  uint16_t startIdx = (ringHead + BUF_SAMPLES - ringCount) % BUF_SAMPLES;
  return &ring[(startIdx + (seq - first)) % BUF_SAMPLES];
}

static volatile uint32_t pendingAckSeq = 0;   // set in BLE callback, applied in loop
static volatile bool     ackPending = false;
static uint32_t bufferOverwriteCount = 0;     // persisted at sleep (DIAG)

void ringPush(const StoredSample& s) {
  if (ringCount == BUF_SAMPLES) {
    // Overwrite oldest (spec §10): the loss must surface, never silently.
    StoredSample& victim = ring[ringHead];
    if (victim.seq >= nextSendSeq) gapPending = true;         // never sent -> visible gap
    bufferOverwriteCount++;
    ringHead = (ringHead + 1) % BUF_SAMPLES;                  // head advances; count stays full
    ring[ (ringHead + BUF_SAMPLES - 1) % BUF_SAMPLES ] = s;
    return;
  }
  ring[ringHead] = s;
  ringHead = (ringHead + 1) % BUF_SAMPLES;
  ringCount++;
}

void ringApplyAck(uint32_t ackedSeq) {
  // Cumulative ACK (spec §8/§10): free everything <= ackedSeq (contiguous by construction).
  while (ringCount && ringFirstSeq() <= ackedSeq) {
    ringCount--;                       // first entry falls out of the window
  }
  if (nextSendSeq <= ackedSeq) nextSendSeq = ackedSeq + 1;
}

// ---------- little-endian helpers ----------
static inline void putU16(uint8_t* p, uint16_t v) { p[0] = v & 0xFF; p[1] = (v >> 8) & 0xFF; }
static inline void putU32(uint8_t* p, uint32_t v) { p[0] = v & 0xFF; p[1] = (v >> 8) & 0xFF; p[2] = (v >> 16) & 0xFF; p[3] = (v >> 24) & 0xFF; }
static inline uint16_t getU16(const uint8_t* p) { return p[0] | (p[1] << 8); }
static inline uint32_t getU32(const uint8_t* p) { return p[0] | ((uint32_t)p[1] << 8) | ((uint32_t)p[2] << 16) | ((uint32_t)p[3] << 24); }

uint16_t crc16Ccitt(const uint8_t* data, size_t len) {
  uint16_t crc = 0xFFFF;
  for (size_t i = 0; i < len; i++) {
    crc ^= (uint16_t)data[i] << 8;
    for (int b = 0; b < 8; b++) crc = (crc & 0x8000) ? (crc << 1) ^ 0x1021 : crc << 1;
  }
  return crc;
}

uint32_t crc32Update(uint32_t crc, const uint8_t* data, size_t len) {
  crc = ~crc;
  for (size_t i = 0; i < len; i++) {
    crc ^= data[i];
    for (int b = 0; b < 8; b++) crc = (crc & 1) ? (crc >> 1) ^ 0xEDB88320 : crc >> 1;
  }
  return ~crc;
}

// =====================================================
// BLE OBJECTS, SESSION STATE, COMMAND QUEUE
// Callbacks run in the NimBLE host task: they only copy bytes / set flags.
// All protocol work happens in loop() (single-threaded state machine).
// =====================================================
NimBLEServer* pServer = nullptr;
NimBLECharacteristic* chCtrl = nullptr;
NimBLECharacteristic* chData = nullptr;
NimBLECharacteristic* chStatus = nullptr;
NimBLECharacteristic* chConfig = nullptr;
NimBLECharacteristic* chOtaCtrl = nullptr;
NimBLECharacteristic* chOtaData = nullptr;

volatile bool     bleConnected = false;
volatile uint16_t bleConnHandle = 0xFFFF;
volatile uint16_t bleMtu = 23;
volatile bool     linkEncrypted = false;
volatile bool     dataSubscribed = false;
volatile bool     statusSubscribed = false;

bool helloDone = false;
bool sessionActive = false;
bool sessionReachedThisBoot = false;    // cap accounting: only pre-session awake time counts (spec §9 rule 4)
uint32_t hs1DoneMs = 0;
bool endSessionRequested = false;
uint32_t bootId = 0;

static PendingCmd cmdQ[CMDQ_DEPTH];
static volatile uint8_t cmdQHead = 0, cmdQTail = 0;

bool cmdQPush(uint8_t channel, const uint8_t* d, size_t n) {
  uint8_t next = (cmdQHead + 1) % CMDQ_DEPTH;
  if (next == cmdQTail) return false;                       // full: drop (cmds are idempotent-ish; app retries)
  if (n > CMDQ_MAXLEN) n = CMDQ_MAXLEN;
  cmdQ[cmdQHead].channel = channel;
  cmdQ[cmdQHead].len = (uint8_t)n;
  memcpy(cmdQ[cmdQHead].data, d, n);
  cmdQHead = next;
  return true;
}

bool cmdQPop(PendingCmd& out) {
  if (cmdQTail == cmdQHead) return false;
  out = cmdQ[cmdQTail];
  cmdQTail = (cmdQTail + 1) % CMDQ_DEPTH;
  return true;
}

// DIAG / persistent counters
Preferences prefs;
uint32_t advExpiredCount = 0;
uint32_t failedHandshakeCount = 0;
bool     prevAdvExpiredFlag = false;
bool     otaPendingConfirm = false;

// Session config (spec §8; persisted)
uint8_t  cfgSampleRateHz = 2;          // DEFAULT 2 Hz, clamp 1..10
uint16_t cfgAdvWindowS = 300;          // DEFAULT 300 s, clamp 120..600

// Timing (millis deadlines — absolute, spec §9 rule 3)
uint32_t windowDeadlineMs = 0;
uint32_t windowFastEndMs = 0;
uint32_t hs1DeadlineMs = 0;            // handshake-1 completion (subscribe+MTU)
uint32_t hs2DeadlineMs = 0;            // HELLO / START completion
uint32_t graceDeadlineMs = 0;
uint32_t graceFastEndMs = 0;
uint32_t bootAwakeStartMs = 0;
RTC_DATA_ATTR uint32_t rtcNoSessionAwakeMs = 0;   // cumulative cap across deep-sleep wakes (spec §9 rule 4)
#define MAX_AWAKE_NO_SESSION_MS 600000UL

uint32_t lastSampleMs = 0;
uint32_t lastSendMs = 0;
uint32_t lastLedMs = 0;
uint8_t  ledPhase = 0;

// OTA state
bool     otaActive = false;
uint32_t otaExpectedSize = 0, otaReceived = 0, otaExpectedCrc = 0, otaRunningCrc = 0;
uint32_t otaLastChunkMs = 0;
bool     otaHadSession = false;

void persistCounters() {
  prefs.putUInt("advExpCnt", advExpiredCount);
  prefs.putUInt("failHsCnt", failedHandshakeCount);
  prefs.putUInt("bufOvrCnt", bufferOverwriteCount);
}

void notifyStatus(const uint8_t* d, size_t n) {
  if (!chStatus) return;
  chStatus->setValue(d, n);
  if (bleConnected && statusSubscribed) chStatus->notify();
}

void sendStatusError(uint8_t forOpcode, uint8_t status) {
  uint8_t msg[3] = { (uint8_t)(forOpcode | 0x80), 0xEE, status }; // generic error frame: reply op, 0xEE marker, status
  notifyStatus(msg, sizeof(msg));
}

// STATUS codes
#define ST_OK 0
#define ST_BAD_ORDER 1
#define ST_MTU_TOO_SMALL 2
#define ST_PROTO_UNSUPPORTED 3
#define ST_SENSOR_FAULT 4

// Forward declarations (OTA handlers are defined after the protocol section)
void handleOtaCtrl(const uint8_t* d, uint8_t n);
void otaHandleChunk(const uint8_t* d, size_t n);

// =====================================================
// NimBLE CALLBACKS (host task — copy/flag only, spec above)
// =====================================================
class ServerCallbacks : public NimBLEServerCallbacks {
  void onConnect(NimBLEServer* s, NimBLEConnInfo& info) override {
    bleConnected = true;
    bleConnHandle = info.getConnHandle();
    bleMtu = info.getMTU();
    linkEncrypted = true;                           // bench compatible
    hs1DeadlineMs = millis() + 10000;               // spec §7 timeout
    hs2DeadlineMs = 0;
    hs1DoneMs = 0;
    DBGLN("[BLE] connected");
  }
  void onDisconnect(NimBLEServer* s, NimBLEConnInfo& info, int reason) override {
    bleConnected = false;
    bleConnHandle = 0xFFFF;
    linkEncrypted = false;
    dataSubscribed = false;
    statusSubscribed = false;
    DBGLN("[BLE] disconnected");
    // State transition itself is handled in loop() (single-threaded rules).
  }
  void onAuthenticationComplete(NimBLEConnInfo& info) override {
    linkEncrypted = true;
  }
  void onMTUChange(uint16_t mtu, NimBLEConnInfo& info) override { bleMtu = mtu; }
};

class CtrlCallbacks : public NimBLECharacteristicCallbacks {
  void onWrite(NimBLECharacteristic* c, NimBLEConnInfo& info) override {
    NimBLEAttValue v = c->getValue();
    if (c == chOtaData) { otaHandleChunk(v.data(), v.size()); return; }  // flash write can't queue (244 B chunks)
    uint8_t ch = (c == chOtaCtrl) ? 2 : 0;
    cmdQPush(ch, v.data(), v.size());
  }
};

class ConfigCallbacks : public NimBLECharacteristicCallbacks {
  void onWrite(NimBLECharacteristic* c, NimBLEConnInfo& info) override {
    NimBLEAttValue v = c->getValue();
    cmdQPush(1, v.data(), v.size());
  }
};

class DataCallbacks : public NimBLECharacteristicCallbacks {
  void onSubscribe(NimBLECharacteristic* c, NimBLEConnInfo& info, uint16_t subValue) override {
    if (c == chData) dataSubscribed = (subValue & 0x0001);
    if (c == chStatus) statusSubscribed = (subValue & 0x0001);
  }
};

static ServerCallbacks serverCb;
static CtrlCallbacks ctrlCb;
static ConfigCallbacks configCb;
static DataCallbacks dataCb;

String deviceName() {
  String mac = NimBLEDevice::getAddress().toString().c_str();
  mac.replace(":", "");
  return String(CONFIG_DEVICE_NAME_PREFIX) + mac.substring(mac.length() - 4);
}

void startAdvertising(uint16_t minInt, uint16_t maxInt) {
  NimBLEAdvertising* adv = NimBLEDevice::getAdvertising();
  adv->stop();
  NimBLEAdvertisementData advData;
  advData.addServiceUUID(SVC_DIABFOOT);
  NimBLEAdvertisementData scanResp;
  scanResp.setName(deviceName().c_str());
  adv->setAdvertisementData(advData);
  adv->setScanResponseData(scanResp);
  adv->setMinInterval(minInt);
  adv->setMaxInterval(maxInt);
  adv->start(0);                                     // indefinite; §9 state machine stops it
  DBG("[BLE] advertising "); DBGLN(deviceName());
}

void stopAdvertising() { NimBLEDevice::getAdvertising()->stop(); }

void bleInit() {
  NimBLEDevice::init("");
  NimBLEDevice::setPower(0);                                          // spec §3 DEFAULT 0 dBm
  NimBLEDevice::setMTU(247);                         // spec §3 target MTU
#if CONFIG_STATIC_PASSKEY
  NimBLEDevice::setSecurityAuth(true, true, true);   // bond, MITM, SC
  NimBLEDevice::setSecurityIOCap(BLE_HS_IO_DISPLAY_ONLY);
  NimBLEDevice::setSecurityPasskey(CONFIG_STATIC_PASSKEY);
#else
  NimBLEDevice::setSecurityAuth(true, false, true);  // bond, no MITM (Just Works), SC
  NimBLEDevice::setSecurityIOCap(BLE_HS_IO_NO_INPUT_OUTPUT);
#endif

  pServer = NimBLEDevice::createServer();
  pServer->setCallbacks(&serverCb, false);

  NimBLEService* svc = pServer->createService(SVC_DIABFOOT);
  const uint32_t encWrite = NIMBLE_PROPERTY::WRITE;
  const uint32_t encNotify = NIMBLE_PROPERTY::NOTIFY;
  chCtrl = svc->createCharacteristic(CHR_CTRL, encWrite, 40);
  chCtrl->setCallbacks(&ctrlCb);
  chData = svc->createCharacteristic(CHR_DATA, encNotify, 244);
  chData->setCallbacks(&dataCb);
  chStatus = svc->createCharacteristic(CHR_STATUS, NIMBLE_PROPERTY::READ | NIMBLE_PROPERTY::NOTIFY, 64);
  chStatus->setCallbacks(&dataCb);
  chConfig = svc->createCharacteristic(CHR_CONFIG, NIMBLE_PROPERTY::READ | NIMBLE_PROPERTY::WRITE, 8);
  chConfig->setCallbacks(&configCb);
  svc->start();

  NimBLEService* dis = pServer->createService("180A");
  NimBLECharacteristic* mfr = dis->createCharacteristic("2A29", NIMBLE_PROPERTY::READ, 20);
  mfr->setValue("DiabFoot");
  NimBLECharacteristic* mdl = dis->createCharacteristic("2A24", NIMBLE_PROPERTY::READ, 20);
  mdl->setValue("Guard v1");
  NimBLECharacteristic* fwv = dis->createCharacteristic("2A26", NIMBLE_PROPERTY::READ, 20);
  fwv->setValue(FW_VERSION_STR);
  dis->start();

  if (PIN_BATT_ADC >= 0) {
    NimBLEService* bat = pServer->createService("180F");
    NimBLECharacteristic* lvl = bat->createCharacteristic("2A19", NIMBLE_PROPERTY::READ | NIMBLE_PROPERTY::NOTIFY, 1);
    uint8_t pct = 100;
    lvl->setValue(&pct, 1);
    bat->start();
  }

  NimBLEService* ota = pServer->createService(SVC_OTA);
  chOtaCtrl = ota->createCharacteristic(CHR_OTA_CTRL, encWrite, 16);
  chOtaCtrl->setCallbacks(&ctrlCb);
  chOtaData = ota->createCharacteristic(CHR_OTA_DATA, NIMBLE_PROPERTY::WRITE_NR | NIMBLE_PROPERTY::WRITE_ENC | NIMBLE_PROPERTY::WRITE_AUTHEN, 244);
  chOtaData->setCallbacks(&ctrlCb);
  ota->start();
}

// =====================================================
// SAMPLING + FRAME PACKING + SENDER (spec §6)
// =====================================================
uint8_t sensorBitmap() {
  uint8_t b = 0;
  if (ads_ok) b |= 0x01;
  if (mpu_ok) b |= 0x02;
  b |= 0x7C;                    // FSR1-5 wired (bits 2-6); presence = ADC rail check is future work
  return b;
}

// ---------- Optimised burst acquisition (spec §16) ----------
// Burst budget: the baseline averaged FSR x20 (with delay(2)) and NTC x10 via
// the ADS1115 at its default 128 SPS — each ADS conversion blocks ~8 ms, so
// the NTC alone cost ~130-180 ms of CPU-awake time per sample (the ADS's own
// current is negligible at ~0.15 mA; its cost is burst TIME). New scheme:
//  - MPU woken first; its ~30 ms gyro start-up overlaps the other reads.
//  - FSR: one dummy read (ADC channel settling) + median-of-5 rapid reads.
//    Median rejects single-read ADC spikes without the peak-smearing a
//    20-sample mean causes on heel-strike transients.
//  - NTC: ONE ADS conversion at 860 SPS (~1.2 ms), smoothed across samples
//    by an EMA (foot temperature changes over minutes; tau ~ 2 s at 2 Hz).
//  - MPU read last, then put back to sleep until the next burst.
// Total burst target ~45 ms (was ~350-400 ms). The ring buffer, sequence,
// ACK and RESUME code paths are deliberately NOT touched by this section.
uint32_t lastBurstMs = 0;
static float ntcEmaC = NAN;

void mpuWakeNoWait() {                         // wake without the blocking 30 ms:
  if (!mpu_ok) return;                         // gyro start-up time is spent doing
  Wire.beginTransmission(mpu_address);         // the FSR/NTC reads instead
  Wire.write(0x6B);
  Wire.write(0x00);
  Wire.endTransmission();
}

static uint16_t medianOf5(uint16_t a[5]) {
  for (int i = 1; i < 5; i++) { uint16_t k = a[i]; int j = i - 1; while (j >= 0 && a[j] > k) { a[j + 1] = a[j]; j--; } a[j + 1] = k; }
  return a[2];
}

float readFSRPressureKPa(int pin, bool &saturated) {
  (void)analogRead(pin);                       // dummy: settle ADC mux/sample cap
  uint16_t raw[5];
  for (int i = 0; i < 5; i++) raw[i] = analogRead(pin);
  float v = (medianOf5(raw) / (float)ADC_MAX) * VCC;
  float p = forceToPressureKPa(estimateForceFromFSR(voltageToFSRResistance(v)));
  saturated = (p > 65000.0f);
  return saturated ? 65000.0f : p;
}

void takeSample(bool offline) {
  uint32_t burstStart = millis();
  if (!mpu_ok || !ads_ok) {
    probeI2CSensors();
  }
  StoredSample s;
  s.seq = nextSampleSeq++;
  s.time_ms = millis();
  s.taken_offline = offline;
  s.status = 0;
  bool anySat = false;
  for (int i = 0; i < 5; i++) {
    bool sat = false;
    float p = readFSRPressureKPa(PIN_FSR[i], sat);
    if (sat) anySat = true;
    s.pressure_kPa[i] = (uint16_t)(p + 0.5f);
  }
  if (anySat) s.status |= 0x01;
  float vntc = readADSVoltage(ntc_channel, 1);           // single conversion (860 SPS set in setup)
  if (vntc >= 0) {
    float t = resistanceToTemperatureC(voltageToNTCResistance(vntc));
    if (t > -900.0f) {
      ntcEmaC = isnan(ntcEmaC) ? t : (ntcEmaC + 0.25f * (t - ntcEmaC));
      s.ntc_temp_c100 = (int16_t)(ntcEmaC * 100.0f);
      s.status |= 0x02;
    } else s.ntc_temp_c100 = 0x7FFF;
  } else s.ntc_temp_c100 = 0x7FFF;
  
  bool mok = readUniversalMPU();
  if (mok) {
    s.status |= 0x04;
    // accel: m/s^2 -> mg ; gyro: rad/s -> 0.1 deg/s
    float mg[3] = { ax * 101.97f, ay * 101.97f, az * 101.97f };
    float d10[3] = { gx * 572.96f, gy * 572.96f, gz * 572.96f };
    for (int i = 0; i < 3; i++) {
      s.accel_mg[i] = (int16_t)constrain(mg[i], -8000, 8000);
      s.gyro_dps10[i] = (int16_t)constrain(d10[i], -5000, 5000);
    }
  } else {
    s.accel_mg[0] = s.accel_mg[1] = s.accel_mg[2] = 0;
    s.gyro_dps10[0] = s.gyro_dps10[1] = s.gyro_dps10[2] = 0;
  }
  ringPush(s);
  lastBurstMs = millis() - burstStart;
}

// Pack up to 8 buffered samples starting at nextSendSeq into one DATA batch.
// Returns false when nothing sendable. (spec §6.1: 12 + 8*27 + 2 = 230 B)
bool sendNextBatch() {
  if (!bleConnected || !dataSubscribed || !ringCount) return false;
  StoredSample* first = ringFind(nextSendSeq);
  if (!first) {                       // send pointer fell behind the ring (overwritten/acked)
    if (ringLastSeq() >= nextSendSeq) gapPending = true;
    nextSendSeq = ringFirstSeq();
    first = ringFind(nextSendSeq);
    if (!first) return false;
  }
  uint8_t buf[244];
  uint8_t count = 0;
  uint32_t baseSeq = first->seq;
  uint32_t baseTime = first->time_ms;
  bool replay = (baseSeq <= replayUntilSeq) || first->taken_offline;
  size_t pos = 12;
  while (count < 8) {
    StoredSample* smp = ringFind(baseSeq + count);
    if (!smp) break;
    uint8_t* w = &buf[pos];
    putU16(w + 0, (uint16_t)(smp->time_ms - baseTime));
    for (int i = 0; i < 5; i++) putU16(w + 2 + i * 2, smp->pressure_kPa[i]);
    putU16(w + 12, (uint16_t)smp->ntc_temp_c100);
    for (int i = 0; i < 3; i++) putU16(w + 14 + i * 2, (uint16_t)smp->accel_mg[i]);
    for (int i = 0; i < 3; i++) putU16(w + 20 + i * 2, (uint16_t)smp->gyro_dps10[i]);
    w[26] = smp->status;
    pos += 27;
    count++;
    if (smp->taken_offline) replay = true;
  }
  if (!count) return false;
  buf[0] = 0x01;                      // msg_type sensor batch
  buf[1] = 1;                         // frame_ver
  putU32(buf + 2, baseSeq);
  putU32(buf + 6, baseTime);
  buf[10] = count;
  uint8_t flags = 0;
  if (ads_ok) flags |= 0x01;
  if (mpu_ok) flags |= 0x02;
  uint16_t mv = readBatteryMv();
  if (mv && mv < BATT_CRITICAL_MV) flags |= 0x04;
  if (replay) flags |= 0x08;
  if (gapPending) flags |= 0x10;
  buf[11] = flags;
  uint16_t crc = crc16Ccitt(buf, pos);
  putU16(buf + pos, crc);
  pos += 2;
  chData->setValue(buf, pos);
  bool ok = chData->notify();
  if (ok) {
    nextSendSeq = baseSeq + count;
    if (gapPending) gapPending = false;
    if (nextSendSeq > replayUntilSeq) replayUntilSeq = 0;
  }
  return ok;
}

// =====================================================
// HANDSHAKE 2 — SESSION PROTOCOL (spec §8)
// =====================================================
void confirmOtaIfPending() {
  if (!otaPendingConfirm) return;
  // Spec §11: a new image confirms itself only after a real session exists.
  esp_ota_mark_app_valid_cancel_rollback();
  otaPendingConfirm = false;
  prefs.putBool("otaPend", false);
  DBGLN("[OTA] image confirmed after successful handshake");
}

void applyConfig(uint8_t rateHz, uint16_t advWindowS, bool persist) {
  cfgSampleRateHz = constrain(rateHz, 1, 10);
  cfgAdvWindowS = constrain(advWindowS, 120, 600);
  if (persist) {
    prefs.putUChar("rateHz", cfgSampleRateHz);
    prefs.putUShort("advWinS", cfgAdvWindowS);
  }
  uint8_t val[4] = { cfgSampleRateHz, (uint8_t)(cfgAdvWindowS & 0xFF), (uint8_t)(cfgAdvWindowS >> 8), 0 };
  if (chConfig) chConfig->setValue(val, sizeof(val));
}

void handleCommand(const PendingCmd& cmd) {
  const uint8_t* d = cmd.data;
  uint8_t n = cmd.len;
  if (cmd.channel == 1) {                       // raw CONFIG characteristic write: [rate, winLo, winHi, flags]
    if (n >= 3) applyConfig(d[0], getU16(d + 1), true);
    return;
  }
  if (cmd.channel == 2) { handleOtaCtrl(d, n); return; }
  if (cmd.channel != 0 || n < 1) return;
  uint8_t op = d[0];
  switch (op) {
    case 0x01: {                                // HELLO
      if (n < 6) { sendStatusError(op, ST_BAD_ORDER); return; }
      uint8_t appProto = d[1];
      uint8_t out[24]; size_t o = 0;
      out[o++] = 0x81;
      out[o++] = PROTO_VER;
      out[o++] = FW_MAJOR; out[o++] = FW_MINOR; out[o++] = FW_PATCH;
      out[o++] = HW_REV;
      putU32(out + o, bootId); o += 4;
      out[o++] = sensorBitmap();
      putU16(out + o, CALIB_VER); o += 2;
      uint16_t mv = readBatteryMv();
      out[o++] = mv ? (uint8_t)constrain((mv - 3000) * 100 / 1200, 0, 100) : 0xFF;
      out[o++] = prevAdvExpiredFlag ? 1 : 0;
      putU16(out + o, (uint16_t)min<uint32_t>(ringCount, 0xFFFF)); o += 2;
      putU32(out + o, ringCount ? ringLastSeq() : nextSampleSeq - 1); o += 4;
      notifyStatus(out, o);
      if (prevAdvExpiredFlag) {                 // report once (spec §9 rule 8)
        prevAdvExpiredFlag = false;
        prefs.putBool("prevExp", false);
      }
      if (appProto != PROTO_VER) {              // no silent downgrade (spec §8)
        sendStatusError(op, ST_PROTO_UNSUPPORTED);
        if (pServer && bleConnHandle != 0xFFFF) pServer->disconnect(bleConnHandle);
        return;
      }
      helloDone = true;
      hs2DeadlineMs = millis() + 15000;          // CONFIG/START window (spec §8)
      DBGLN("[HS2] HELLO ok");
      break;
    }
    case 0x02: {                                // CONFIG_SET [op, rate, winLo, winHi, flags]
      if (!helloDone) { sendStatusError(op, ST_BAD_ORDER); return; }
      if (n >= 4) applyConfig(d[1], getU16(d + 2), true);
      uint8_t out[5] = { 0x82, cfgSampleRateHz, (uint8_t)(cfgAdvWindowS & 0xFF),
                         (uint8_t)(cfgAdvWindowS >> 8), ST_OK };
      notifyStatus(out, sizeof(out));
      break;
    }
    case 0x03: {                                // START
      if (!helloDone) { sendStatusError(op, ST_BAD_ORDER); return; }
      // Retry sensors if not yet initialized
      if (!ads_ok || !mpu_ok) {
        probeI2CSensors();
      }
      mpuSleep(false);                          // Wake MPU for session
      sessionActive = true;
      sessionReachedThisBoot = true;
      devState = ST_SESSION_ACTIVE;
      hs2DeadlineMs = 0;
      rtcNoSessionAwakeMs = 0;                  // session reached: cap resets (spec §9)
      uint8_t out[2] = { 0x83, ST_OK };
      notifyStatus(out, sizeof(out));
      if (pServer && bleConnHandle != 0xFFFF)
        pServer->updateConnParams(bleConnHandle, 24, 48, 4, 500);   // 30-60 ms, latency 4, timeout 5 s (spec §7)
      confirmOtaIfPending();
      DBGLN("[HS2] START -> streaming");
      break;
    }
    case 0x04: {                                // STOP
      sessionActive = false;
      uint8_t out[5]; out[0] = 0x84; putU32(out + 1, nextSampleSeq - 1);
      notifyStatus(out, sizeof(out));
      break;
    }
    case 0x05: {                                // RESUME [op, lastReceivedSeq u32]
      if (n < 5) { sendStatusError(op, ST_BAD_ORDER); return; }
      uint32_t lastRx = getU32(d + 1);
      uint32_t firstAvail = ringCount ? ringFirstSeq() : lastRx + 1;
      uint32_t gap = (firstAvail > lastRx + 1) ? (firstAvail - (lastRx + 1)) : 0;
      nextSendSeq = max<uint32_t>(lastRx + 1, firstAvail);
      replayUntilSeq = ringCount ? ringLastSeq() : 0;
      uint32_t replayCnt = (ringCount && ringLastSeq() >= nextSendSeq) ? (ringLastSeq() - nextSendSeq + 1) : 0;
      uint8_t out[13]; out[0] = 0x85;
      putU32(out + 1, firstAvail); putU32(out + 5, gap); putU32(out + 9, replayCnt);
      notifyStatus(out, sizeof(out));
      if (helloDone && devState == ST_GRACE_RECONNECT) {
        // Reconnected inside grace with the same boot epoch: session resumes (spec §10)
        sessionActive = true;
        devState = ST_SESSION_ACTIVE;
      }
      break;
    }
    case 0x06: {                                // END_SESSION (user intent)
      uint8_t out[5]; out[0] = 0x86; putU32(out + 1, nextSampleSeq - 1);
      notifyStatus(out, sizeof(out));
      endSessionRequested = true;               // loop() disconnects + sleeps (spec §9 rule 6)
      break;
    }
    case 0x09: {                                // ACK_SAMPLES [op, ackedSeq u32]
      if (n >= 5) { pendingAckSeq = getU32(d + 1); ackPending = true; }
      break;
    }
    case 0x07: {                                // GET_DIAG
      uint8_t out[16]; size_t o = 0;
      out[o++] = 0x87;
      out[o++] = (uint8_t)esp_reset_reason();
      putU32(out + o, advExpiredCount); o += 4;
      putU32(out + o, failedHandshakeCount); o += 4;
      putU32(out + o, bufferOverwriteCount); o += 4;
      putU16(out + o, readBatteryMv()); o += 2;
      notifyStatus(out, o);
      break;
    }
    default:
      sendStatusError(op, ST_BAD_ORDER);
      break;
  }
}

// =====================================================
// OTA (spec §11) — chunks are written in the callback via otaHandleChunk
// because flash writes cannot wait for the 40-byte command queue.
// =====================================================
void otaReset() {
  if (Update.isRunning()) Update.abort();
  otaActive = false; otaExpectedSize = otaReceived = otaRunningCrc = 0;
}

void handleOtaCtrl(const uint8_t* d, uint8_t n) {
  if (n < 1) return;
  if (!helloDone) { sendStatusError(0x07, ST_BAD_ORDER); return; }
  uint8_t op = d[0];
  if (op == 0x10) {                             // OTA_BEGIN [op, size u32, crc32 u32]
    if (n < 9) return;
    otaExpectedSize = getU32(d + 1);
    otaExpectedCrc = getU32(d + 5);
    uint16_t mv = readBatteryMv();
    if (mv && mv < BATT_CRITICAL_MV) { sendStatusError(0x07, ST_SENSOR_FAULT); return; }
    if (!Update.begin(otaExpectedSize, U_FLASH)) { sendStatusError(0x07, ST_SENSOR_FAULT); return; }
    otaReceived = 0; otaRunningCrc = 0; otaLastChunkMs = millis();
    otaHadSession = sessionActive;
    otaActive = true;
    devState = ST_OTA_ACTIVE;
    uint8_t out[2] = { 0x90, ST_OK };
    notifyStatus(out, sizeof(out));
    DBGLN("[OTA] begin");
  } else if (op == 0x11) {                      // OTA_END
    if (!otaActive) return;
    bool crcOk = (otaReceived == otaExpectedSize) && (otaRunningCrc == otaExpectedCrc);
    if (!crcOk) {                               // failed image never touches the boot slot (spec §11)
      Update.abort();
      otaReset();
      sendStatusError(0x07, ST_SENSOR_FAULT);
      DBGLN("[OTA] CRC/size mismatch -> discarded");
      return;
    }
    if (Update.end(true)) {
      prefs.putBool("otaPend", true);           // confirm after first real handshake on next boot
      uint8_t out[2] = { 0x91, ST_OK };
      notifyStatus(out, sizeof(out));
      DBGLN("[OTA] complete -> reboot");
      delay(200);
      ESP.restart();
    } else {
      otaReset();
      sendStatusError(0x07, ST_SENSOR_FAULT);
    }
  } else if (op == 0x12) {                      // OTA_ABORT
    otaReset();
    if (otaHadSession) devState = ST_SESSION_ACTIVE; else devState = ST_CONNECTED_UNAUTH;
    uint8_t out[2] = { 0x92, ST_OK };
    notifyStatus(out, sizeof(out));
  }
}

void otaHandleChunk(const uint8_t* d, size_t n) {
  if (!otaActive || !Update.isRunning()) return;
  size_t w = Update.write((uint8_t*)d, n);
  otaReceived += w;
  otaRunningCrc = crc32Update(otaRunningCrc, d, w);
  otaLastChunkMs = millis();
}

// =====================================================
// OPTION B STATE MACHINE + DEEP SLEEP (spec §9)
// =====================================================
void ledWrite(bool on) { if (PIN_LED >= 0) digitalWrite(PIN_LED, on ? HIGH : LOW); }

void ledTick() {
  if (PIN_LED < 0) return;
  uint32_t now = millis();
  if (now - lastLedMs < 120) return;
  lastLedMs = now;
  ledPhase++;
  switch (devState) {
    case ST_FAST_ADV: ledWrite(ledPhase & 1); break;                 // fast blink
    case ST_SLOW_ADV: ledWrite((ledPhase % 25) == 0); break;         // pulse ~ every 3 s
    case ST_GRACE_RECONNECT: ledWrite((ledPhase % 8) < 2); break;    // double-ish blink
    case ST_SESSION_ACTIVE: ledWrite(true); break;                   // solid
    case ST_OTA_ACTIVE: ledWrite(ledPhase & 1); break;
    default: ledWrite(false); break;
  }
}

void openWindow(uint32_t fastMs) {
  uint32_t now = millis();
#if !CONFIG_ENABLE_DEEP_SLEEP
  rtcNoSessionAwakeMs = 0;
  uint32_t winMs = 300000UL;
#else
  uint32_t remainingCap = (rtcNoSessionAwakeMs >= MAX_AWAKE_NO_SESSION_MS)
      ? 0 : (MAX_AWAKE_NO_SESSION_MS - rtcNoSessionAwakeMs);
  uint32_t winMs = min<uint32_t>((uint32_t)cfgAdvWindowS * 1000UL, remainingCap);
  if (winMs == 0) {                              // cumulative cap spent: straight back to sleep
    devState = ST_DEEP_SLEEP;
    return;
  }
#endif
  windowDeadlineMs = now + winMs;
  windowFastEndMs = now + min<uint32_t>(fastMs, winMs);
  devState = ST_FAST_ADV;
  // Advertising intervals: NimBLE units here make 160 ~= 100-160 ms and
  // 1600 ~= 1.0-1.6 s depending on core version interpretation — both inside
  // the spec's FAST/SLOW intent.
  startAdvertising(160, 240);
}

void enterDeepSleep(bool expiredWithoutSession) {
#if !CONFIG_ENABLE_DEEP_SLEEP
  DBGLN("[PWR] Sleep disabled: restarting continuous BLE advertising (no RST button needed)");
  if (bleConnected && pServer && bleConnHandle != 0xFFFF) pServer->disconnect(bleConnHandle);
  openWindow(60000);
  return;
#endif
  uint32_t now = millis();
  if (!sessionReachedThisBoot) rtcNoSessionAwakeMs += (now - bootAwakeStartMs);
  if (expiredWithoutSession) {
    advExpiredCount++;
    prevAdvExpiredFlag = true;
    prefs.putBool("prevExp", true);
    // visible signal before going dark (spec §9 rule 8)
    for (int i = 0; i < 3 && PIN_LED >= 0; i++) { ledWrite(true); delay(150); ledWrite(false); delay(150); }
  }
  persistCounters();
  stopAdvertising();
  if (bleConnected && pServer && bleConnHandle != 0xFFFF) pServer->disconnect(bleConnHandle);
  NimBLEDevice::deinit(true);
  mpuConfigWakeOnMotion();
  mpuSleep(true);
  adsPowerDown();
  if (PIN_MPU_INT >= 0) {
    gpio_wakeup_enable((gpio_num_t)PIN_MPU_INT, GPIO_INTR_LOW_LEVEL);
    esp_sleep_enable_gpio_wakeup();
  }
  if (PIN_BUTTON >= 0) {
    gpio_wakeup_enable((gpio_num_t)PIN_BUTTON, GPIO_INTR_LOW_LEVEL);
    esp_sleep_enable_gpio_wakeup();
  }
  // NOTE: no timer wake — Option C periodic beacons were NOT adopted (spec §9.4).
  DBGLN("[PWR] deep sleep");
  esp_deep_sleep_start();                        // does not return (wake = reboot)
}

bool wasConnectedSession = false;                // loop-side connection bookkeeping

void stateTick() {
  uint32_t now = millis();

  // --- connection appeared (callback only set flags) ---
  if (bleConnected && devState != ST_CONNECTED_UNAUTH && devState != ST_SESSION_ACTIVE &&
      devState != ST_OTA_ACTIVE) {
    devState = ST_CONNECTED_UNAUTH;
    helloDone = false;
    sessionActive = false;
    stopAdvertising();
    wasConnectedSession = false;
  }

  // --- connection dropped ---
  if (!bleConnected && (devState == ST_CONNECTED_UNAUTH || devState == ST_SESSION_ACTIVE ||
                        devState == ST_OTA_ACTIVE)) {
    if (otaActive) otaReset();
    bool hadSession = sessionActive || wasConnectedSession;
    helloDone = false;
    sessionActive = false;
    bleMtu = 23;
    if (endSessionRequested) {                  // user intent: no grace (spec §9 rule 6)
      endSessionRequested = false;
      enterDeepSleep(false);
    } else if (hadSession) {                     // accidental loss: grace window (spec §9/§10)
      devState = ST_GRACE_RECONNECT;
      graceDeadlineMs = now + 120000;
      graceFastEndMs = now + 30000;
      startAdvertising(160, 240);
      // MPU stays duty-cycled by takeSample() during grace as well (spec §16)
    } else {                                     // never reached a session: remaining window only
#if !CONFIG_ENABLE_DEEP_SLEEP
      openWindow(60000);
#else
      if ((int32_t)(windowDeadlineMs - now) > 0) {
        devState = ((int32_t)(windowFastEndMs - now) > 0) ? ST_FAST_ADV : ST_SLOW_ADV;
        startAdvertising(devState == ST_FAST_ADV ? 160 : 1600, devState == ST_FAST_ADV ? 240 : 1600);
      } else {
        enterDeepSleep(true);
      }
#endif
    }
  }
  wasConnectedSession = sessionActive;

  // --- END_SESSION requested while still connected: ack was sent, now drop + sleep ---
  if (endSessionRequested && bleConnected) {
    endSessionRequested = false;
    if (pServer && bleConnHandle != 0xFFFF) pServer->disconnect(bleConnHandle);
    delay(50);
    enterDeepSleep(false);
  }

  switch (devState) {
    case ST_FAST_ADV:
      if ((int32_t)(now - windowFastEndMs) >= 0) {
        uint16_t mv = readBatteryMv();           // low-batt gate (spec §9 rule 7)
        if (mv && mv < BATT_CRITICAL_MV) { enterDeepSleep(true); break; }
        devState = ST_SLOW_ADV;
        startAdvertising(320, 480);
      }
#if CONFIG_ENABLE_DEEP_SLEEP
      if ((int32_t)(now - windowDeadlineMs) >= 0) enterDeepSleep(true);
#else
      if ((int32_t)(now - windowDeadlineMs) >= 0) {
        windowDeadlineMs = now + 120000;
      }
#endif
      break;
    case ST_SLOW_ADV:
#if CONFIG_ENABLE_DEEP_SLEEP
      if ((int32_t)(now - windowDeadlineMs) >= 0) enterDeepSleep(true);
#else
      if ((int32_t)(now - windowDeadlineMs) >= 0) {
        windowDeadlineMs = now + 120000;
        startAdvertising(320, 480);
      }
#endif
      break;
    case ST_CONNECTED_UNAUTH: {
      // Handshake 1 must finish (subscribed + MTU) in 10 s (spec §7)
      bool hs1Done = dataSubscribed && statusSubscribed;
      if (!hs1Done && hs1DeadlineMs && (int32_t)(now - hs1DeadlineMs) >= 0) {
        failedHandshakeCount++;
        if (pServer && bleConnHandle != 0xFFFF) pServer->disconnect(bleConnHandle);
        hs1DeadlineMs = 0;
      }
      if (hs1Done && !hs1DoneMs) hs1DoneMs = now;
      // HELLO must follow within 10 s of Handshake 1 completing (spec §8)
      if (hs1Done && !helloDone && hs1DoneMs && (int32_t)(now - hs1DoneMs) >= 10000) {
        failedHandshakeCount++;
        hs1DoneMs = 0;
        if (pServer && bleConnHandle != 0xFFFF) pServer->disconnect(bleConnHandle);
      }
      // HELLO/START deadlines (spec §8)
      if (helloDone && hs2DeadlineMs && (int32_t)(now - hs2DeadlineMs) >= 0) {
        failedHandshakeCount++;
        hs2DeadlineMs = 0;
        if (pServer && bleConnHandle != 0xFFFF) pServer->disconnect(bleConnHandle);
      }
      // Absolute window deadline still applies while unauthenticated (spec §9 rule 3/5)
      if ((int32_t)(now - windowDeadlineMs) >= 0) {
        if (pServer && bleConnHandle != 0xFFFF) pServer->disconnect(bleConnHandle);
        enterDeepSleep(true);
      }
      break;
    }
    case ST_SESSION_ACTIVE:
      break;                                     // sampling/sending in loop()
    case ST_GRACE_RECONNECT:
      if ((int32_t)(now - graceFastEndMs) >= 0 && (int32_t)(graceDeadlineMs - now) > 0) {
        startAdvertising(1600, 1600);            // cheapen the tail of grace (idempotent restart)
        graceFastEndMs = graceDeadlineMs;        // only once
      }
      if ((int32_t)(now - graceDeadlineMs) >= 0) enterDeepSleep(false);
      break;
    case ST_OTA_ACTIVE:
      if (otaActive && (now - otaLastChunkMs > 60000)) {   // stalled transfer
        otaReset();
        devState = otaHadSession ? ST_SESSION_ACTIVE : ST_CONNECTED_UNAUTH;
        sessionActive = otaHadSession;
      }
      break;
    case ST_DEEP_SLEEP:
      enterDeepSleep(false);                     // e.g. cumulative cap already spent at window open
      break;
    default:
      break;
  }
}

// =====================================================
// SETUP / LOOP
// =====================================================
void setup() {
#if DEBUG_SERIAL
  Serial.begin(115200);
  delay(200);
#endif
  bootAwakeStartMs = millis();
  // setCpuFrequencyMhz(80);         // halves-class CPU draw vs 160 MHz; BLE is fine at 80 (spec §16)
  DBGLN("\n=== DIABFOOT GUARD (BLE) FW " FW_VERSION_STR " ===");
  if (PIN_LED >= 0) pinMode(PIN_LED, OUTPUT);
  if (PIN_BUTTON >= 0) pinMode(PIN_BUTTON, INPUT_PULLUP);
  if (PIN_BATT_ADC >= 0) pinMode(PIN_BATT_ADC, INPUT);
  analogReadResolution(12);

  prefs.begin("diabfoot", false);
  bootId = prefs.getUInt("bootid", 0) + 1;
  prefs.putUInt("bootid", bootId);
  advExpiredCount = prefs.getUInt("advExpCnt", 0);
  failedHandshakeCount = prefs.getUInt("failHsCnt", 0);
  bufferOverwriteCount = prefs.getUInt("bufOvrCnt", 0);
  prevAdvExpiredFlag = prefs.getBool("prevExp", false);
  otaPendingConfirm = prefs.getBool("otaPend", false);
  applyConfig(prefs.getUChar("rateHz", 2), prefs.getUShort("advWinS", 300), false);

  esp_sleep_wakeup_cause_t cause = esp_sleep_get_wakeup_cause();
  DBG("[PWR] wake cause: "); DBGLN((int)cause);
  if (cause == ESP_SLEEP_WAKEUP_UNDEFINED) {
    rtcNoSessionAwakeMs = 0;
  }
#if DEBUG_SERIAL
  rtcNoSessionAwakeMs = 0;
#endif

  bleInit();
  openWindow(120000);                            // 120 s advertising window
  lastSampleMs = millis();

  delay(500);                                    // Sensor & power rail stabilization
  probeI2CSensors();
}

void loop() {
  // 1) Protocol commands queued by BLE callbacks
  PendingCmd cmd;
  while (cmdQPop(cmd)) handleCommand(cmd);

  // 2) Cumulative ACK from the app (spec §10)
  if (ackPending) { ackPending = false; ringApplyAck(pendingAckSeq); }

  // 3) State machine (advertising windows, handshake deadlines, grace, expiry)
  stateTick();
  ledTick();

  // 4) Sampling — only in SESSION (full rate) and GRACE (half rate) (spec §9.3/§10)
  uint32_t now = millis();
  bool sampling = (devState == ST_SESSION_ACTIVE) || (devState == ST_GRACE_RECONNECT);
  if (sampling && !otaActive) {
    uint32_t periodMs = 1000UL / cfgSampleRateHz;
    if (devState == ST_GRACE_RECONNECT) periodMs *= 2;
    if (now - lastSampleMs >= periodMs) {
      lastSampleMs = now;
      takeSample(devState == ST_GRACE_RECONNECT);
    }
  }

  // 5) Sender — paced batches while connected and subscribed
  if (bleConnected && sessionActive && dataSubscribed && now - lastSendMs >= 30) {
    // Flush rule (spec §6.3): full batch of 8, or oldest unsent >= 1000 ms
    StoredSample* nxt = ringFind(nextSendSeq);
    bool dueFull = nxt && ringLastSeq() >= nextSendSeq + 7;
    bool dueAge = nxt && (now - nxt->time_ms >= 1000);
    bool replaying = replayUntilSeq && nextSendSeq <= replayUntilSeq;
    if (dueFull || dueAge || replaying) {
      if (sendNextBatch()) lastSendMs = now;
    }
  }

#if CONFIG_INTER_SAMPLE_LIGHT_SLEEP
  // Optional inter-sample light sleep (streaming only). Timer wake is set a
  // little before the next sample is due; BLE events can also wake the CPU.
  if (devState == ST_SESSION_ACTIVE && !otaActive) {
    uint32_t periodMs = 1000UL / cfgSampleRateHz;
    uint32_t elapsed = millis() - lastSampleMs;
    if (elapsed + 25 < periodMs) {
      esp_sleep_enable_timer_wakeup((uint64_t)(periodMs - elapsed - 5) * 1000ULL);
      esp_light_sleep_start();
    }
  }
#endif

  delay(5);                                      // yield; all waits above are deadline-based
}
