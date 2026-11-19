#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/../results"
REF_TEMP="${1:-25.4}"

echo "=========================================================================="
echo "  DiabFoot Guard — TEST-01: NTC Thermistor MAE Accuracy Benchmark"
echo "  Reference Temperature: ${REF_TEMP} °C"
echo "=========================================================================="
echo "Select execution mode:"
echo "  1) Live Continuous Metrology Viewer (Live ADC + Vout + R_NTC + °C) [Recommended]"
echo "  2) Automated 20-Sample MAE Benchmark Batch Run"
read -p "Enter choice [1/2] (default 1): " CHOICE

if [ "$CHOICE" == "2" ]; then
    chmod +x "$SCRIPT_DIR/test_01_ntc_mae.py"
    exec uv run --with bleak python3 "$SCRIPT_DIR/test_01_ntc_mae.py" --reference "$REF_TEMP" --output-dir "$OUTPUT_DIR"
else
    chmod +x "$SCRIPT_DIR/live_temperature_viewer.py"
    exec uv run --with bleak python3 "$SCRIPT_DIR/live_temperature_viewer.py" --reference "$REF_TEMP" --output-dir "$OUTPUT_DIR"
fi
