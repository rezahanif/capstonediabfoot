#!/usr/bin/env bash
# ==============================================================================
# track_sensor.sh — Real-time ESP32 Sensor CSV Tracker for macOS
# ==============================================================================

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$DIR/sensor_logger.py"

# Ensure uv or python3 is available
if command -v uv >/dev/null 2>&1; then
    RUNNER="uv run --with pyserial python3"
else
    RUNNER="python3"
fi

# Execute Python logger with all arguments passed through
exec $RUNNER "$PYTHON_SCRIPT" "$@"
