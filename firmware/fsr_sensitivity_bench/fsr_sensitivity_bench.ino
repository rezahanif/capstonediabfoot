/*
 * DiabFoot Guard — FSR Sensitivity Bench Firmware
 * Single ESP32-C3 + Single FSR Benchmark
 *
 * Hardware Circuit:
 *   3.3V ─── [ FSR ] ───┬─── [ Reference Resistor R_ref ] ─── GND
 *                       │
 *                       └─── [ GPIO 0 / ADC1_CH0 ]
 *
 * Compatible with any R_ref (e.g. 10k, 22k, 47k, 100k, or trimpot).
 * Streams fast, low-noise oversampled ADC readings over USB Serial at 115200 baud.
 */

#include <Arduino.h>

// Pin Configuration
const int PIN_FSR = 0;              // GPIO 0 (ADC1_CH0 on ESP32-C3)
const int LED_PIN = 8;              // Built-in status LED on ESP32-C3 (if equipped)

// ADC Parameters
const float VCC = 3.3f;
const int ADC_MAX = 4095;
const int OVERSAMPLE_COUNT = 16;    // 16x oversampling for clean 12-bit resolution

// Stream Timing
const unsigned long STREAM_INTERVAL_MS = 20; // 50 Hz stream rate
unsigned long lastStreamMs = 0;
unsigned long sampleCount = 0;

void setup() {
  Serial.begin(115200);
  delay(1000); // Allow USB CDC enumeration

  // Configure ADC for 0 - 3.3V range
  analogReadResolution(12);
  analogSetAttenuation(ADC_11db);

  pinMode(PIN_FSR, INPUT);
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW); // LED ON (active low on typical C3)

  Serial.println();
  Serial.println("==========================================================");
  Serial.println("  DiabFoot Guard — FSR Sensitivity Bench Engine");
  Serial.println("  Pin: GPIO 0 (ADC1_CH0) | Baud: 115200 | Rate: 50 Hz");
  Serial.println("  Circuit: 3.3V -> FSR -> GPIO 0 -> R_ref -> GND");
  Serial.println("==========================================================");
  Serial.println("HEADER:time_ms,adc_raw,voltage_mv");
}

void loop() {
  unsigned long now = millis();

  if (now - lastStreamMs >= STREAM_INTERVAL_MS) {
    lastStreamMs = now;

    // 16x oversampled read
    long totalAdc = 0;
    for (int i = 0; i < OVERSAMPLE_COUNT; i++) {
      totalAdc += analogRead(PIN_FSR);
      delayMicroseconds(50);
    }

    int avgAdc = (int)(totalAdc / OVERSAMPLE_COUNT);
    avgAdc = constrain(avgAdc, 0, ADC_MAX);

    // Compute actual millivolts
    float voltageV = (avgAdc / (float)ADC_MAX) * VCC;
    int voltageMv = (int)(voltageV * 1000.0f);

    sampleCount++;

    // Stream formatted telemetry line
    Serial.printf("DATA,%lu,%d,%d\n", now, avgAdc, voltageMv);

    // Activity blink every 50 samples (1 sec)
    if (sampleCount % 50 == 0) {
      digitalWrite(LED_PIN, !digitalRead(LED_PIN));
    }
  }

  // Handle incoming serial commands from Python runner
  if (Serial.available()) {
    char cmd = Serial.read();
    if (cmd == 'P' || cmd == 'p') {
      Serial.println("[FSR_BENCH] PING_ACK");
    }
  }
}
