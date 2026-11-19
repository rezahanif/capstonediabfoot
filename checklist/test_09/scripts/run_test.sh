#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$SCRIPT_DIR/test_09_reception_processing_rate.py"
RESULTS_DIR="$SCRIPT_DIR/../results"

echo "=========================================================================="
echo "  DiabFoot Guard — Checklist Test 09 Runner"
echo "  BLE Data Reception & App Processing Success Rate Measurement"
echo "=========================================================================="

chmod +x "$PYTHON_SCRIPT"
exec python3 "$PYTHON_SCRIPT" -o "$RESULTS_DIR" "$@"
