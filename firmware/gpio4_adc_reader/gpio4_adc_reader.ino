/*
 * DiabFoot Guard — ESP32-C3 GPIO 4 ADC Reader
 * Reads ADC values from GPIO 4 (ADC1_CH4) over USB Serial (CDC)
 *
 * Pinout:
 *   ESP32-C3 GPIO 4 = ADC1_CH4
 *   Resolution: 12-bit (0 - 4095)
 *   Voltage Range: 0 - 3.3V (ADC_11db)
 */

#include <Arduino.h>

const int PIN_ADC = 4;              // GPIO 4 (ADC1_CH4)
const int LED_PIN = 8;              // Status LED (active LOW on most C3 modules)
const int OVERSAMPLE_COUNT = 16;    // 16x oversampling for clean, low-noise reading
const float VCC = 3.3f;
const int ADC_MAX = 4095;

const unsigned long SAMPLE_INTERVAL_MS = 50; // 20 Hz update rate
unsigned long lastSampleMs = 0;
unsigned long sampleCount = 0;

void setup() {
  Serial.begin(115200);
  delay(1000); // Wait for USB CDC enumeration

  // Configure ADC
  analogReadResolution(12);
  analogSetAttenuation(ADC_11db); // 0 - 3.3V range

  pinMode(PIN_ADC, INPUT);
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW); // LED ON

  Serial.println();
  Serial.println("==========================================================");
  Serial.println("  ESP32-C3 ADC Monitor — GPIO 4 (ADC1_CH4)");
  Serial.println("  Baud: 115200 | Resolution: 12-bit (0-4095) | Range: 0-3.3V");
  Serial.println("==========================================================");
}

void loop() {
  unsigned long now = millis();

  if (now - lastSampleMs >= SAMPLE_INTERVAL_MS) {
    lastSampleMs = now;

    // 16x oversampled read
    long totalAdc = 0;
    for (int i = 0; i < OVERSAMPLE_COUNT; i++) {
      totalAdc += analogRead(PIN_ADC);
      delayMicroseconds(50);
    }
    int avgAdc = (int)(totalAdc / OVERSAMPLE_COUNT);
    avgAdc = constrain(avgAdc, 0, ADC_MAX);

    float voltage = (avgAdc / (float)ADC_MAX) * VCC;
    float percent = (avgAdc / (float)ADC_MAX) * 100.0f;

    // Visual ASCII bar gauge (20 chars)
    int gaugeLen = (avgAdc * 20) / ADC_MAX;
    char gauge[21];
    for (int i = 0; i < 20; i++) {
      gauge[i] = (i < gaugeLen) ? '#' : '-';
    }
    gauge[20] = '\0';

    sampleCount++;

    // Format output: Human readable + parseable
    Serial.printf("[GPIO 4] ADC: %4d / 4095 (%5.1f%%) | Voltage: %5.3f V | [%s]\n",
                  avgAdc, percent, voltage, gauge);

    // Blink LED every 20 samples (1 sec)
    if (sampleCount % 20 == 0) {
      digitalWrite(LED_PIN, !digitalRead(LED_PIN));
    }
  }
}
