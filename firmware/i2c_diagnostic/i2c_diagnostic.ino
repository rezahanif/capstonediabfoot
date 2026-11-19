#include <Arduino.h>
#include <Wire.h>

void setup() {
  Serial.begin(115200);
  delay(2000);
  Serial.println("\n[DIAG] === ESP32-C3 I2C PIN DIAGNOSTIC ===");
  Serial.flush();

  const int sda = 8;
  const int scl = 9;

  Serial.printf("[DIAG] Step 1: Testing GPIO %d (SDA) and GPIO %d (SCL) as digital inputs...\n", sda, scl);
  pinMode(sda, INPUT_PULLUP);
  pinMode(scl, INPUT_PULLUP);
  delay(100);
  int val_sda = digitalRead(sda);
  int val_scl = digitalRead(scl);
  Serial.printf("[DIAG] Pin levels with pullup: SDA=%d, SCL=%d\n", val_sda, val_scl);
  if (val_sda == 0) Serial.println("[DIAG] WARNING: SDA is held LOW by hardware!");
  if (val_scl == 0) Serial.println("[DIAG] WARNING: SCL is held LOW by hardware!");
  Serial.flush();

  Serial.println("[DIAG] Step 2: Calling Wire.begin(8, 9, 100000)...");
  Serial.flush();
  bool w_ok = Wire.begin(sda, scl, 100000);
  Serial.printf("[DIAG] Wire.begin returned: %s\n", w_ok ? "TRUE" : "FALSE");
  Wire.setTimeOut(50);
  Serial.flush();

  Serial.println("[DIAG] Step 3: Probing ADS1115 at 0x48...");
  Serial.flush();
  Wire.beginTransmission(0x48);
  byte err_ads = Wire.endTransmission();
  Serial.printf("[DIAG] Probing 0x48 returned err: %d (%s)\n", err_ads,
    err_ads == 0 ? "ACK (OK)" : (err_ads == 2 ? "NACK (Not Found)" : (err_ads == 5 ? "TIMEOUT" : "OTHER ERR")));
  Serial.flush();

  Serial.println("[DIAG] Step 4: Probing MPU at 0x68...");
  Serial.flush();
  Wire.beginTransmission(0x68);
  byte err_mpu = Wire.endTransmission();
  Serial.printf("[DIAG] Probing 0x68 returned err: %d (%s)\n", err_mpu,
    err_mpu == 0 ? "ACK (OK)" : (err_mpu == 2 ? "NACK (Not Found)" : (err_mpu == 5 ? "TIMEOUT" : "OTHER ERR")));
  Serial.flush();

  Serial.println("[DIAG] Step 5: Full I2C scan from 0x08 to 0x77 (skipping reserved)...");
  Serial.flush();
  int found = 0;
  for (uint8_t a = 8; a <= 119; a++) {
    Wire.beginTransmission(a);
    byte e = Wire.endTransmission();
    if (e == 0) {
      Serial.printf("[DIAG] -> Found device at 0x%02X!\n", a);
      found++;
    }
  }
  Serial.printf("[DIAG] Scan complete. Total devices found: %d\n", found);
  Serial.flush();
}

void loop() {
  Serial.println("[DIAG] Heartbeat tick...");
  delay(2000);
}
