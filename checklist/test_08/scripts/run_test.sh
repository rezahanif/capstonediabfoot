#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$SCRIPT_DIR/test_08_sampling_rate.py"
OUTPUT_DIR="$SCRIPT_DIR/../results"

echo "=========================================================================="
echo "  DiabFoot Guard — Test 08 MPU 50 Hz Sampling Rate Bench"
echo "  Target: Smart_Insole_Left (50 Hz / 20 ms Cadence & Dynamic Throttling)"
echo "=========================================================================="

chmod +x "$PYTHON_SCRIPT"
exec uv run --with bleak python3 "$PYTHON_SCRIPT" --duration "${1:-25.0}" --output-dir "$OUTPUT_DIR"
