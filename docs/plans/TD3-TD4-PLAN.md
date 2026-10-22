# TD-3 + TD-4 Plan — UI→Domain Migration (5-FSR ready) & Data Classification

**Status:** approved plan v1.0 · 2026-10-03
**Trigger:** new firmware sketch (ESP32-C3 + 5×FSR on GPIO0–4 + ADS1115 NTC +
MPU6050, Adafruit libraries). BLE streaming section not yet re-injected.
**Related:** `docs/TECH_DEBT.md` TD-2/3/4 and new TD-10…TD-13.

---

## PART A — What the new firmware resolves (and what it breaks)

### A1. TD-2 is answered by firmware source: units are m/s² and rad/s

The sketch uses the **Adafruit MPU6050 library** (`mpu.getEvent`), which
outputs acceleration in **m/s²** and gyro in **rad/s** — exactly the README
example (`0.1,-9.8,0.3`). The FSM's g/deg/s comment was the wrong doc, not
the README. **No app change is needed**: the `SensorUnitDetector` (TD-2
mitigation) will auto-detect m/s² at session start and lock
`MPU6050_MS2_RADS`, converting accel ÷9.80665 and gyro ×57.2958 before the
FSM sees anything. The first on-device session only needs to confirm the
`Sensor unit profile locked: MPU6050_MS2_RADS` logline.

Side notes from the sketch: accelerometer range ±8 g and gyro ±500 deg/s are
ample for gait; the 21 Hz filter band is fine for step content (<5 Hz);
temperature now comes from a dedicated ADS1115 NTC rather than the MPU's
die temperature.

### A2. TD-3's blocker is removed: real per-zone pressure data exists

The firmware produces **5 FSR pressure values in kPa per foot** (voltage →
resistance → conductance → force → kPa). That is the "real per-zone data"
the pressure map was waiting for — the UI's fixed-weight distribution of a
single FSR becomes obsolete once this streams over BLE.

### A3. New flaws/debts the sketch opens

| # | Finding | Severity |
|---|---|---|
| **TD-10** | **BLE payload contract is now undefined.** The serial line is 31 fields (`time_ms`, per-FSR v/r/force/pressure ×5, NTC v/r/temp, then mpu status + 6 IMU + mpu temp). The app's parser expects the legacy 8-field line `ax,ay,az,gx,gy,gz,pressure,temp`. If BLE re-injection copies the serial format, `fromBleString` will silently **misparse** — field 0 is `time_ms`, field 6 is fsr2 *voltage*; both are "valid floats", so strict parsing won't even reject the line. | **High** |
| **TD-10a** | **Zero-IMU → phantom walking.** When the MPU is missing, the firmware prints `ax..gz = 0`. The app computes `gravityRemoved = |0 − 1.0| = 1.0` → `accelMag ≈ 1.0` ≫ MOTION_START → the FSM walks and counts steps from a device with no IMU. | **High** |
| **TD-11** | **FSR values are uncalibrated estimates.** `estimateForceFromFSR` is a piecewise conductance approximation (÷80, ÷30 µS→N — not the Interlink curve), and `FSR_AREA_M2 = 7.85e-5 m²` (1 cm²) is an assumption. The kPa outputs are placeholders. | Medium |
| **TD-12** | **Cadence mismatch.** Loop cost ≈ 250 ms of sampling (5 FSR × 20 samples × 2 ms + ADS 10 × 5 ms) + `delay(500)` → ≈ 1.2–1.3 Hz effective, far from the 5 Hz the app assumes. The stall watchdog (`STALE_PACKET_TIMEOUT_MS = 2000`) was sized for ~5 Hz; a BLE stream mirroring this loop rate would false-fire it constantly. | Medium |
| **TD-13** | **No battery monitoring exists in the firmware** — no battery pin, no divider, no reading. Every battery UI number remains fabricated until hardware adds it. | Low |
| — | NTC failure prints `temp_ntc = -999` — a valid float that passes the app parser (it rejects NaN/Inf only). Skin-temperature range validation needed. | folded into TD-10 |
| — | `mpu_status` prints `MPU_NOT_FOUND` (non-numeric) inside an otherwise numeric line. | folded into TD-10 |
| — | `mpu_tempC` is printed but unused by the app. | no action |

