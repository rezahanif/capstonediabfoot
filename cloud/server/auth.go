package main

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"google.golang.org/api/idtoken"
)

// ── context keys ────────────────────────────────────────────────────────────

type ctxKey int

const (
	ctxUserID ctxKey = iota
	ctxRole
)

func userIDFrom(r *http.Request) string { return r.Context().Value(ctxUserID).(string) }
func roleFrom(r *http.Request) string   { return r.Context().Value(ctxRole).(string) }

// ── middleware ──────────────────────────────────────────────────────────────

func (s *server) auth(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		raw := strings.TrimPrefix(r.Header.Get("Authorization"), "Bearer ")
		if raw == "" {
			http.Error(w, "missing bearer token", http.StatusUnauthorized)
			return
		}
		token, err := jwt.Parse(raw, func(t *jwt.Token) (any, error) {
			return s.jwtSecret, nil
		}, jwt.WithValidMethods([]string{"HS256"}))
		if err != nil || !token.Valid {
			http.Error(w, "invalid or expired token", http.StatusUnauthorized)
			return
		}
		claims, ok := token.Claims.(jwt.MapClaims)
		if !ok {
			http.Error(w, "invalid claims", http.StatusUnauthorized)
			return
		}
		sub, _ := claims["sub"].(string)
		role, _ := claims["role"].(string)
		if sub == "" || role == "" {
			http.Error(w, "invalid claims", http.StatusUnauthorized)
			return
		}
		ctx := context.WithValue(r.Context(), ctxUserID, sub)
		ctx = context.WithValue(ctx, ctxRole, role)
		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

func (s *server) role(role string, next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if roleFrom(r) != role {
			http.Error(w, "forbidden", http.StatusForbidden)
			return
		}
		next.ServeHTTP(w, r)
	})
}

// ── Authentication (Google, Email/Password, Dev) ───────────────────────────

type authRequest struct {
	IDToken string `json:"id_token"`
}

type emailLoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type registerRequest struct {
	Email       string `json:"email"`
	Password    string `json:"password"`
	DisplayName string `json:"display_name,omitempty"`
}

type authResponse struct {
	Token       string `json:"token"`
	Role        string `json:"role"`
	PseudoID    string `json:"pseudo_id,omitempty"`
	DisplayName string `json:"display_name,omitempty"`
}

// handleEmailLogin authenticates patient or doctor via email and bcrypt password.
func (s *server) handleEmailLogin(w http.ResponseWriter, r *http.Request) {
	var req emailLoginRequest
	if err := decodeJSON(w, r, &req); err != nil {
		return
	}
	email := strings.TrimSpace(strings.ToLower(req.Email))
	if email == "" || req.Password == "" {
		http.Error(w, "email and password are required", http.StatusBadRequest)
		return
	}

	userID, role, pseudoID, displayName, err := s.db.AuthenticateEmailUser(r.Context(), email, req.Password)
	if errors.Is(err, errInvalidCredentials) {
		http.Error(w, "invalid email or password", http.StatusUnauthorized)
		return
	}
	if err != nil {
		http.Error(w, "database error", http.StatusInternalServerError)
		return
	}

	signed, err := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"sub":  userID,
		"role": role,
		"exp":  time.Now().Add(7 * 24 * time.Hour).Unix(),
	}).SignedString(s.jwtSecret)
	if err != nil {
		http.Error(w, "token issue failed", http.StatusInternalServerError)
		return
	}

	writeJSON(w, http.StatusOK, authResponse{
		Token:       signed,
		Role:        role,
		PseudoID:    pseudoID,
		DisplayName: displayName,
	})
}

// handleEmailRegister registers a new patient account with bcrypt password.
func (s *server) handleEmailRegister(w http.ResponseWriter, r *http.Request) {
	var req registerRequest
	if err := decodeJSON(w, r, &req); err != nil {
		return
	}
	email := strings.TrimSpace(strings.ToLower(req.Email))
	if !strings.Contains(email, "@") || len(req.Password) < 6 {
		http.Error(w, "valid email and password of at least 6 characters required", http.StatusBadRequest)
		return
	}

	userID, role, pseudoID, err := s.db.RegisterUser(r.Context(), email, req.Password, strings.TrimSpace(req.DisplayName))
	if errors.Is(err, errUserExists) {
		http.Error(w, "an account with this email already exists", http.StatusConflict)
		return
	}
	if err != nil {
		http.Error(w, "registration failed: "+err.Error(), http.StatusInternalServerError)
		return
	}

	signed, err := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"sub":  userID,
		"role": role,
		"exp":  time.Now().Add(7 * 24 * time.Hour).Unix(),
	}).SignedString(s.jwtSecret)
	if err != nil {
		http.Error(w, "token issue failed", http.StatusInternalServerError)
		return
	}

	displayName := req.DisplayName
	if displayName == "" {
		displayName = strings.Split(email, "@")[0]
	}

	writeJSON(w, http.StatusCreated, authResponse{
		Token:       signed,
		Role:        role,
		PseudoID:    pseudoID,
		DisplayName: displayName,
	})
}

