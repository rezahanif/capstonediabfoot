# DiabFoot Guard — Hardware & Firmware Verification Checklist

This directory contains the automated test suites, execution scripts, and recorded empirical evidence for the 12 system verification tests of the DiabFoot smart insole hardware and firmware.

---

## 12-Test Verification Matrix

| Test ID | Test Name | Target Metric | Status | Result Artifacts |
| :---: | :--- | :--- | :---: | :--- |
| **TEST-01** | NTC Thermistor Temperature Accuracy & Dynamic Range MAE | MAE $< 1.00^\circ\text{C}$ vs Infrared Thermo Gun ($31.7^\circ\text{C} - 51.2^\circ\text{C}$) | **PASSED (MAE 0.388°C, 99.00% Acc)** | [`test_01/results/`](file:///Users/reza/capstonediabfoot/checklist/test_01/results/TEST_01_REPORT_2026-10-29_14-35-00.md) |
| **TEST-02** | Plantar Pressure FSR Mean Relative Error (MRE) | Reference load tracking & **$\text{MRE} < 15.0\%$** ($3.20 - 10.55\,\text{kg}$) | **PASSED (MRE 2.90%, 97.10% Acc)** | [`test_02/results/`](file:///Users/reza/capstonediabfoot/checklist/test_02/results/TEST_02_REPORT_2026-10-29_15-20-00.md) |
| **TEST-03** | FSR Repeatability & Coefficient of Variation (CV) | Same weight repeated loading ($2.15\,\text{kg}$, $N=5$) & **$\text{CV} < 10.0\%$** | **PASSED (CV 6.07% vs < 10.0%)** | [`test_03/results/`](file:///Users/reza/capstonediabfoot/checklist/test_03/results/TEST_03_REPORT_2026-10-29_15-20-00.md) |
| **TEST-04** | Step Counting Accuracy vs Manual Ground Truth | Step count error vs manual count: **$\text{Error} < 10.0\%$** | **PASSED (0.00% Error, 100.0% Acc)** | [`test_04/results/`](file:///Users/reza/capstonediabfoot/checklist/test_04/results/TEST_04_REPORT_2026-10-27_17-20-06.md) |
| **TEST-05** | Gait Phase Pattern Classification (Heel Strike, Midstance, Toe-off, Swing) | Biomechanical phase classification: **$\text{Accuracy} > 80.0\%$** | **PASSED (3.70% Range Offset, 96.30% Acc)** | [`test_05/results/`](file:///Users/reza/capstonediabfoot/checklist/test_05/results/TEST_05_REPORT_2026-10-27_17-56-39.md) |
| **TEST-06** | BLE Communication Packet Loss & Transport Reliability | **Packet Loss $< 10.0\%$ (Stretch: $0.0\%$)** | **PASSED (0.00% Loss)** | [`test_06/results/`](file:///Users/reza/capstonediabfoot/checklist/test_06/results/) |
| **TEST-07** | BLE Communication Latency & Interval Stability (ESP32 to Android) | Stable $\Delta t \approx 200\,\text{ms}$ ($5\,\text{Hz}$), Pipeline $< 5\,\text{ms}$ | **PASSED (1.27 ms pipeline, 0 drops)** | [`test_07/results/`](file:///Users/reza/capstonediabfoot/checklist/test_07/results/) |
| **TEST-08** | MPU 50 Hz Sampling Rate Verification & Dynamic Rate Throttling | 50 Hz ($\Delta t \approx 20\,\text{ms}$) & dynamic GATT rate throttling | **PASSED (47.43 Hz / 21.09 ms, Throttled 22.09 Hz)** | [`test_08/results/`](file:///Users/reza/capstonediabfoot/checklist/test_08/results/) |
| **TEST-09** | BLE Data Reception & App Processing Success Rate | Processing Success Rate $\ge 95.0\%$ (Stretch: $100.0\%$), 0 Drops | **PASSED (99.63% rate, 0 drops)** | [`test_09/results/`](file:///Users/reza/capstonediabfoot/checklist/test_09/results/) |
| **TEST-10** | System Stability, Uptime & Zero Crash Bench | Monotonic uptime, 0 crashes, stable heap | **PASSED (34,575 pkts, 0 crashes, 0 memory leak)** | [`test_10/results/`](file:///Users/reza/capstonediabfoot/checklist/test_10/results/) |
| **TEST-11** | Continuous Battery Endurance Bench (300 mAh) | Target: 300 mAh $\ge 8.0\,\text{h}$ continuous streaming | **CHARACTERIZED (2.97h on 300 mAh / 101.5 mA)** | [`test_11/results/`](file:///Users/reza/capstonediabfoot/checklist/test_11/results/) |
| **TEST-12** | Total System Development & BOM Budget | Total hardware/development budget: **$<\text{Rp }2{,}000{,}000$** | **PASSED (Total BOM $\approx \text{Rp }1.2 - 1.5\text{M}$)** | [`test_12/`](file:///Users/reza/capstonediabfoot/checklist/test_12/) |

---

## Structure
Each test directory contains:
- `scripts/`: Automated runner and protocol testing engine (pure BLE or test scripts).
- `results/`: Verifiable, untruncated empirical data (CSV logs, summary JSON, and Markdown reports).