### A4. The one firmware decision required: the BLE line format (v2)

The serial format is debug-grade (raw voltage/resistance/force *and*
derived pressure — redundant over the radio). Recommended **compact BLE v2
line** (12 fields, IMU-first so ordering is recognizable):

```
ax,ay,az,gx,gy,gz,p1,p2,p3,p4,p5,tempC
```

- `p1…p5` = FSR pressures in kPa, **fixed zone order** (Part B, Phase 1).
- `time_ms`, voltages, resistances, forces, mpu temp: dropped — the app
  stamps arrival time itself and recomputes nothing from them.
- `mpu_status` dropped: absence of an IMU is conveyed by the zero-IMU guard
  (TD-10a). The failing branch already prints six `0`s, so the field count
  stays 12 regardless of MPU state.
- Length ≈ 60–80 bytes → fits the existing 512-byte MTU with room.
- Cadence (TD-12) belongs to whoever injects the BLE loop: recommend
  notifying every ~200 ms (≈5 Hz) independent of the 500 ms serial debug
  delay, so the app's watchdog tuning stays valid.

---

## PART B — TD-3 migration plan: business logic out of the UI, 5-FSR ready

Principle: the UI never parses protocol strings again. The data layer owns
parsing + validation; the domain layer owns zone semantics; the ViewModel
owns state; the card only renders what it's given.

### Phase 1 — Domain: packet model v2 + parser

1. **`PlantarZone` enum** (domain): `HEEL, MIDFOOT, MEDIAL_FOREFOOT,
   LATERAL_FOREFOOT, TOES` + a documented **`FSR_ZONE_ORDER`** contract
   constant mapping `p1…p5` → zones. ⚠️ *The physical placement table is the
   one thing only the owner can fill in — the plan assumes `p1=heel …
   p5=toes` until the insole layout is confirmed.*
2. **`SensorPacketParser`** (replaces direct `SensorPacket.fromBleString`
   call sites; the data class stays):
   - Detects format by field count: **8 fields = legacy v1** (current
     firmware), **12 fields = v2** (5-FSR line above) — both parse to the
     same model.
   - `SensorPacket` gains `pressuresKpa: List<Float>` (1 entry legacy, 5
     entries v2) and `imuValid: Boolean`.
   - **Zero-IMU guard (TD-10a):** all six IMU fields exactly 0 →
     `imuValid = false` → the step-count path skips the packet (pressure and
     temperature still flow). A genuinely resting foot always reports ≈ ±1 g
     on some axis, so all-zero ⇒ invalid.
   - **Range validation:** temperature outside −5…55 °C (catches the NTC
     `-999` sentinel), any pressure < 0 → reject packet, log.
   - Unit normalization (`SensorUnitDetector`) stays exactly where it is —
     after the parser, before the FSM.
3. Unit tests: v1 parse, v2 parse, all-zero-IMU flag, −999 temp rejection,
   negative pressure rejection, mixed legacy/v2 streams.

### Phase 2 — Domain: zone distribution use case

1. Rewrite `MapPressureToGridUseCase`: input `List<Float>` of 5 zone kPa
   values → a 5×10 grid where each zone owns a fixed row-band of the plantar
   map (heel = bottom band … toes = top band) with per-cell intensity. The
   dead 2-value logic and the `y<4`/`y<7` duplicate branch die.
2. The use case becomes genuinely invoked: `SensorViewModel` calls it and
   exposes `PressureMapState` (per-foot grid + a packet-sequence key for
   recomposition). If the ViewModel pass proves unnecessary, the mapping
   moves to the data source and the unused injection is removed — decision
   falls out of Phase 3.

### Phase 3 — UI: `PlantarPressureCard` stops parsing

1. Delete the card's internal `parsePressure` CSV splitting and the
   fixed-weight 5-zone distribution entirely.
2. The card renders `PressureMapState` from the ViewModel: 5 real zone
   intensities per foot (v2) — or, while legacy firmware still streams,
   the single total pressure rendered as an **un-zoned map with a "5-zone
   view requires firmware v2" caption** instead of a fabricated
   distribution (brand spec rule R7: no fake data).
