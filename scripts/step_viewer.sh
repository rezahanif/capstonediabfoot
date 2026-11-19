#!/usr/bin/env bash
# ==============================================================================
# step_viewer.sh — DiabFoot Guard Wireless BLE & USB Multi-Step Gait Viewer
# ==============================================================================

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$DIR/step_viewer.py"

if command -v uv >/dev/null 2>&1; then
    RUNNER="uv run --with bleak --with pyserial python3"
else
    RUNNER="python3"
fi

exec $RUNNER "$PYTHON_SCRIPT" "$@"
