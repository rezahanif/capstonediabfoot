package main

import (
	"encoding/json"
	"io"
	"net/http"
	"time"
)

// The dumb store: accept the versioned envelope from cloud/DATA_CONTRACT.md,
// store it verbatim as JSONB, deduplicate on the Idempotency-Key header.
// No field is interpreted beyond the few needed for indexing.

type uploadEnvelope struct {
	SchemaVersion string `json:"schema_version"`
	PatientID     string `json:"patient_id"` // must match the token's owner
	GeneratedAt   string `json:"generated_at"`
	Batch         struct {
		Period struct {
			Start string `json:"start"`
			End   string `json:"end"`
		} `json:"period"`
	} `json:"batch"`
}

type uploadResponse struct {
	Stored       bool `json:"stored"` // false = idempotent replay, already stored
	Deduplicated bool `json:"deduplicated"`
}

const maxPayloadBytes = 2 << 20 // 2 MiB per batch envelope

func (s *server) handleUpload(w http.ResponseWriter, r *http.Request) {
	idemKey := r.Header.Get("Idempotency-Key")
	if idemKey == "" {
		http.Error(w, "missing Idempotency-Key header", http.StatusBadRequest)
		return
	}

	body, err := io.ReadAll(http.MaxBytesReader(w, r.Body, maxPayloadBytes))
	if err != nil {
		http.Error(w, "payload too large or unreadable", http.StatusRequestEntityTooLarge)
		return
	}

	var env uploadEnvelope
	if err := json.Unmarshal(body, &env); err != nil {
		http.Error(w, "invalid json envelope", http.StatusBadRequest)
		return
	}
	if env.SchemaVersion == "" {
		http.Error(w, "missing schema_version", http.StatusBadRequest)
		return
	}
	if env.PatientID != "" && env.PatientID != userIDFrom(r) {
		// Payloads are pseudonymous; the patient_id inside the envelope must
		// never contradict the authenticated user.
		http.Error(w, "envelope patient_id does not match authenticated user", http.StatusForbidden)
		return
	}

	parseTime := func(s string) *time.Time {
		if s == "" {
			return nil
		}
		if t, err := time.Parse(time.RFC3339, s); err == nil {
			return &t
		}
		return nil
	}
	from := parseTime(env.Batch.Period.Start)
	to := parseTime(env.Batch.Period.End)

	inserted, err := s.db.InsertUpload(
		r.Context(), userIDFrom(r), idemKey, env.SchemaVersion, "daily_batch", body, from, to)
	if err != nil {
		http.Error(w, "db error", http.StatusInternalServerError)
		return
	}
	writeJSON(w, http.StatusCreated, uploadResponse{Stored: inserted, Deduplicated: !inserted})
}
