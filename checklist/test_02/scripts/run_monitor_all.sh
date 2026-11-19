#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$SCRIPT_DIR/monitor_all_fsr.py"
OUTPUT_DIR="$SCRIPT_DIR/../results"

echo "=========================================================================="
echo "  DiabFoot Guard — 5-Channel FSR Continuous Electrical Monitor"
echo "  Monitors simultaneously: S1 (GPIO 0), S2 (GPIO 1), S3 (GPIO 2), S4 (GPIO 3), S5 (GPIO 4)"
echo "=========================================================================="

chmod +x "$PYTHON_SCRIPT"
exec uv run --with bleak python3 "$PYTHON_SCRIPT" --output-dir "$OUTPUT_DIR" "$@"
