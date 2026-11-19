#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$SCRIPT_DIR/monitor_fsr.py"
OUTPUT_DIR="$SCRIPT_DIR/../results"

echo "=========================================================================="
echo "  DiabFoot Guard — Sensor 2 (GPIO 1 / MTH 1) Continuous Electrical Monitor"
echo "  Displays real-time: ADC count (0-4095), Voltage Vout (V), Resistance (Ω/kΩ)"
echo "=========================================================================="

chmod +x "$PYTHON_SCRIPT"
exec uv run --with bleak python3 "$PYTHON_SCRIPT" --sensor 2 --output-dir "$OUTPUT_DIR" "$@"
