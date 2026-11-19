#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/../results"

echo "=========================================================================="
echo "  DiabFoot Guard — TEST-02: Plantar Pressure FSR Metrology & MRE Bench"
echo "=========================================================================="
echo "Select execution mode:"
echo "  1) Live Continuous Metrology Reader (Live ADC + Piecewise kPa + kg) [Recommended]"
echo "  2) Guided Calibration Step Wizard (Tare -> Step Weight Entry)"
read -p "Enter choice [1/2] (default 1): " CHOICE

if [ "$CHOICE" == "2" ]; then
    chmod +x "$SCRIPT_DIR/test_02_weight_accuracy.py"
    exec uv run --with bleak python3 "$SCRIPT_DIR/test_02_weight_accuracy.py" --output-dir "$OUTPUT_DIR"
else
    chmod +x "$SCRIPT_DIR/live_fsr_reader.py"
    exec uv run --with bleak python3 "$SCRIPT_DIR/live_fsr_reader.py" --output-dir "$OUTPUT_DIR"
fi
