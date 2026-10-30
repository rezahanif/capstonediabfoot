// Package main: Insole Cloud API — a dumb store for patient→doctor data.
//
// The server intentionally contains NO clinical processing: the patient app
// computes everything (see cloud/DATA_CONTRACT.md); the doctor web app
// fetches and displays. This server verifies Google sign-ins, manages the
// doctor→patient pairing, and stores versioned JSON payloads idempotently.
package main

import (
	"log"
	"net/http"
	"os"
)

type server struct {
	db           *DB
	jwtSecret    []byte
	googleAud    string // Android OAuth client ID (the mobile ID token's `aud`)
	googleWebAud string // Web OAuth client ID (the doctor web app's `aud`), optional
}

func getenv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

func main() {
	dbURL := getenv("DATABASE_URL", "")
	jwtSecret := getenv("JWT_SECRET", "")
	googleAud := getenv("GOOGLE_CLIENT_ID", "")
	googleWebAud := getenv("GOOGLE_WEB_CLIENT_ID", "")
	addr := getenv("ADDR", ":8080")

	if dbURL == "" || jwtSecret == "" || (googleAud == "" && googleWebAud == "" && os.Getenv("ALLOW_DEV_AUTH") != "true") {
		log.Fatal("DATABASE_URL, JWT_SECRET and GOOGLE_CLIENT_ID (and/or GOOGLE_WEB_CLIENT_ID) must be set")
	}

	db, err := NewDB(dbURL)
	if err != nil {
		log.Fatalf("database: %v", err)
	}
	defer db.Close()

	s := &server{db: db, jwtSecret: []byte(jwtSecret), googleAud: googleAud, googleWebAud: googleWebAud}

	mux := http.NewServeMux()
	// Auth
	mux.HandleFunc("POST /v1/auth/google", s.handleGoogleLogin)
	mux.HandleFunc("POST /v1/auth/login", s.handleEmailLogin)
	mux.HandleFunc("POST /v1/auth/register", s.handleEmailRegister)
	mux.Handle("GET /v1/me", s.auth(http.HandlerFunc(s.handleMe)))

	// Versioned thresholds config (TD-8) — static distribution, no processing
	mux.Handle("GET /v1/config/thresholds", s.auth(http.HandlerFunc(s.handleThresholdsConfig)))

	// Patient: doctor pairing
	mux.Handle("POST /v1/pairing/link", s.auth(s.role("patient", http.HandlerFunc(s.handlePairingLink))))
	mux.Handle("GET /v1/pairing/status", s.auth(s.role("patient", http.HandlerFunc(s.handlePairingStatus))))
	mux.Handle("DELETE /v1/pairing", s.auth(s.role("patient", http.HandlerFunc(s.handlePairingUnlink))))

	// Doctor: codes + patients
	mux.Handle("POST /v1/doctor/pairing-codes", s.auth(s.role("doctor", http.HandlerFunc(s.handleCreatePairingCode))))
	mux.Handle("GET /v1/doctor/patients", s.auth(s.role("doctor", http.HandlerFunc(s.handleListPatients))))
	mux.Handle("GET /v1/doctor/patients/{patientId}/uploads", s.auth(s.role("doctor", http.HandlerFunc(s.handlePatientUploads))))

	// Patient: dumb-store uploads (Idempotency-Key header)
	mux.Handle("POST /v1/uploads", s.auth(s.role("patient", http.HandlerFunc(s.handleUpload))))

	log.Printf("insole-cloud listening on %s", addr)
	log.Fatal(http.ListenAndServe(addr, corsMiddleware(mux)))
}
