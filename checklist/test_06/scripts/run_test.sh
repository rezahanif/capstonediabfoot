#!/usr/bin/env bash
# ==============================================================================
# DiabFoot Guard — Checklist Test 06 Runner
# BLE Communication Packet Loss & Reliability Test
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$SCRIPT_DIR/ble_packet_loss_test.py"
RESULTS_DIR="$SCRIPT_DIR/../results"

echo "=========================================================================="
echo "  DiabFoot Guard — Checklist Test 06"
echo "  BLE Communication Packet Loss & Reliability Measurement"
echo "=========================================================================="

if ! command -v uv &>/dev/null; then
    echo "[-] Error: 'uv' is not installed or not in PATH."
    exit 1
fi

exec uv run --with bleak python3 "$PYTHON_SCRIPT" -o "$RESULTS_DIR" "$@"
