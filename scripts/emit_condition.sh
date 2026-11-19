#!/usr/bin/env bash
# Quick CLI launcher to emit synthetic BLE telemetry for each clinical condition
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONDITION="${1:-normal}"

echo "=========================================================="
echo "  DIABFOOT CLINICAL SCENARIO TELEMETRY EMITTER"
echo "=========================================================="
echo "Target Condition: $CONDITION"
echo ""
echo "Available conditions:"
echo "  - swell    : Condition 1 (HIGH Risk, Valid Swell: Temp + Pressure)"
echo "  - hotspot  : Condition 2 (HIGH Risk, Sustained Hotspot: Temp only)"
echo "  - pressure : Condition 3 (MEDIUM Risk, Mechanical Overload)"
echo "  - normal   : Condition 4 (LOW Risk, Balanced Walk)"
echo "  - single   : Condition 5 (Single Insole: Left connected only)"
echo "  - rest     : Standing / Rest"
echo ""
echo "NOTE: For multi-day conditions (swell or hotspot), open the app to"
echo "      Settings -> Hardware Diagnostics -> 'Clinical Scenario Seeder'"
echo "      and tap 'Seed Left Hotspot' to satisfy Day 1 immediately!"
echo "=========================================================="

python3 "$SCRIPT_DIR/stream_bilateral.py" --condition "$CONDITION"