3. Aggregation: `InsoleSensorData` gains `leftZonePressuresKpa /
   rightZonePressuresKpa`; `BleSensorDataSource.aggregateSensorData()`
   populates them under the existing packet mutex. Peak-pressure tracking
   switches from the single `pressure` field to per-zone peaks where v2 is
   available.

### Phase 4 — Firmware touchpoint (owner action, one line of contract)

Re-inject BLE streaming with the v2 12-field line at ~5 Hz notify cadence.
Until then, nothing in the app changes behavior — v1 keeps working through
the same parser. Commit the firmware sketch into the repo (e.g. `firmware/`)
so the contract has a source of truth.

---

## PART C — TD-4: what gets encrypted, what gets computed, what gets believed

### C1. Data classification (the "which data is encrypted" decision, recorded)

Payload format today: **CSV over BLE, JSON (JSONB) to the cloud** — plain
numbers either way. Decision (no over-engineering):

| Data class | In transit | At rest (server) | On device |
|---|---|---|---|
| **Patient profile** (identity, email, doctor link) | TLS | Encrypted at rest / DB-level protection; identity is the linkability key | **Encrypted via Keystore** — this is `EncryptionManager`'s real home (AES-256-GCM with random IV, already built); pairing code + profile live in EncryptedSharedPreferences |
| **Sensor summaries** (steps, temps, episodes — JSONB) | TLS | **Plain JSONB.** Raw numbers mean nothing without the app's engine; the pseudonymous `patient_id` carries all linkability and sits in the protected profile class | Plain local DB (Room) |
| **Usage data** (wear time, prompt log) | TLS | Plain JSONB, same reasoning | Plain |
| **Raw 5 Hz stream** | Never leaves the device | n/a | In-memory only |

Justification: app-level encryption of summary numbers adds key management
(key exchange with a dumb store, rotation, recovery) while defending
against a threat the model doesn't have — an attacker who can already read
the PostgreSQL or TLS stream gets identities and linkage, not step counts.
If a compliance audit later disagrees, the `EncryptionManager` +
`DataExportManager` scaffold already defines the encrypted-CSV export path;
flipping summary payloads to it is additive, not a rewrite.

### C2. Real data sources — the remaining fabrication, item by item

| Stub | Fate |
|---|---|
| `TrendsDataSource.getDailySummary` (hardcoded 5000 steps) + `getWeeklyHealthScore` (85f) + `DailySummary` | **Delete** — unreferenced since the Trends rebuild; fabricated data should not survive as callable code. `fetchSensorDataByTimeframe` stays as the honest remote-fetch scaffold |
| `DiagnosticsDataSource.getDeviceStatus` (battery 85, RSSI −65) | **Replace with real values**: RSSI is genuinely available (last scan result / `readRSSI()` on the connected GATT). Battery → **"unavailable"** honest state until firmware adds it (TD-13). No screen displays this module yet, so this is groundwork, not UI work |
| `SupabaseDataSource`, `NotificationDataSource` | Keep as no-op transport scaffolds (documented) — they fabricate nothing displayable |
| `EncryptionManager`, `DataExportManager`, `BleSecurityHandler` | Keep — now with an assigned job (C1 profile encryption + future export) |
| Diagnostics screens showing fabricated data | None exist in the UI today; brand spec rule R7 keeps it that way: a value that can't be computed shows "unavailable", never a placeholder |

### C3. Threshold provenance — no invented numbers

| Threshold | Source | Status |
|---|---|---|
| ΔT ≥ **2.2 °C** | Literature (JMIR mHealth 2025 once-per-minute plantar cohort; IWGDF 2023 — cited in `00-shared-sensing-data-contract.md` §11) | **Keep** — clinically grounded |
| Pressure ≥ **200** | **No source.** Also device-relative: built on uncalibrated conductance→kPa estimates (TD-11) | **Relabel as device-scale placeholder**: comment in `SensorConstants` + UX spec note — "alert level pending FSR calibration; not a clinical value". No new pressure threshold is invented until calibration data exists |
| Motion/step thresholds (0.05/0.12/0.15 g) | Empirically tuned in the POC (owner validation) | Keep, documented as POC-tuned |
| Persistence rules (3 evaluations, 60/30-min cooldowns) | Product decision (alarm-fatigue rule from the patient plan MD) | Keep, documented as product-tuned |

