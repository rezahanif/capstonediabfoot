#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$SCRIPT_DIR/battery_endurance_watcher.py"
T10_DIR="$SCRIPT_DIR/../../test_10/results"
T11_DIR="$SCRIPT_DIR/../results"

echo "=========================================================================="
echo "  DiabFoot Guard — Test 10 & 11 Battery & Stability Runner"
echo "  Target: 300 mAh >= 8.0 Hours Runtime & Zero Crashes"
echo "=========================================================================="

chmod +x "$PYTHON_SCRIPT"
exec uv run --with bleak python3 "$PYTHON_SCRIPT" --hours "${1:-8.0}" --side "RIGHT" --t10-dir "$T10_DIR" --t11-dir "$T11_DIR"
