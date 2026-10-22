# Technical Debt Log

Findings that are deliberately **not fixed yet**, with the validation or
decision needed to close them. Items are ordered by risk.

## TD-1 — Steps are counted during TRANSITION (field validation pending)

**Status:** accepted behaviour (POC-authoritative), 2026-10-02.
**Code:** `WalkingFSM.process()` — step detection runs when
`state == WALKING || state == TRANSITION`.

**Problem:** the very first motion spike that leaves STANDING counts a step
before walking is confirmed. Each walk bout can therefore start with one
"phantom" step, and sit-to-stand movements count as steps too. The unit test
comment previously claimed "Steps only count in WALKING" — the comment was
wrong, not the code; it has been corrected to match actual behaviour.

**Why not fixed:** the current behaviour was validated in the POC and the
owner wants real-world numbers before changing clinical output.

**To close:**
1. Field test: walk a known distance (e.g. 50 steps) with both insoles and
   compare app step count vs manual count, including several sit-to-stand
   transitions without walking.
2. If counts are inflated: restrict step detection to WALKING only —
   one-line change at `WalkingFSM.process()` (drop the
   `|| state == WalkState.TRANSITION` condition) — and update
   `WalkingFSMTest` accordingly.

## TD-2 — Firmware sensor units (RESOLVED by firmware source, 2026-10-03)

**Status:** resolved by source. The new firmware sketch uses the Adafruit
MPU6050 library, which outputs **accel in m/s² and gyro in rad/s** — the
README was right, the FSM's "tuned in G units" comment was the wrong doc.
`SensorUnitDetector` will auto-detect and lock `MPU6050_MS2_RADS`
(accel ÷9.80665, gyro ×57.2958) at runtime; the FSM thresholds are unchanged.

**Remaining:** confirm the `Sensor unit profile locked: MPU6050_MS2_RADS`
logline on the first on-device session with the re-injected BLE stream.
Full analysis: `docs/plans/TD3-TD4-PLAN.md` §A1.

## TD-3 — UI re-parses raw BLE strings (IMPLEMENTED, 2026-10-03)

**Status:** closed. All three phases landed: `SensorPacketParser` (format
detection: legacy 8-field / v2 11-field stream / `T,` temperature line,
zero-IMU guard, range validation), `PlantarZone` + `FSR_ZONE_ORDER`
(S1=HALLUX … S5=HEEL per Gambar 8.3), `MapPressureToGridUseCase` rewritten
to the real 5-zone 5×10 grid and invoked by `SensorViewModel`
(`PressureMapState`), and `PlantarPressureCard` no longer parses anything —
it renders the ViewModel grid, with an honest un-zoned caption while legacy
firmware streams. Plan: `docs/plans/TD3-TD4-PLAN.md` Part B.

## TD-4 — Fabricated remote/diagnostic data sources (EXECUTED, 2026-10-03)

**Status:** executed. Deleted: fabricated `getDailySummary` /
`getWeeklyHealthScore` / `DailySummary` (Trends reads real Room steps).
`DiagnosticsDataSource` now reports real RSSI from `InsoleBleManager` scan
results and `batteryLevel = null` (renders "Unavailable") — no placeholder
numbers. `SensorConstants` relabeled with provenance: 200/240 = device-scale
placeholder (TD-11), 2.2 °C = literature-sourced (JMIR mHealth 2025 /
IWGDF 2023), step thresholds = POC-tuned. Encryption scope settled (Part C1
of the plan): patient profile = Keystore-encrypted (`EncryptionManager`'s
assigned job); sensor/usage summaries = TLS + plain JSONB. Remaining:
wire `EncryptionManager` when the cloud sync module exists; wire
`Supabase`/`Notification` scaffolds when the backend lands.

## TD-5 — Subscribe to ALL notify characteristics

**Status:** deferred (default kept deliberately).
**Code:** `InsoleBleManager.onServicesDiscovered` / `queueNotifications`.

**Problem:** the app subscribes to every notify-capable characteristic of
every service instead of matching `InsoleUUIDs.LEFT_CHARACTERISTIC` /
`RIGHT_CHARACTERISTIC` (the constants are unused documentation). Descriptor
writes are now serialized (one GATT write in flight), but any extra notify
characteristic the firmware exposes still costs bandwidth.

**To close:** confirm the actual notify characteristic UUID(s) from the
ESP32 firmware, then filter `queueNotifications` on them.

## TD-6 — Doctor pairing by QR scan

**Status:** deferred (needs learning — owner decision, 2026-10-02).
**Code:** `SettingsScreen` (MY DOCTOR section), `cloud/README.md` §4.

**Problem:** pairing is code-entry only (8-char token the doctor shares).
The planned UX is for the doctor web app to show a QR code the patient app
scans, which requires a camera/ML-kit scan flow the app doesn't have yet.

**To close:** add the ML-kit barcode scanner dependency, render the code as
a QR in the doctor web app, and add a "Scan QR" alternative in
`DoctorLinkCard`. The server-side contract does not change — the QR simply
encodes the same pairing code.

## TD-7 — Temperature/pressure history is not persisted (Trends is steps-only)

**Status:** deferred.
**Code:** `BleSensorDataSource` keeps only per-foot peaks in memory;
Room stores steps (daily/hourly) only.

**Problem:** the Trends tab can show real step history, but real
temperature trends and pressure exposure over days need persisted history
(min/max/avg per foot per day, episode log) that the app currently discards
between sessions. Until then, `cloud/DATA_CONTRACT.md`'s `temperature` and
`episodes` blocks can't be produced honestly for past days.

