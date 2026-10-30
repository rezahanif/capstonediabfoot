package main

import (
	"encoding/json"
	"net/http"
	"time"
)

// ── patient side ────────────────────────────────────────────────────────────

type linkRequest struct {
	Code string `json:"code"`
}

type pairingStatusResponse struct {
	Linked     bool   `json:"linked"`
	DoctorName string `json:"doctor_name,omitempty"`
}

// handlePairingLink binds the entered code to the authenticated patient.
// Offline-first in the app: the phone stores the code locally first; this
// server call creates the real, enforced doctor→patient binding.
func (s *server) handlePairingLink(w http.ResponseWriter, r *http.Request) {
	var req linkRequest
	if err := decodeJSON(w, r, &req); err != nil {
		return
	}
	doctorName, err := s.db.LinkPairingCode(r.Context(), userIDFrom(r), req.Code)
	if notFoundOr(w, err, "unknown pairing code") {
		return
	}
	writeJSON(w, http.StatusOK, pairingStatusResponse{Linked: true, DoctorName: doctorName})
}

func (s *server) handlePairingStatus(w http.ResponseWriter, r *http.Request) {
	doctorName, linked, err := s.db.PairingStatus(r.Context(), userIDFrom(r))
	if err != nil {
		http.Error(w, "db error", http.StatusInternalServerError)
		return
	}
	writeJSON(w, http.StatusOK, pairingStatusResponse{Linked: linked, DoctorName: doctorName})
}

func (s *server) handlePairingUnlink(w http.ResponseWriter, r *http.Request) {
	if err := s.db.UnlinkDoctor(r.Context(), userIDFrom(r)); err != nil {
		http.Error(w, "db error", http.StatusInternalServerError)
		return
	}
	writeJSON(w, http.StatusOK, map[string]bool{"linked": false})
}

// ── doctor side ─────────────────────────────────────────────────────────────

type codeResponse struct {
	Code      string `json:"code"`
	ExpiresIn int    `json:"expires_in_days"`
}

func (s *server) handleCreatePairingCode(w http.ResponseWriter, r *http.Request) {
	code, err := s.db.CreatePairingCode(r.Context(), userIDFrom(r))
	if err != nil {
		http.Error(w, "could not create code", http.StatusInternalServerError)
		return
	}
	writeJSON(w, http.StatusOK, codeResponse{Code: code, ExpiresIn: 30})
}

type patientRow struct {
	PatientID string `json:"patient_id"`
	PseudoID  string `json:"pseudo_id"`
	LinkedAt  string `json:"linked_at"`
}

func (s *server) handleListPatients(w http.ResponseWriter, r *http.Request) {
	rows, err := s.db.DoctorPatients(r.Context(), userIDFrom(r))
	if err != nil {
		http.Error(w, "db error", http.StatusInternalServerError)
		return
	}
	out := make([]patientRow, 0, len(rows))
	for _, row := range rows {
		linked, _ := row["linked_at"].(time.Time)
		pseudo, _ := row["pseudo_id"].(string)
		id, _ := row["patient_id"].(string)
		out = append(out, patientRow{
			PatientID: id,
			PseudoID:  pseudo,
			LinkedAt:  linked.Format(time.RFC3339),
		})
	}
	writeJSON(w, http.StatusOK, out)
}

func (s *server) handlePatientUploads(w http.ResponseWriter, r *http.Request) {
	patientID := r.PathValue("patientId")
	if patientID == "" {
		http.Error(w, "missing patientId", http.StatusBadRequest)
		return
	}
	payloads, err := s.db.DoctorPatientUploads(r.Context(), userIDFrom(r), patientID, 100)
	if notFoundOr(w, err, "patient not linked to you") {
		return
	}
	if payloads == nil {
		payloads = []json.RawMessage{}
	}
	writeJSON(w, http.StatusOK, payloads)
}
