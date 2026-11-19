#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$SCRIPT_DIR/monitor_fsr1.py"
OUTPUT_DIR="$SCRIPT_DIR/../results"

echo "=========================================================================="
echo "  DiabFoot Guard — FSR 1 (GPIO 0 / Hallux) Continuous Electrical Monitor"
echo "  Displays real-time: ADC count (0-4095), Voltage Vout (V), Resistance (Ω/kΩ)"
echo "=========================================================================="

chmod +x "$PYTHON_SCRIPT"
exec uv run --with bleak python3 "$PYTHON_SCRIPT" --output-dir "$OUTPUT_DIR" "$@"
