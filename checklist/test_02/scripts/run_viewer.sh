#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/../results"
TARGET_SIDE="AUTO"

if [ "$1" == "--help" ] || [ "$1" == "-h" ]; then
    echo "Usage: ./run_viewer.sh [side: LEFT|RIGHT|AUTO]"
    echo "Example: ./run_viewer.sh LEFT"
    echo "Example: ./run_viewer.sh RIGHT"
    exit 0
fi

if [ -n "$1" ]; then
    TARGET_SIDE=$(echo "$1" | tr '[:lower:]' '[:upper:]')
fi

echo "=========================================================================="
echo "  DiabFoot Guard — TEST-02: Live Continuous Plantar Pressure Metrology Viewer"
echo "  Target Side: ${TARGET_SIDE}"
echo "=========================================================================="

chmod +x "$SCRIPT_DIR/live_fsr_reader.py"
exec uv run --with bleak python3 "$SCRIPT_DIR/live_fsr_reader.py" --side "$TARGET_SIDE" --output-dir "$OUTPUT_DIR"
