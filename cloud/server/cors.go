package main

import (
	"net/http"
	"os"
	"strings"
)

// WEB_ORIGIN: the exact origin the doctor web app is served from
// (e.g. http://localhost:5173 in dev, https://doctor.example.com in prod).
// Empty = no CORS headers (API-only, same-origin deployments).
func webOrigin() string {
	return strings.TrimSpace(os.Getenv("WEB_ORIGIN"))
}

// corsMiddleware allows the doctor web SPA (a different origin from the API)
// to call these endpoints with the Authorization header. No origin configured
// → behaves exactly as before. Supports "*" or exact matching.
func corsMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		origin := webOrigin()
		reqOrigin := r.Header.Get("Origin")

		if origin != "" {
			if origin == "*" {
				w.Header().Set("Access-Control-Allow-Origin", "*")
			} else if reqOrigin == origin || origin == "" {
				w.Header().Set("Access-Control-Allow-Origin", reqOrigin)
				w.Header().Add("Vary", "Origin")
			}
			w.Header().Set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS")
			w.Header().Set("Access-Control-Allow-Headers", "Authorization, Content-Type, Idempotency-Key")
			w.Header().Set("Access-Control-Max-Age", "86400")
		}
		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}
		next.ServeHTTP(w, r)
	})
}