Net effect: every number the app acts on is either literature-sourced,
POC-validated by the owner, or explicitly labeled a calibration placeholder —
and nothing is presented as clinical that isn't.

---

## PART D — Calibration model (TD-11, approved design — implementation PENDING)

Two layers, split along the hardware/user boundary. **Both pending**: Layer 1
needs a bench session with real weights; Layer 2 is app work scheduled after
Layer 1 exists (stance normalization on uncalibrated estimates is possible
but premature).

### Layer 1 — device bench calibration (firmware, NVS)

Per-FSR curve fit replacing the ÷80/÷30 conductance guess:

1. Bench session with known masses on a small plate per sensor:
   0 / 2 / 5 / 10 / 20 / 30 kg → record voltage/conductance at each step.
2. Fit a 6-point LUT (piecewise-linear, Interlink-style conductance curve)
   per sensor — not one global curve; sensors vary unit to unit.
3. Store per-sensor coefficients in NVS (`Preferences.h`); firmware
   interpolates the LUT → kPa at stream time.
4. The firmware sketch already carries `#define BATTERY_PIN` and the
   calibration notes; the LUT replaces `estimateForceFromFSR` when data
   exists. Until then every kPa value is a device-scale estimate and the
   app's pressure threshold stays labeled as a placeholder (Part C3).

### Layer 2 — user normalization (app, `CalibrationEngine` domain module)

Person differences live with the user profile, not the hardware:

1. **Stance self-calibration** (one-time, ~30 s): user stands still wearing
   both insoles → scale per-sensor gains so Σforce ≈ body weight from the
   profile. Corrects foot-size/placement variation between users.
2. **Foot-size input** (EU size / length in cm): scales contact area for
   kPa/PTI interpretation (pressure = force/area).
3. Output: a versioned `calibration_profile` (per user + device pair),
   stamped as `calibration_version` onto every DATA_CONTRACT batch so the
   doctor app knows the provenance of the numbers it processes.

### Division of labor rationale

Sensor nonlinearity is a hardware property → fixed at the hardware boundary
(Layer 1, one bench session per device). Person differences are user state →
live in the app next to the user profile (Layer 2, re-runnable any time the
user's weight or footwear changes). Neither layer invents clinical
thresholds — see Part C3.

### Firmware evolution decisions recorded (2026-10-03)

- **50 Hz**: the shared contract's 50 Hz pressure requirement is for
  per-step waveform features (peak/PTI/contact per region) consumed only by
  the future doctor web app. v2.0 ships at 5 Hz (FSM minimum); the v2.1 path
  is on-chip edge processing — firmware samples FSRs at 50 Hz and emits
  per-step *features* (~1–2 Hz of tiny lines), not 50 Hz of raw radio.
- **Battery (TD-13)**: rides the slow `T,` line as an optional third field
  (`T,<tempC>,<batPct>`); divider 100k/100k from VBAT (pre-regulator) into
  GPIO5 (ADC2 — free, since Wi-Fi is never used); implemented in the sketch
  behind `#define BATTERY_PIN (-1)`, disabled until the divider is wired.
  300 mAh ≈ 4–6 h continuous streaming at 5 Hz.

---

## STATUS (2026-10-03)

- Part B Phase 1–3 + Part C2/C3: **implemented** (app) — see commit and
  `TECH_DEBT.md` TD-3/TD-4.
- Firmware v2 BLE section: **written** — `firmware/diabfoot_guard_sensor/`
  (flash + on-device test pending; side `#define` must be flipped per insole).
- Part D: **design recorded, implementation pending** (bench session first).
- Owner confirmations used: zone mapping S1=HALLUX…S5=HEEL (Gambar 8.3),
  temperature stream separated at 1/min, battery 3.3 V / 300 mAh design.
