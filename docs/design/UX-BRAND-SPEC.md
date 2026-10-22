# UX & Brand Spec — Patient App (post-POC)

**Status:** v1.0 · 2026-10-02
**Scope:** every page after sign-in. Landing / Login / Sign-up are excluded from this spec (they are pre-product marketing surfaces).
**Companion documents:** `patient-mobile-app-plan.md` (S1–S5 UX plan), `00-shared-sensing-data-contract.md` (who may see what), `docs/TECH_DEBT.md` (known gaps), `cloud/README.md` (backend).

---

## 1. The one rule

> **The patient should feel supported, not haunted.**

The app turns sensor data into three things: a calm status ("how am I doing?"),
an action ("what should I do?"), and immediate feedback ("what can I do right
now?"). Flow: **SEE → UNDERSTAND → ACT**.

Hard product rules that every screen must obey:

| # | Rule |
|---|---|
| R1 | No raw medical numbers to the patient — no ΔT values, no kPa, no scores, no probabilities. Steps are activity feedback (already shown on Home) and are allowed. |
| R2 | Every prompt **names the foot and the action** ("Check your left foot — look for redness, swelling, or a sore"). |
| R3 | A **single abnormal reading never creates a prompt** — persistence over time only. |
| R4 | Prompts are **rate-limited** (cooldown + snooze mindset) so support can never become nagging. |
| R5 | **"No recent data" is never shown as "All clear."** A silent failure must look like a failure. |
| R6 | Calm language only: "attention" must not look like "emergency". There is no red "critical" state in the patient app — that belongs to the doctor web app. |
| R7 | Data shown must be **real**. A screen that would show fabricated values shows an honest empty state instead. |

## 2. Brand system (locked to the Home tab)

The home tab is the single source of truth. Do not introduce new colors,
shapes, or type styles — extend `DashboardColors` with an official token if a
color is genuinely missing.

### Tokens (`core/theme/DashboardTheme.kt`)

| Token | Value | Use |
|---|---|---|
| `Background` | `#EBF1FF` | every page background |
| `Navy` | `#001633` | titles, primary text |
| `Brand` | `#114784` | primary blue — buttons, accents, LEFT badge |
| `StatusCard` | `#114784` | navy hero card background |
| `Green` / `GreenMint` / `GreenBar` | `#2EB88A` / `#10B981` / `#34D399` | connected / success / chart bars |
| `Yellow` | `#EAB308` | attention (the *only* "check" signal color) |
| `TextMuted` / `TextGray` / `TextLightGray` | `#44474E` / `#6B7280` / `#9CA3AF` | secondary text hierarchy |
| `CardBg` | `#FFFFFF` | all cards |
| `CardTintBlue` | `#F0F7FF` | highlighted card state |
| `SideRight` | `#E91E63` | RIGHT-insole accent |
| `Alert` | `#E53935` | destructive actions / disabled states only |
| `ProgressTrack` | `#E5E7EB` | empty bar/track |

### Grammar

- **Cards:** white, radius 20 (16 inside lists), elevation 2–4, padding 16–20.
- **Hero card:** navy `StatusCard`, radius 20, height ~151, white 11sp
  uppercase label + 30–32sp bold headline + 12–13sp calm body + colored icon
  circle. Pattern: `SystemStatusCard`.
- **Section labels:** 11sp, `TextMuted`, `ExtraBold`, 0.5sp letter-spacing,
  uppercase ("MY DOCTOR", "LAST 7 DAYS").
- **Buttons:** pill (radius 50) or radius 14, `Brand` background, bold 13sp
  white text.
- **Type:** no MaterialTheme typography mixing — sp values + weights as above.

## 3. Page-by-page audit (2026-10-02)

| Page | Brand | Data honesty | Verdict / action |
|---|---|---|---|
| Home (`DashboardScreen`) | ✅ reference | mixed: real sensor data; battery hardcoded `100` (placeholder), greeting hardcoded "Reza" | **Reference** — batteries/greeting need real sources later |
| Monitoring (`MonitoringScreen`) | ✅ follows | real | Keep. `PlantarPressureCard` hardcodes a few hexes (→ TD-9) |
| Pairing (`BleDevicePairingScreen`) | ✅ (3 hardcoded hexes → tokens `CardTintBlue`/`SideRight`/`Alert`) | real | Fixed in this pass |
| **Alerts** (`NotificationsScreen`) | ❌ was MaterialTheme defaults | ❌ was a no-op stub | **Rebuilt**: live guidance prompts + honest hero states (§4) |
| **Trends** (`TrendsScreen`) | ❌ was MaterialTheme defaults | ❌ was fabricated (score 85, "5000 steps") | **Rebuilt**: real Room step history, fake score removed (§5) |
| Settings (`SettingsScreen`) | ❌ was unbranded + **unreachable** | ❌ fake threshold sliders | **Rebuilt + wired** from Home avatar menu (§6, cloud/README §pairing) |
| Landing / Login / Sign-up | n/a | n/a | Out of scope per product decision |

## 4. Alerts tab — design

Hero states: **All clear** (green circle) · **Time to check** (yellow circle)
· **No recent data** (blue circle). Rule R5 enforced in code: a
disconnected/stale stream produces the third state, never the first.

Guidance prompts (white cards, one action area each):

| Prompt | Fires when | Copy source |
|---|---|---|
| Check your {left/right} foot | \|ΔT\| ≥ 2.2 °C **sustained 3+ evaluations**, 60 min cooldown | plan S2 |
| Your {foot} foot is taking more pressure | pressure ≥ 200, 30 min cooldown per foot | plan S3 |
| No recent data | either insole disconnected / no data | plan S5 |

Each prompt shows "Try:" bullets — concrete actions, never interpretation.
The notification-history list renders **only when non-empty** (the push
history data source is still a backend scaffold, TD-4).

Rationale for rule values: 2.2 °C is the clinically significant asymmetry
threshold (`SensorConstants.TEMPERATURE_DIFF_WARNING`); 200 is the existing
pressure warning level. Both are frozen constants today — moving them into
versioned remote config is TD-8.

## 5. Trends tab — design

- "This week" navy hero: real total steps + plain-language takeaway
  ("You moved on 5 of the last 7 days, with Tuesday your busiest").
- Last-7-days card: real daily steps from Room (`steps` table, 62-day
  retention) with `GreenBar` proportional bars, today highlighted.
- **The fabricated "Weekly Health Score" was removed** — R1/R7. A real score
  needs clinical sign-off and doctor-side computation; it may return in the
  doctor web app, never as a made-up number here.
- Temperature/pressure trends need history the app doesn't persist yet
  (only peaks) — TD-7. When that lands, they appear here as plain words
  ("Your feet have been steadily warmer on the left over 3 days") for the
  patient, full numbers for the doctor.

## 6. Settings screen — design

Reachable from the Home avatar menu (was previously unreachable).
Sections: **MY DOCTOR** (pairing code entry / linked doctor card),
**DEVICE** (insole pairing link), **ALERTS** (honest static explanation of
the clinical thresholds — they are not user sliders), **ABOUT**.

The fake pressure/temperature slider cards were removed (they wrote to a
stub and controlled nothing — R7).

## 7. Copy guidelines

| Do | Don't |
|---|---|
| "Your left foot has been warmer than your right for a while." | "Temperature asymmetry +2.6 °C" |
| "Please take a look for redness, swelling, or a sore." | "Possible Charcot activity" |
| "Try slowing down for a moment." | "Pressure exceeded 318 kPa" |
| "No recent data — reconnect your insoles" | (silence shown as "All clear") |
| Name the foot and the action | Show probabilities, scores, or diagnoses |
| Calm, supportive, short | Alarm language, red-alert phrasing |

### Threshold provenance

The pressure alert level (200) is a **device-scale placeholder pending FSR
calibration** (TD-11) — not a clinical value. The 2.2 °C inter-foot asymmetry
threshold is **literature-sourced** (JMIR mHealth 2025; IWGDF 2023). No
number is ever presented as clinical that isn't.
