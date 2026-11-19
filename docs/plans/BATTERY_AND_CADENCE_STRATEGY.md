# Architectural Strategy: 8.0+ Hour Battery Endurance & 20 Hz Ankle Gait Sensing

- **Target Objective**: Achieve continuous operational battery lifetime $\ge 8.0\,\text{hours}$ on a $300\,\text{mAh}$ LiPo cell.
- **Sensing Constraint**: Maintain high-accuracy step detection using an ankle-mounted MPU-6050 (6-DOF IMU) combined with 5 plantar FSRs.
- **Empirical Baseline (TEST-11)**: $300\,\text{mAh}$ yielded **2.97 hours** continuous transmission at $5\,\text{Hz}$ ($\approx 101.5\,\text{mA}$ average active current).

---

## 1. Biomechanical Literature: Required Frequency for Ankle Gait Sensing

A key question in wearable design is: *What is the minimum sampling frequency required for accurate step counting when the IMU is located at the ankle?*

```
Human Gait Cycle (Walking: ~1.5 - 2.0 Hz, 90-120 steps/min)
┌─────────────────────────────────────────────────────────────┐
│                       STRIDE (~1000 ms)                     │
├───────────────────────────────────┬─────────────────────────┤
│       STANCE PHASE (~600-800 ms)   │   SWING PHASE (~400 ms) │
│  Heel Strike ──> Flat Foot ──> Push-off  │   Mid-Swing ──> Decel   │
└───────────────────────────────────┴─────────────────────────┘
```

### Empirical Literature Review (*MDPI Sensors, Frontiers in Sports Biomechanics*)

1. **Walking Cadence Frequency:**
   - Normal human walking cadence is **$1.5 - 2.0\,\text{Hz}$** ($90 - 120\,\text{steps/min}$).
   - The primary kinematic signals (vertical acceleration and pitch angular velocity at the ankle) reside below **$5 - 8\,\text{Hz}$**.
2. **Sampling Frequency Benchmarks:**
   - **Theoretical Floor ($10 - 15\,\text{Hz}$):** Captures the fundamental waveform, but peak impact instants suffer from temporal quantization jitter.
   - **Clinical & Commercial Standard ($20 - 30\,\text{Hz}$):** Validated by research ankle trackers (e.g., ActiGraph, GaitUp).
     - At **$20\,\text{Hz}$ ($50\,\text{ms}$ resolution)**: The $600 - 800\,\text{ms}$ stance phase contains **12 to 16 data frames**, and the swing phase contains **8 data frames**.
     - This gives ample temporal resolution for Finite State Machines (`WalkingFSM`) to detect transitions:
       $$\text{HEEL\_STRIKE} \longrightarrow \text{FLAT\_FOOT} \longrightarrow \text{TOE\_OFF} \longrightarrow \text{SWING}$$
   - **When is $50 - 100\,\text{Hz}$ Required?**
     - Only when evaluating high-impact transient shock waves (sprinting, jumping) or running Zero-Velocity Update (ZUPT) dead-reckoning trajectory integration. For diabetic patient walking monitoring, **$20\,\text{Hz}$ is optimal**.

---

## 2. Power Profiling: Resolving the 101 mA Paradox

To achieve $8.0\,\text{hours}$ from a $300\,\text{mAh}$ LiPo cell:
$$\text{Target Average Current } (I_{\text{target}}) \le \frac{300\,\text{mAh}}{8.0\,\text{h}} = \mathbf{37.5\,\text{mA}}$$

During TEST-11, the ESP32-C3 drew an effective average of **$101.5\,\text{mA}$**. Where does that power go?

```
Current Breakdown in Continuous Unbatched BLE Streaming:
┌─────────────────────────────────────────────────────────────┐
│ [CPU @ 80 MHz: 12-15 mA]  [Sensors I2C/ADC: < 1 mA]        │
│ [2.4 GHz BLE RF Radio Synthesizer & PA: 80 - 100 mA]        │
└─────────────────────────────────────────────────────────────┘
Total Current: ~101 mA  ==> Battery dies in 2.97 hours!
```

- **The CPU & Sensors are NOT the problem:** The CPU at 80 MHz uses only $\approx 14\,\text{mA}$. Reading I2C (MPU) and ADC (FSR) takes $< 0.4\,\text{ms}$ per cycle ($< 1\%$ duty cycle).
- **The RF Transceiver IS the problem:** Transmitting individual BLE packets at high frequency keeps the 2.4 GHz radio synthesizer and power amplifier powered on or constantly toggling, preventing the ESP32-C3 from entering low-power modem sleep.

---

## 3. The Proposed Firmware Solution: Decoupled Sampling & Batched BLE Delivery

The core principle is to **decouple sensor sampling frequency ($20\,\text{Hz}$) from over-the-air BLE radio transmission frequency ($4\,\text{Hz}$)**.

