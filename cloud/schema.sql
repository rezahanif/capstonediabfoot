-- Insole Cloud — PostgreSQL schema v1
-- Apply: psql "$DATABASE_URL" -f schema.sql
-- Requires PostgreSQL 13+ (gen_random_uuid() via pgcrypto or built-in on 13+).

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ─────────────────────────────────────────────────────────────────────────────
-- Users: one row per human. Google identity is stored ONLY here — every
-- clinical payload uses the pseudonymous pseudo_id from patient_profiles.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role          TEXT NOT NULL CHECK (role IN ('patient', 'doctor')),
    google_sub    TEXT NOT NULL UNIQUE,          -- stable Google identity
    email         TEXT,                          -- contact only, never in payloads
    display_name  TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ─────────────────────────────────────────────────────────────────────────────
-- Profiles
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE doctor_profiles (
    user_id          UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    clinic_name      TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE patient_profiles (
    user_id          UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    -- Pseudonymous ID used in ALL sensor payloads (never a name/email).
    pseudo_id        TEXT NOT NULL UNIQUE DEFAULT 'PT-' || upper(substr(md5(random()::text), 1, 8)),
    -- 1 doctor per patient, enforced here AND via doctor_patients UNIQUE below.
    doctor_user_id   UUID REFERENCES users(id) ON DELETE SET NULL,
    linked_at        TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ─────────────────────────────────────────────────────────────────────────────
-- Doctor → patients: 1 doctor : N patients. The UNIQUE on patient_id is the
-- hard constraint that a patient can never have two doctors.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE doctor_patients (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    patient_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    linked_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (patient_id)                          -- ← 1 doctor per patient
);
CREATE INDEX idx_doctor_patients_doctor ON doctor_patients (doctor_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- Pairing codes: doctor generates, patient enters. Single-use, 30-day expiry.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE pairing_codes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        TEXT NOT NULL UNIQUE,            -- 8 chars A-Z0-9, no ambiguous chars
    doctor_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ NOT NULL DEFAULT now() + INTERVAL '30 days',
    used_by     UUID REFERENCES users(id) ON DELETE SET NULL,  -- NULL = unused
    used_at     TIMESTAMPTZ
);
CREATE INDEX idx_pairing_codes_doctor ON pairing_codes (doctor_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- Devices: which insole pair a patient wears (for context in doctor views).
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE device_registry (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    left_address  TEXT,
    right_address TEXT,
    first_seen    TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_seen     TIMESTAMPTZ,
    UNIQUE (patient_id, left_address, right_address)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- Data uploads — the DUMB STORE. The patient app computes everything
-- (steps, temperature summaries, alert episodes) per DATA_CONTRACT.md and
-- uploads versioned JSON envelopes. The server never interprets them.
--
-- idempotency_key: the app retries offline syncs; the UNIQUE constraint makes
-- every retry a no-op instead of a duplicate medical record.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE data_uploads (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    idempotency_key  TEXT NOT NULL UNIQUE,       -- e.g. UUIDv4 generated per batch
    schema_version   TEXT NOT NULL,              -- payload contract version ("1.0")
    kind             TEXT NOT NULL CHECK (kind IN ('daily_batch')),
    payload          JSONB NOT NULL,             -- the whole DATA_CONTRACT envelope
    payload_from     TIMESTAMPTZ,                -- envelope.batch.period.start
    payload_to       TIMESTAMPTZ,                -- envelope.batch.period.end
    received_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_uploads_patient_time ON data_uploads (patient_id, payload_from DESC);

-- ─────────────────────────────────────────────────────────────────────────────
-- Audit log: every doctor read of patient data is recorded (medical-access
-- expectation). Patient appends are NOT audited (too noisy).
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE audit_log (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action       TEXT NOT NULL,                  -- 'READ_PATIENT_UPLOADS', …
    target_id    UUID,                           -- patient user id, if applicable
    detail       JSONB,
    at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_actor ON audit_log (actor_id, at DESC);
