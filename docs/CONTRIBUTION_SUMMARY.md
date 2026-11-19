# Contribution Summary — Reza Hanif Firmansyah (`rezahanif`)

Individual software, firmware, cloud, and verification contributions for **DiabFoot Guard** (capstone C-601 documentation reference; detailed Indonesian report draft in [`docs/report/Bab_07.tex`](report/Bab_07.tex)).

---

## 1. Subsystem Ownership & Key Modules

### Edge Firmware (`firmware/`)
- **Primary sketches:** [`firmware/diabfoot_guard_sensor/diabfoot_guard_sensor.ino`](../firmware/diabfoot_guard_sensor/diabfoot_guard_sensor.ino), [`firmware/insole_preproduction/insole_preproduction.ino`](../firmware/insole_preproduction/insole_preproduction.ino)
- **Responsibilities:**
  - Non-blocking `millis()` acquisition & transmission scheduler on single-core ESP32-C3
  - 5 Hz BLE GATT notify stream (`ax,ay,az,gx,gy,gz,p1,p2,p3,p4,p5`) + 60 s `T,<tempC>[,<batPct>]` NTC heartbeat
  - 11-node piecewise linear FSR LUT calibration, auto-tare fault guard, and inactivity sleep trigger
  - Supporting diagnostic/calibration benches under `firmware/` and `scripts/`

### Patient Mobile Application (`app/`)
- **BLE & Telemetry Pipeline:**
  - [`InsoleBleManager.kt`](../app/src/main/java/com/project/insole/core/ble/InsoleBleManager.kt) — dual-foot GATT connections, serialized descriptor writes, `SharedFlow` telemetry stream
  - [`SensorPacketParser.kt`](../app/src/main/java/com/project/insole/features/sensor/domain/model/SensorPacketParser.kt) — auto-detection of v2 11-field, `T,` heartbeat, and legacy 8-field CSV packets; `-999` sentinel filtering
  - [`SensorUnitDetector`](../app/src/main/java/com/project/insole/features/sensor/domain/model/SensorUnitsProfile.kt) — runtime normalization from `m/s²` + `rad/s` to canonical `g` + `deg/s`
- **Biomechanics & Clinical Domain Logic:**
  - [`WalkingFSM.kt`](../app/src/main/java/com/project/insole/features/sensor/domain/model/WalkingFSM.kt) — IMU + FSR swing-unweighting (`< 40 kPa`) and stance touchdown (`>= 60 kPa`) gait FSM
  - [`WearGating.kt`](../app/src/main/java/com/project/insole/features/sensor/domain/model/WearGating.kt) — physiological skin temp (`27.0..38.5 °C`) & active FSR contact filtering
  - [`ClinicalScoringVariables.kt`](../app/src/main/java/com/project/insole/features/sensor/domain/model/ClinicalScoringVariables.kt) — 2-day consecutive thermal asymmetry (`ΔT >= 2.2 °C`) & `LOW`/`MEDIUM`/`HIGH` risk scoring
  - [`ThresholdsConfig.kt`](../app/src/main/java/com/project/insole/features/notifications/domain/ThresholdsConfig.kt) — versioned rules with allometric weight scaling `min(P_nominal * (W/60)^0.67, 2800 kPa)`
  - [`PromptRuleEngine.kt`](../app/src/main/java/com/project/insole/features/notifications/domain/PromptRuleEngine.kt) — calm non-alarmist patient prompts, persistence gates, cooldowns, and `NO_DATA` rule
- **UI, Offline Persistence & Cloud Sync:**
  - [`MapPressureToGridUseCase.kt`](../app/src/main/java/com/project/insole/features/sensor/domain/usecase/MapPressureToGridUseCase.kt) & [`PlantarPressureCard.kt`](../app/src/main/java/com/project/insole/features/sensor/presentation/components/PlantarPressureCard.kt) — 5×10 zone mapping & radial-gradient Canvas foot heatmap
  - [`InsoleDatabase.kt`](../app/src/main/java/com/project/insole/core/database/InsoleDatabase.kt) — Room DB v4 with non-destructive migrations (`DailyFootMetricDao`, `NotificationDao`, `StepDao`, `HourlyStepDao`)
  - [`GenerateCloudPayloadUseCase.kt`](../app/src/main/java/com/project/insole/features/sensor/domain/usecase/GenerateCloudPayloadUseCase.kt), [`SyncCloudDataUseCase.kt`](../app/src/main/java/com/project/insole/features/sensor/domain/usecase/SyncCloudDataUseCase.kt), [`DataExportManager.kt`](../app/src/main/java/com/project/insole/features/sensor/domain/service/DataExportManager.kt)
  - [`DiagnosticsScreen.kt`](../app/src/main/java/com/project/insole/features/diagnostics/presentation/DiagnosticsScreen.kt) & [`ClinicalScenarioSeeder.kt`](../app/src/main/java/com/project/insole/features/diagnostics/presentation/seeder/ClinicalScenarioSeeder.kt)

### Cloud Backend (`cloud/`) & Companion Clinician Web App
- **Go REST API (`cloud/server/`):** `main.go`, `auth.go` (Google ID-token + email/password JWT), `db.go`, `pairing.go`, `uploads.go` (`Idempotency-Key`), `config.go`, `cors.go`
- **Database & Contract:** [`cloud/schema.sql`](../cloud/schema.sql) (PostgreSQL, pseudonymous `PT-*` IDs, 1:N `doctor_patients`, `audit_log`) and [`cloud/DATA_CONTRACT.md`](../cloud/DATA_CONTRACT.md) v1.0
- **Clinician Web Console (separate repo `diabfoot-webapp-for-doctor`):** Vue 3 + Vite + TypeScript + ECharts dashboard consuming `/v1/*` endpoints

---

## 2. Verification & Test Evidence

### Android Unit Tests (`./gradlew testDebugUnitTest --rerun-tasks`)
All **21 test suites (140 unit tests)** pass with 0 failures and 0 skipped tests, covering:
- BLE reconnect & packet parsing (`SensorPacketParserTest` — 20 tests, `SensorUnitsProfileTest` — 10 tests, `ReconnectPolicyTest` — 4 tests)
- Gait FSM & wear-gating (`WalkingFSMTest` — 25 tests, `DualFootStepCounterTest` — 7 tests, `WearGatingTest` — 3 tests)
- Clinical scoring & prompt rules (`PromptRuleEngineTest` — 19 tests, `ClinicalRiskScoringTest` — 2 tests, `AnalyzePressureThresholdUseCaseTest` — 6 tests)
- Heatmap, cloud payload, pairing, trends & seeder (`MapPressureToGridUseCaseTest` — 9 tests, `GenerateCloudPayloadUseCaseTest` — 2 tests, `TrendsViewModelTest` — 3 tests, `ClinicalScenarioSeederTest` — 3 tests, `DoctorPairingRemoteSourceTest` — 3 tests, plus entity/security/notification tests)

### Hardware, BLE & Endurance Verification (`checklist/`)
- 12 verification test suites documented in [`checklist/README.md`](../checklist/README.md) (`TEST-01`..`TEST-12`), scripts under `checklist/test_*/scripts/`, raw CSV/JSON/Markdown reports under `checklist/test_*/results/`, and [`checklist/DiabFoot_Master_Verification_Report_Tests_01-11.pdf`](../checklist/DiabFoot_Master_Verification_Report_Tests_01-11.pdf).