**To close:** add a Room table for daily foot summaries (per-foot min/max/
avg temp, peak pressure, wear minutes), populate it from the live flow
under the packet mutex, then (a) surface plain-word trends on the patient
Trends tab, (b) produce the full DATA_CONTRACT batches for upload.

## TD-8 — Prompt rules hard-coded in the ViewModel (IMPLEMENTED, 2026-10-03)

**Status:** executed. The rule evaluator moved to the domain layer:
`features/notifications/domain/PromptRuleEngine.kt` (pure, `Clock`-injected,
FakeClock-tested) owns persistence, cooldowns, and the no-data rule;
`ThresholdsConfig` is a versioned config loaded via
`ThresholdsConfigRepository` — cloud `GET /v1/config/thresholds` (served by
`cloud/server/config.go`), cached locally, falling back to bundled
`ThresholdsConfig.DEFAULT` ("v1"). `NotificationsViewModel` is now thin: it
feeds live inputs to the engine and maps semantic `DomainPrompt`s to copy;
every prompt carries `thresholdVersion`, which flows into the DATA_CONTRACT
`prompt_log`/`episodes` fields when the sync module lands.

**Remaining:** implement the remote fetch in
`ThresholdsConfigRemoteDataSource` once the app has an HTTP client (sync
module); until then the endpoint contract is documented and the app serves
the cached/bundled "v1" config. Publishing a new regime = update
`defaultThresholdsConfigJSON` in `config.go`, bump the version, get
clinical sign-off.

**Original problem:** the patient plan requires versioned, remotely
configurable thresholds ("configuration, not hard-coded") with clinical
sign-off; the values were compile-time constants and the rule evaluator
lived in the presentation layer.

## TD-9 — PlantarPressureCard hardcodes colors outside the palette

**Status:** deferred (cosmetic).
**Code:** `features/sensor/presentation/components/PlantarPressureCard.kt`.

**Problem:** a few `Color(0x…)` literals bypass the `DashboardColors`
palette locked in docs/design/UX-BRAND-SPEC.md. Purely a token-hygiene
issue; visual output is consistent.

**To close:** replace the literals with `DashboardColors` tokens (add new
official tokens to `DashboardTheme.kt` if a color has no equivalent).

## TD-10 — BLE payload contract v2 (FIX on both sides landed, 2026-10-03)

**Status:** code complete — flash + on-device test pending.
**Code:** `firmware/diabfoot_guard_sensor/diabfoot_guard_sensor.ino` +
`SensorPacketParser` / `BleSensorDataSource`.

**Resolved contract (owner-confirmed):** the 31-field serial line is
debug-only and never goes over the air. BLE carries two line types on one
notify characteristic: the **stream** `ax,ay,az,gx,gy,gz,p1,p2,p3,p4,p5`
(11 fields, ~5 Hz; MPU missing → six `0`s) and the **slow**
`T,<tempC>[,<batPct>]` (1/min; optional battery field). The app's parser
detects by structure, applies the zero-IMU guard (all-zero IMU → no step
counting) and skin-temperature range validation (−5…55 °C kills the `-999`
sentinel), and the stall watchdog is stamped by stream packets only.

**Remaining:** flash the left insole (flip `INSOLE_IS_LEFT`), confirm the
`Sensor unit profile locked: MPU6050_MS2_RADS` logline and live v2 lines,
then flash the right insole.

## TD-11 — FSR force values are uncalibrated estimates (design approved, bench PENDING)

**Status:** pending bench session (owner: needs a real-weight test).
**Design approved** — `docs/plans/TD3-TD4-PLAN.md` Part D, two layers:
Layer 1 (firmware, NVS): per-FSR 6-point LUT from a 0/2/5/10/20/30 kg bench
session replaces `estimateForceFromFSR`. Layer 2 (app, `CalibrationEngine`):
30-second stance self-calibration scaling gains to body weight + foot-size
area normalization, outputting a versioned `calibration_profile` stamped
onto DATA_CONTRACT batches. Until Layer 1 runs, every kPa value is a
device-scale estimate and the pressure threshold stays a labeled placeholder
(TD-4).

## TD-12 — Stream cadence mismatch (FIXED in firmware, 2026-10-03)

**Status:** closed in code. The sketch's loop is a `millis()` scheduler —
stream notify every 200 ms (5 Hz), temperature every 60 s, serial debug
every 500 ms — fully decoupled from the old `delay(500)`. FSR stream
sampling is lightened to 4×2 ms per sensor (~40 ms for all five + one MPU
read) to fit the 200 ms budget; the 20-sample averaging survives on the
serial-debug path. The app watchdog tuning (2 s) is valid again.
Verify at flash time that notify cadence holds under a connection.

## TD-13 — Firmware has no battery monitoring (design proposed, hardware PENDING)

**Status:** design approved, awaiting hardware.
**Design:** divider `VBAT — 100k — [GPIO5] — 100k — GND` (measure before
the regulator; ADC sees 1.5–2.1 V; ~21 µA drain — use 1M+1M for ~2 µA).
GPIO5 = ADC2, free because Wi-Fi is never used. Battery rides the slow
`T,` line as an optional third field (`T,<tempC>,<batPct>`, 30 s sample).
The sketch implements it behind `#define BATTERY_PIN (-1)` — flip to 5 and
rebuild after soldering. The app parser already accepts the 3-field line
and `DiagnosticsDataSource` will show the real percentage; until then it
renders "Unavailable" honestly. Runtime estimate: 300 mAh ≈ 4–6 h
continuous streaming at 5 Hz.