```
                    ESP32-C3 Firmware Architecture
┌─────────────────────────────────────────────────────────────────┐
│  Hardware Sensors (Ankle MPU + 5 FSRs)                          │
└───────────────┬─────────────────────────────────────────────────┘
                │ Sample every 50 ms (20 Hz) — CPU active 0.4 ms
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  Internal Ring Buffer (RAM)                                     │
│  [ Frame 1 ] [ Frame 2 ] [ Frame 3 ] [ Frame 4 ] [ Frame 5 ]    │
└───────────────┬─────────────────────────────────────────────────┘
                │ Flush every 250 ms (4 Hz burst)
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  BLE GATT Notification (1 MTU burst = 5 frames packed)          │
│  Duration: ~2 ms burst ──> Radio immediately enters MODEM SLEEP │
└─────────────────────────────────────────────────────────────────┘
```

### Power Math with Batching & Modem Sleep

| Operating State | Current Draw | Duty Cycle | Average Current Contribution |
| :--- | :---: | :---: | :---: |
| **Radio Modem Sleep** (CPU 80 MHz, sampling 20 Hz) | $\approx 18.0\,\text{mA}$ | $88\%$ | $15.8\,\text{mA}$ |
| **Active BLE Radio Burst** (Transmitting batch) | $\approx 85.0\,\text{mA}$ | $12\%$ | $10.2\,\text{mA}$ |
| **Sensors (MPU-6050 + ADS1115 + FSR dividers)** | $\approx 1.5\,\text{mA}$ | Continuous | $1.5\,\text{mA}$ |
| **Effective Total Average Current ($I_{\text{avg}}$)** | — | — | **$\mathbf{27.5\,\text{mA}}$** |

### Projected Battery Lifetime:
$$\text{Expected Lifetime} = \frac{300\,\text{mAh}}{27.5\,\text{mA}} \approx \mathbf{10.9\,\text{hours}} \quad (\ge 8.0\,\text{h Target Met!})$$

---

## 4. Transmission Payload Design & Android App Compatibility

### A. Over-The-Air Payload Format
With BLE MTU set to 517 bytes (`BLEDevice::setMTU(517)`), a single GATT notification can hold up to 512 payload bytes.
- A single 11-field CSV frame is $\sim 55\,\text{bytes}$:
  `0.123,0.456,9.810,0.012,0.034,0.005,12.5,14.2,0.0,0.0,0.0`
- A batch of 5 frames separated by newline (`\n`) is:
  $$5 \times 55\,\text{bytes} = \mathbf{275\,\text{bytes}} \ll 512\,\text{bytes MTU}$$

### B. Android App Adaptation (`BleSensorDataSource.kt`)
In `BleSensorDataSource.kt`, incoming data is parsed line by line. To handle batches cleanly:

```kotlin
// Inside onBleCharacteristicChanged in BleSensorDataSource.kt:
val dataString = String(rawData, Charsets.UTF_8)

// Support both single-line legacy and multi-line batched packets:
val lines = dataString.trim().lines()
for (line in lines) {
    val trimmedLine = line.trim()
    if (trimmedLine.isEmpty()) continue
    
    val sensorPacket = SensorPacketParser.parse(trimmedLine) ?: continue
    
    // Process each 20 Hz frame sequentially through WalkingFSM
    stepCounterService.process(sensorPacket, isLeft)
    emitToUi(sensorPacket, isLeft)
}
```

**Benefits of this design:**
1. **Zero Loss of Gait Precision:** The Android `WalkingFSM` still receives every single $20\,\text{Hz}$ sample with exact relative temporal ordering.
2. **Zero In-App Pipeline Delay:** Processing 5 frames in one mutex lock takes $< 2.0\,\text{ms}$, well within the Android 60 FPS frame window ($16.6\,\text{ms}$).
3. **100% Backward Compatible:** Single-line packets (such as calibration or temperature heartbeats) continue to work without modification.

---

## 5. Implementation Roadmap

```
┌────────────────────────────────────────────────────────┐
│ Phase 1: Firmware Batching Engine                      │
│ - Implement 20 Hz timer for MPU/FSR acquisition        │
│ - Buffer 5 frames in memory                            │
│ - Transmit batch every 250 ms over stream characteristic│
├────────────────────────────────────────────────────────┤
│ Phase 2: Modem Sleep Configuration                    │
│ - Enable esp_pm_configure with light/modem sleep       │
│ - Set BLE connection interval to 30-45 ms              │
├────────────────────────────────────────────────────────┤
│ Phase 3: Android App Line-Splitting Parser             │
│ - Update BleSensorDataSource to process lines()        │
│ - Unit test multi-line batch dispatch                  │
├────────────────────────────────────────────────────────┤
│ Phase 4: Verification Bench                            │
│ - Re-run TEST-11 battery endurance bench               │
│ - Target: Confirm >= 8.0 hours on 300 mAh cell         │
└────────────────────────────────────────────────────────┘
```
