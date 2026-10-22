# Insole Cloud — Patient ↔ Doctor Backend

**Status:** prep v1.0 · 2026-10-02
**Hosting:** your own server (PostgreSQL + the Go scaffold in `server/`)
**Principle:** the cloud **stores** data, it does not **process** it. The
patient app computes summaries/alerts (see `DATA_CONTRACT.md`); the doctor
web app fetches and processes for display. See `schema.sql` for the DDL.

---

## 1. Architecture

```text
 SHOE (ESP32, BLE) ──► PATIENT PHONE (app: parse → FSM/step count →
                        temperature/pressure summaries, alert episodes)
                              │  batches per DATA_CONTRACT.md (HTTPS, JWT)
                              ▼
                   YOUR GO API  (this folder /server)  ──►  PostgreSQL
                        │  dumb store: upsert JSONB blobs, pairing, auth
                        ▼
                   DOCTOR WEB APP (separate repo, not built yet)
                        fetches patient uploads, processes for display
```

Why a dumb store: no clinical logic on the server means no clinical logic to
certify, version, or debug there. Rules live in the patient app (versioned
via `threshold_version` in every payload) and in the doctor web app.

## 2. Auth — Google OAuth with your own server

You set up the Google OAuth client (Android + Web). The flow is **ID-token
verification**, not implicit browser flows:

1. Android app signs in with Google (Credential Manager) and gets an **ID token**.
2. App POSTs `{ id_token }` to `POST /v1/auth/google`.
3. Go server verifies signature + `aud` + expiry via
   `google.golang.org/api/idtoken` (`idtoken.Validate`).
4. Server upserts `users` by the Google `sub`, issues its **own JWT**
   (7-day expiry, HS256 with `JWT_SECRET` env) — the app never talks to
   Google again after sign-in.
5. App stores the JWT in **EncryptedSharedPreferences/Keystore** — this is
   what the existing `EncryptionManager` scaffold is for.

**Missed items checklist** (things easy to forget — all covered below/in schema):

| Item | Where |
|---|---|
| Idempotent offline sync (retries must not duplicate rows) | `data_uploads.idempotency_key UNIQUE` + `Idempotency-Key` header |
| Pseudonymous patient identity — no emails/names in sensor payloads | `patient_profiles.pseudo_id`; payloads carry `patient_id` only |
| No PHI in push notifications (future) | transport-only note; payloads never contain free-text health data |
| Doctor access audit trail | `audit_log` on every doctor read |
| Refresh/expiry: 7-day JWT + re-login on 401 (simple for v1) | auth.go |
| Retention & deletion (account delete ⇒ payloads deleted) | `data_uploads ON DELETE CASCADE` |
| Rate limiting (per token) | TODO in server scaffold (`middleware` stub) |
| TLS only | terminate at your reverse proxy (Caddy/Nginx); HTTP never exposed |
| API versioning | everything under `/v1` |
| Threshold versioning | every episode in the payload carries `threshold_version` |
| Backups | `pg_dump` cron on your server (documented, not coded) |

## 3. Database (PostgreSQL)

Chosen over SQLite/Mongo because: relational integrity for the
doctor→patient binding (`UNIQUE(patient_id)` is what enforces
*1 doctor per patient*), `JSONB` for the dumb-store payloads without schema
churn, `TIMESTAMPTZ` everywhere (no timezone bugs), row-level policies can
be added later. Apply with `psql -f schema.sql`.

Key tables: `users` (patient|doctor) · `patient_profiles` · `doctor_profiles`
· `doctor_patients` (the 1:N relation) · `pairing_codes` (single-use,
30-day expiry) · `device_registry` · `data_uploads` (JSONB blobs) ·
`audit_log`.

## 4. Doctor ↔ patient pairing (token method)

```
DOCTOR (web app)                        PATIENT (Settings → My Doctor)
────────────────                        ──────────────────────────────
POST /v1/doctor/pairing-codes
  → generates 8-char code, 30-day expiry, single-use
        │  shares code out-of-band (visit / SMS)
        └──────────────────────────────► patient enters code
                                         (app stores locally, "pending")
                                         POST /v1/pairing/link {code}
                                           → server validates, inserts
                                             doctor_patients (UNIQUE patient)
                                           → marks code used
patient sees "linked" on next status fetch ◄── GET /v1/pairing/status
```

Rules: a patient can link **exactly one** doctor (DB-enforced); relinking
requires unlinking first (server: DELETE /v1/pairing, or doctor-side
removal). The doctor name is returned by `/v1/pairing/status` — until the
server is live the Android Settings screen shows "pending confirmation"
honestly. QR-scan pairing (doctor shows QR containing the code) is deferred —
**TD-6** in `docs/TECH_DEBT.md`.

## 5. Endpoints (scaffold implements these)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/v1/auth/google` | none | verify Google ID token → app JWT |
| GET | `/v1/me` | JWT | who am I (role, pseudo id) |
| GET | `/v1/config/thresholds` | JWT | versioned prompt-thresholds config (TD-8, static JSON in `config.go`) |
| POST | `/v1/pairing/link` | patient | link entered code to this patient |
| GET | `/v1/pairing/status` | patient | linked doctor info |
| DELETE | `/v1/pairing` | patient | unlink doctor |
| POST | `/v1/doctor/pairing-codes` | doctor | generate a pairing code |
| GET | `/v1/doctor/patients` | doctor | list linked patients |
| POST | `/v1/uploads` | patient | dumb-store upload (`Idempotency-Key`) |
| GET | `/v1/doctor/patients/{patientId}/uploads` | doctor | fetch patient payloads (audited) |

## 6. Running the scaffold

```bash
cd cloud/server
cp .env.example .env         # fill DATABASE_URL, JWT_SECRET, GOOGLE_CLIENT_ID
psql "$DATABASE_URL" -f ../schema.sql
go run .
# API listens on :8080 (behind your TLS reverse proxy)
```

## 7. What is intentionally NOT here

- Doctor web app (separate repo, later).
- Any aggregation/analytics SQL — the payloads are already processed.
- Push notifications, QR pairing (TD-6), payment/billing, FHIR anything.
