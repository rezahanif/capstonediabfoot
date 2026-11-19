#!/usr/bin/env bash
# ==============================================================================
# track_sensor_bl.sh — Wireless BLE Insole Tracker & CSV Logger for macOS
# ==============================================================================

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$DIR/sensor_ble_logger.py"

# Ensure uv or python3 is available
if command -v uv >/dev/null 2>&1; then
    RUNNER="uv run --with bleak python3"
else
    RUNNER="python3"
fi

exec $RUNNER "$PYTHON_SCRIPT" "$@"
