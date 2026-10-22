# Patient → Doctor Data Contract

**Status:** v1.0 · 2026-10-02
**Producer:** patient Android app (`capstonediabfoot`)
**Store:** Insole Cloud (`schema.sql` → `data_uploads`, JSONB, dumb store)
**Consumer:** doctor web app (separate repo, future — processes for display)
**Derived from:** `00-shared-sensing-data-contract.md` (the full clinical
contract), adapted to the **current hardware**: 1 FSR pressure sensor + 1 NTC
temperature sensor per foot, BLE notifications at **5 Hz**.

---

## 1. Processing boundaries (agreed)

| Stage | What happens |
|---|---|
| **Firmware (shoe)** | High-frequency processing stays here: sampling and the 5 Hz notification stream. The sending cadence follows the firmware frequency — the app never asks for more or less. |
| **Patient app** | Parsing, step counting (FSM), per-minute temperature sampling, daily summaries, alert/prompt episodes. This is the ONLY processing stage. |
| **Cloud** | **Stores only.** Idempotent, versioned JSONB blobs. No rules, no aggregation, no interpretation. |
| **Doctor web app** | Fetches stored envelopes and processes them for clinical display (trends, numbers, locations — everything the patient app must not show). |

## 2. Envelope (the only upload format)

```jsonc
{
  "schema_version": "1.0",          // contract version — bump on any change
  "patient_id": "PT-3F9A2C1B",      // pseudonymous; NEVER name/email/Google id
  "device_id": "L:AA:BB:CC:DD:EE+R:11:22:33:44:55",  // paired insole addresses
  "calibration_version": "none",    // reserved: TD-11 Layer-2 profile version
                                    // ("none" until stance calibration exists)
  "generated_at": "2026-10-02T23:59:00Z",           // when the app built the batch
  "batch": {
    "period": {                      // the window this batch covers
      "start": "2026-10-02T00:00:00Z",
      "end":   "2026-10-02T23:59:00Z"
    },
    "wear_minutes": 512,             // minutes with live data from both feet
    "daily_activity": { /* §3 */ },
    "temperature": { /* §4 */ },
    "episodes": [ /* §5 */ ],
    "prompt_log": [ /* §6 */ ]
  }
}
```

Transport rules:
- `POST /v1/uploads` with header **`Idempotency-Key: <UUIDv4>`** (one per
  batch, generated once — retries after offline periods reuse the same key so
  the store deduplicates).
- One batch per patient-day, uploaded at the next opportunity (end of day,
  app foreground, or connectivity return). The 5 Hz stream itself is **never
  uploaded** — high-frequency handling belongs to the firmware, per §1.
- `schema_version` governs evolution: the doctor web app should accept any
  `1.x` and quarantine unknown majors.

## 3. `daily_activity`

Real data the app already computes and stores (Room `steps` table).

```jsonc
"daily_activity": {
  "total_steps": 4213,
  "left_steps": 2107,          // per-foot split where known
  "right_steps": 2106,
  "active_minutes_estimated": 46   // walking-state minutes (coarse, optional)
}
```

## 4. `temperature`

Current hardware has one NTC per foot, so "site" granularity is one per foot.
When the firmware gains more sensors, `sites` grows — the doctor app keys on
`site_id` (forward-compatible with the shared contract's site enum).

```jsonc
"temperature": {
  "left":  { "min_c": 30.1, "max_c": 33.8, "avg_c": 32.2, "samples": 512, "site_id": "MIDFOOT" },
  "right": { "min_c": 29.9, "max_c": 33.1, "avg_c": 31.6, "samples": 512, "site_id": "MIDFOOT" }
}
```

**Confirmed hardware region mapping** (owner's insole layout figure,
"Gambar 8.3" — plantar view): the 5 FSRs per foot are
**p1 = HALLUX, p2 = MTH1 (1st metatarsal head), p3 = MTH2 (2nd metatarsal
head), p4 = MTH4 (4th metatarsal head), p5 = HEEL (calcaneus)**; the single
NTC sits at **MIDFOOT** (hence `site_id: "MIDFOOT"` above). These five
regions are the `site_or_region` values used in `episodes` below; they map
into the shared contract's region enum as HALLUX / MTH1 / MTH2 / MTH4
(within its MTH2_4 super-region) / HEEL, with MIDFOOT unsensored.

## 5. `episodes` (what the doctor actually acts on)

Each episode records a **persistent** condition the patient app detected
(single readings never appear — persistence rules, threshold-versioned):

```jsonc
"episodes": [
  {
    "type": "THERMAL_ASYMMETRY",        // or "HIGH_PRESSURE"
    "foot": "L",                        // "L" | "R"
    "site_or_region": "MIDFOOT",        // HALLUX | MTH1 | MTH2 | MTH4 | HEEL | MIDFOOT
    "start": "2026-10-02T08:14:00Z",
    "end":   "2026-10-02T09:02:00Z",
    "duration_min": 48,
    "peak_value": 2.6,                  // ΔT °C for thermal; peak index for pressure
    "threshold_version": "v1"           // v1: ΔT ≥ 2.2 °C sustained; pressure ≥ 200
  }
]
```

## 6. `prompt_log` (doctor sees what the patient saw)

Every patient-facing prompt that actually fired (post-cooldown), so the
doctor can interpret the patient's behavior without re-deriving it:

```jsonc
"prompt_log": [
  {
    "prompt_id": "THERMAL_L",
    "shown_at": "2026-10-02T09:02:10Z",
    "threshold_version": "v1",          // version of the rule config that fired
    "related_episode_start": "2026-10-02T08:14:00Z"
  }
]
```

The `threshold_version` on every episode and prompt-log entry is the version
of the active thresholds config (`GET /v1/config/thresholds`), so the doctor
app can always tell which rule regime produced the data.

## 7. Kotlin reference (for the future sync module)

The producer side will be plain data classes serialized by the sync module
once the server is deployable — no clinical logic moves, this is mapping only:

```kotlin
data class UploadEnvelope(
    val schemaVersion: String,       // "1.0"
    val patientId: String,           // pseudo id from pairing
    val deviceId: String,
    val generatedAt: String,         // ISO-8601 UTC
    val batch: Batch,
)
data class Batch(
    val period: Period,
    val wearMinutes: Int,
    val dailyActivity: DailyActivity,
    val temperature: TemperatureBlock,
    val episodes: List<Episode>,
    val promptLog: List<PromptLogEntry>,
)
```

## 8. What the doctor web app may show (from the shared contract §8)

Numbers, locations, persistence and trends — yes. Diagnosis or probability
scores — never. The patient app shows none of this payload's numbers; the
doctor app may show all of it. That asymmetry is the product.
