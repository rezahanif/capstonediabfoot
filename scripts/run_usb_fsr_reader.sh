#!/usr/bin/env bash
# ==============================================================================
# run_usb_fsr_reader.sh — DiabFoot Guard FSR Sensitivity Bench Runner
# ==============================================================================
# Quick execution wrapper for scripts/usb_fsr_reader.py.
#
# Usage examples:
#   ./scripts/run_usb_fsr_reader.sh                  # Default 10k resistor
#   ./scripts/run_usb_fsr_reader.sh --preset 47k     # Recommended for bench weight testing
#   ./scripts/run_usb_fsr_reader.sh --preset 100k    # Ultra high sensitivity
#   ./scripts/run_usb_fsr_reader.sh --r-ref 20000    # Custom resistor / trimpot setting
#   ./scripts/run_usb_fsr_reader.sh --scroll         # Continuous scroll log
# ==============================================================================

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$DIR/usb_fsr_reader.py"

if command -v uv >/dev/null 2>&1; then
    RUNNER="uv run --with pyserial python3"
else
    RUNNER="python3"
fi

exec $RUNNER "$PYTHON_SCRIPT" "$@"
