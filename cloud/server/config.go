package main

import (
	"net/http"
)

// Versioned prompt-thresholds config served to patient apps (TD-8).
//
// Static configuration, not clinical processing: the server's only job is
// distributing the signed-off threshold version. The app caches it locally
// and falls back to its bundled defaults when this endpoint is unreachable.
// To publish a new regime: change the JSON below (or swap to reading a file
// from CONFIG_DIR), bump `version`, and restart — apps adopt it on next
// refresh and stamp the version onto every prompt/episode they produce.
const defaultThresholdsConfigJSON = `{
  "version": "v1",
  "thermal_delta_warning_c": 2.2,
  "thermal_persistence_count": 3,
  "thermal_cooldown_minutes": 60,
  "pressure_warning": 2000,
  "pressure_cooldown_minutes": 30
}`

// handleThresholdsConfig serves the active thresholds config verbatim.
// Auth-required for consistency with the rest of the API; the payload is
// not sensitive.
func (s *server) handleThresholdsConfig(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	_, _ = w.Write([]byte(defaultThresholdsConfigJSON))
}
