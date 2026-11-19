# DiabFoot — IoT Diabetic Foot Monitoring

An Android application, ESP32-C3 firmware suite, and self-hosted Go + PostgreSQL backend for early risk monitoring of Diabetic Foot Ulcer (DFU) via a dual smart-insole pair.

---

## Overview

DiabFoot connects to left and right ESP32-C3 smart insoles over Bluetooth Low Energy (BLE) to monitor plantar pressure distribution across five anatomical zones, midfoot skin temperature, and gait biomechanics.

The system uses a **three-layer architecture**:
1. **Edge (`firmware/`)** — ESP32-C3 microcontrollers sample 5× FSR sensors, an NTC thermistor (via ADS1115), and an MPU-6050 IMU, streaming BLE notifications at **5 Hz**.
2. **Patient Mobile App (`app/`)** — Kotlin + Jetpack Compose (Clean Architecture, Hilt, Room DB) handles packet parsing, IMU+FSR walking FSM, wear-gating, 2-day thermal asymmetry (`ΔT >= 2.2 °C`) & weight-scaled plantar pressure evaluation, calm patient-facing guidance (`PromptRuleEngine`), and offline-first persistence.
3. **Cloud Backend (`cloud/`)** — Self-hosted Go REST API (`cloud/server/`) + PostgreSQL (`cloud/schema.sql`) acting purely as an idempotent **dumb store** (`DATA_CONTRACT.md` v1.0) with unified JWT auth (Google ID-token + email/password) and 1:N doctor–patient pairing. Clinician analytics are rendered client-side by the companion web console (`diabfoot-webapp-for-doctor`, Vue 3 + Vite + TypeScript).

---

## Key Features

- **Real-time dual-insole BLE streaming** — Independent Left & Right GATT connections (`InsoleBleManager`) with 5 Hz stream + 1/min `T,` temperature heartbeat support
- **5-zone plantar pressure heatmap** — Anatomical mapping (`HALLUX`, `MTH1`, `MTH2`, `MTH4`, `HEEL`) rendered via `MapPressureToGridUseCase` and radial-gradient `PlantarPressureCard`
- **Bilateral temperature & wear-gating** — `WearGating` filters off-shoe ambient readings and `-999` fault sentinels; `ClinicalScoringVariables` tracks 2-day consecutive hotspots (`ΔT >= 2.2 °C`)
- **IMU + FSR step & gait FSM** — `WalkingFSM` fuses swing-phase unweighting (`< 40 kPa`) and stance touchdown (`>= 60 kPa`) with IMU motion energy
- **Versioned clinical rules & empathetic UX** — `PromptRuleEngine` + `ThresholdsConfig` (allometric body-weight scaling, persistence gates, and cooldowns)
- **Offline-first persistence & idempotent sync** — Room Database (with non-destructive migrations), clinical export (`DataExportManager`), diagnostics & scenario seeding (`ClinicalScenarioSeeder`), and Go cloud batch sync (`SyncCloudDataUseCase`)

---

## Repository Structure

```text
capstonediabfoot/
├── app/                   # Android patient app (Kotlin, Jetpack Compose, Hilt, Room)
│   └── src/test/          # 21 unit test suites (140 tests)
├── firmware/              # ESP32-C3 Arduino sketches, BLE specs & calibration benches
├── cloud/                 # Self-hosted Go REST API (server/), schema.sql, DATA_CONTRACT.md
├── checklist/             # 12 hardware/firmware/system verification suites (TEST-01..12) + Master PDF
├── docs/                  # Architecture plans, TECH_DEBT.md, UX-BRAND-SPEC.md, report/ & CONTRIBUTION_SUMMARY.md
├── results/               # Gait trials & raw sensor CSV logs (results/raw_logs/)
└── scripts/               # Python BLE/USB loggers, calibration & simulator utilities
```

---

## BLE Telemetry Contract (v2 & Legacy v1)

`SensorPacketParser` auto-detects three line formats on the GATT notify characteristic:

| Format | Payload | Cadence |
|---|---|---|
| **v2 Stream** | `ax,ay,az,gx,gy,gz,p1,p2,p3,p4,p5` | 5 Hz (200 ms) |
| **v2 Slow Heartbeat** | `T,<tempC>[,<batPct>]` | 1/min (60 s) |
| **Legacy v1** | `ax,ay,az,gx,gy,gz,pressure,temperature` | 5 Hz |

- Accelerometer & gyroscope units (`m/s²` & `rad/s` from Adafruit MPU-6050) are normalized to `g` and `deg/s` via `SensorUnitsProfile` / `SensorUnitDetector`.

### BLE UUIDs

| Device | Service UUID | Characteristic UUID |
|---|---|---|
| **Left insole** (`Smart_Insole_Left`) | `4fa2c732-ca9a-4c20-9492-c167df3c942b` | `beb5483e-36e1-4688-b7f5-ea07361b26a8` |
| **Right insole** (`Smart_Insole_Right`) | `4fa2c732-ca9a-4c20-9492-c167df3c942c` | `beb5483e-36e1-4688-b7f5-ea07361b26c9` |

---

## Build & Test

### Android App (requires JDK 17)
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew testDebugUnitTest      # Run all 21 unit test suites (140 tests)
./gradlew lint                   # Android lint
```
Create `local.properties` in the root with `sdk.dir=/path/to/Android/sdk`. Configure the API URL via `-Pdiabfoot.apiBaseUrl=...`, `DIABFOOT_API_BASE_URL`, or `local.properties` (defaults and ADB reverse fallback live in `core/network/ApiConfig.kt`).

### Cloud Server (Go + PostgreSQL)
```bash
cd cloud/server
cp .env.example .env             # Set DATABASE_URL, JWT_SECRET, GOOGLE_CLIENT_ID
psql "$DATABASE_URL" -f ../schema.sql
go run .                         # Listens on :8080
```

---

## Verification & Documentation

- **Contribution breakdown & evidence index:** [`docs/CONTRIBUTION_SUMMARY.md`](docs/CONTRIBUTION_SUMMARY.md)
- **Hardware & BLE verification suite (`TEST-01`..`TEST-12`):** [`checklist/README.md`](checklist/README.md)
- **Technical debt & design decisions:** [`docs/TECH_DEBT.md`](docs/TECH_DEBT.md)
- **Patient UX rules:** [`docs/design/UX-BRAND-SPEC.md`](docs/design/UX-BRAND-SPEC.md)
- **Cloud payload specification:** [`cloud/DATA_CONTRACT.md`](cloud/DATA_CONTRACT.md)

---

## License

This project is developed for academic/capstone purposes.