// handleGoogleLogin verifies a Google ID token (signature, audience, expiry)
// and issues the app's own JWT. New sign-ups default to the patient role;
// promoting a user to doctor is a manual DB step for v1.
func (s *server) handleGoogleLogin(w http.ResponseWriter, r *http.Request) {
	var req authRequest
	if err := decodeJSON(w, r, &req); err != nil {
		return
	}

	var sub, email, name string
	if os.Getenv("ALLOW_DEV_AUTH") == "true" && (req.IDToken == "dev-doctor" || req.IDToken == "dev-patient" || req.IDToken == "dev-admin" || req.IDToken == "dev-reza" || req.IDToken == "dev-elka") {
		if req.IDToken == "dev-doctor" || req.IDToken == "dev-admin" || req.IDToken == "dev-elka" {
			sub = "dev-doctor-elka-sub"
			email = "elka@diabfoot.local"
			name = "Dr. Elka"
		} else {
			sub = "dev-patient-reza-sub"
			email = "reza@diabfoot.local"
			name = "Reza"
		}
	} else {
		payload, err := idtoken.Validate(r.Context(), req.IDToken, s.googleAud)
		if err != nil && s.googleWebAud != "" {
			// The doctor web app sends tokens whose `aud` is the Web client ID.
			payload, err = idtoken.Validate(r.Context(), req.IDToken, s.googleWebAud)
		}
		if err != nil {
			http.Error(w, "invalid google token: "+err.Error(), http.StatusUnauthorized)
			return
		}
		sub, _ = payload.Claims["sub"].(string)
		email, _ = payload.Claims["email"].(string)
		name, _ = payload.Claims["name"].(string)
		if sub == "" {
			http.Error(w, "google token missing sub", http.StatusUnauthorized)
			return
		}
	}

	userID, role, pseudoID, err := s.db.UpsertGoogleUser(r.Context(), sub, email, name)
	if err != nil {
		http.Error(w, "db error", http.StatusInternalServerError)
		return
	}

	// Auto-promote dev-doctor/dev-admin/dev-elka if dev auth
	if os.Getenv("ALLOW_DEV_AUTH") == "true" && (req.IDToken == "dev-doctor" || req.IDToken == "dev-admin" || req.IDToken == "dev-elka") && role != "doctor" {
		_, _ = s.db.pool.Exec(r.Context(), "UPDATE users SET role = 'doctor' WHERE id = $1", userID)
		role = "doctor"
	}

	signed, err := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"sub":  userID,
		"role": role,
		"exp":  time.Now().Add(7 * 24 * time.Hour).Unix(),
	}).SignedString(s.jwtSecret)
	if err != nil {
		http.Error(w, "token issue failed", http.StatusInternalServerError)
		return
	}

	writeJSON(w, http.StatusOK, authResponse{Token: signed, Role: role, PseudoID: pseudoID, DisplayName: name})
}

func (s *server) handleMe(w http.ResponseWriter, r *http.Request) {
	writeJSON(w, http.StatusOK, map[string]string{
		"user_id": userIDFrom(r),
		"role":    roleFrom(r),
	})
}

// ── small helpers shared by all handlers ────────────────────────────────────

func writeJSON(w http.ResponseWriter, status int, body any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(body)
}

func decodeJSON(w http.ResponseWriter, r *http.Request, dst any) error {
	defer r.Body.Close()
	dec := json.NewDecoder(http.MaxBytesReader(w, r.Body, 4<<20))
	if err := dec.Decode(dst); err != nil {
		http.Error(w, "invalid json body", http.StatusBadRequest)
		return err
	}
	return nil
}

var errNotFound = errors.New("not found")

func notFoundOr(w http.ResponseWriter, err error, notFoundMsg string) bool {
	if err == nil {
		return false
	}
	if errors.Is(err, errNotFound) {
		http.Error(w, notFoundMsg, http.StatusNotFound)
		return true
	}
	http.Error(w, fmt.Sprintf("db error: %v", err), http.StatusInternalServerError)
	return true
}
