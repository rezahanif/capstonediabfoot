#!/usr/bin/env bash
# ==============================================================================
# DiabFoot Guard — Spec v1 Wireless BLE Live Sensor Tracker
# Runs the automated Spec v1 BLE client on macOS via uv (bleak)
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$SCRIPT_DIR/sensor_ble_spec_logger.py"

echo "=========================================================================="
echo "  DiabFoot Guard — Spec v1 Wireless BLE Live Sensor Tracker"
echo "  5 FSR Plantar Pressures + NTC Temperature + 6-DOF IMU (Accel & Gyro)"
echo "=========================================================================="

if ! command -v uv &>/dev/null; then
    echo "[-] Error: 'uv' is not installed or not in PATH."
    echo "    Install with: curl -LsSf https://astral.sh/uv/install.sh | sh"
    exit 1
fi

exec uv run --with bleak python3 "$PYTHON_SCRIPT" "$@"
