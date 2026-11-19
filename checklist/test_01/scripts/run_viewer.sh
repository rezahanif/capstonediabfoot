#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/../results"

if [ "$1" == "--help" ] || [ "$1" == "-h" ]; then
    echo "Usage: ./run_viewer.sh [reference_temperature_celsius] [side]"
    echo "Examples:"
    echo "  ./run_viewer.sh LEFT           # Target Left insole with default 25.4°C reference"
    echo "  ./run_viewer.sh 26.5 LEFT      # Target Left insole with 26.5°C reference"
    echo "  ./run_viewer.sh RIGHT          # Target Right insole"
    echo "  ./run_viewer.sh 26.5           # Auto-detect insole side with 26.5°C reference"
    echo ""
    echo "NOTE: The reference temperature is the value measured by your external"
    echo "thermo gun/thermometer, used to compute accuracy and Mean Absolute Error (MAE)."
    echo "It is NOT the insole's reading. The insole reading is streamed live via BLE."
    exit 0
fi

REF_TEMP="25.4"
TARGET_SIDE="AUTO"

for arg in "$@"; do
    arg_upper=$(echo "$arg" | tr '[:lower:]' '[:upper:]')
    if [[ "$arg" =~ ^[0-9]+(\.[0-9]+)?$ ]]; then
        REF_TEMP="$arg"
    elif [ "$arg_upper" == "LEFT" ] || [ "$arg_upper" == "RIGHT" ] || [ "$arg_upper" == "AUTO" ]; then
        TARGET_SIDE="$arg_upper"
    fi
done

echo "=========================================================================="
echo "  DiabFoot Guard — TEST-01: Live NTC Temperature & Metrology Viewer"
echo "  • Target Insole Side                       : ${TARGET_SIDE}"
echo "  • External Benchmark Reference (Thermo Gun) : ${REF_TEMP} °C"
echo "    (Usage: ./run_viewer.sh [ref_temp] [side: LEFT|RIGHT])"
echo "  • Insole Skin Temperature                  : Streamed LIVE via BLE"
echo "=========================================================================="

chmod +x "$SCRIPT_DIR/live_temperature_viewer.py"
exec uv run --with bleak python3 "$SCRIPT_DIR/live_temperature_viewer.py" --reference "$REF_TEMP" --side "$TARGET_SIDE" --output-dir "$OUTPUT_DIR"
