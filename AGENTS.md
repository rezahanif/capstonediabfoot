# AGENTS.md — DiabFoot Workspace Instructions

> **Note:** the agent's decision workflow (categorize every solution as **Fix** → plan & execute, or **Open-ended** → compare options & trade-offs → wait for the user's decision; always ask when confirmation is missing) lives in the user-level instructions at `~/.zcode/AGENTS.md`, so it applies in every workspace, not just this repo.

## 1. Project snapshot

**DiabFoot** — Android app (capstone) monitoring diabetic foot health via a custom BLE smart insole pair (ESP32-C3: MPU-6050 IMU + 5× FSR pressure + NTC temperature). Kotlin, Jetpack Compose, Clean Architecture, Hilt, Room. Namespace: `com.project.insole`.

- `app/src/main/java/com/project/insole/`
  - `core/` — cross-cutting: `ble/` (GATT managers), `database/` (Room), `di/` (Hilt `AppModule`), `security/` (`EncryptionManager`), `network/`, `theme/` (locked brand tokens), `notifications/`, `presentation/`
  - `features/<name>/{data,domain,presentation}` — auth, sensor, tracking, trends, diagnostics, notifications, settings, main
- `cloud/` — **separate** backend: Go server (`cloud/server/`), PostgreSQL DDL (`schema.sql`), payload contract (`DATA_CONTRACT.md`). Cloud **stores only, never processes** — no clinical logic on the server.
- `docs/TECH_DEBT.md` — authoritative open-issues log (TD-1 … TD-13). **Read before touching** WalkingFSM, packet parsing, thresholds, battery/temperature display.
- `docs/design/UX-BRAND-SPEC.md` — locked patient-app UX & brand rules (R1–R7).
- `docs/plans/TD3-TD4-PLAN.md` — approved migration plan for BLE payload v2 + fabricated-data cleanup.

## 2. Build & test

```bash
./gradlew assembleDebug          # build (JDK 17 required)
./gradlew testDebugUnitTest      # unit tests (app/src/test, JUnit4 + coroutines-test)
./gradlew lint                   # Android lint
```

- Toolchain: Gradle 8.13, AGP 8.13.2, Kotlin 2.0.21, compileSdk/targetSdk 34, minSdk 26.
- BLE requires a **physical device** — nothing BLE-related can be verified on an emulator.
- Gotcha: `settings.gradle.kts` and root `build.gradle.kts` force `kotlinx-metadata-jvm:0.9.0` (Kotlin 2.0 compatibility for Hilt/KSP). Do **not** remove this resolution override.

## 3. Architecture & data rules

- **Layer flow:** presentation → domain → data. `domain/` is pure Kotlin (no Android imports) — keep FSM, counters, models there.
- **Parse BLE once** in the data layer. UI must never re-parse raw BLE strings (this is TD-3's original sin).
- **Room migrations must be real `Migration` objects** (see `MIGRATION_2_3` in `AppModule.kt`). Never use `fallbackToDestructiveMigration` — wiping historical step data is unacceptable for a medical monitoring app.
- **Sensor units:** firmware sends m/s² and rad/s; the app normalizes to canonical internal units (g, deg/s) via `SensorUnitsProfile` with runtime auto-detection. FSM thresholds are tuned in g/deg/s — never change firmware and FSM independently.
- **Design system:** colors/shapes/type come from `DashboardColors` tokens in `core/theme/DashboardTheme.kt`. Never hardcode `Color(0x…)` literals; extend the palette with an official token if one is missing.
- **Product rules R1–R7** (UX-BRAND-SPEC.md) are binding: no raw medical numbers to the patient, single abnormal readings never prompt, cooldowns on prompts, "no data" never looks like "all clear", and above all **R7 — data shown must be real; show honest empty states instead of fabricated values**.

## 4. Known traps (see TECH_DEBT.md for full detail)

- **TD-10 (open, high):** firmware BLE payload is migrating 8-field → 12-field CSV. Parser changes must version-detect both, guard against the `-999` temperature sentinel and zero-IMU packets (phantom steps otherwise).
- **TD-12:** firmware cadence (~1.2 Hz) mismatches the app's 5 Hz staleness watchdog — decide before re-injecting BLE.
- **TD-13:** no battery hardware exists; every battery number in the UI today is a placeholder. Don't present it as real.
- **TD-4:** several data sources (`Trends`, `Diagnostics`, `Supabase`) still contain fabricated values; `EncryptionManager`/`DataExportManager` are deliberate unwired scaffolds — check TECH_DEBT.md before assuming code is live.
- **TD-1:** steps counting during TRANSITION is deliberate (POC-validated) — do not "fix" without the owner's field-validation decision.
