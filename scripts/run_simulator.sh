#!/usr/bin/env bash
# Quick launcher for DiabFoot Insole BLE Simulator
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "Starting Smart Insole BLE Simulator..."
echo "Make sure Bluetooth is turned ON on this Mac and on your Android phone."
echo ""
uv run --with bless python3 "$SCRIPT_DIR/simulate_insole_ble.py" "$@"
