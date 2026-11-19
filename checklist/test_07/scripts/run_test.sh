#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$SCRIPT_DIR/test_07_ble_latency.py"
RESULTS_DIR="$SCRIPT_DIR/../results"

echo "=========================================================================="
echo "  DiabFoot Guard — Checklist Test 07 Runner"
echo "  BLE Communication Latency & Interval Stability Measurement"
echo "=========================================================================="

chmod +x "$PYTHON_SCRIPT"
exec python3 "$PYTHON_SCRIPT" -o "$RESULTS_DIR" "$@"
