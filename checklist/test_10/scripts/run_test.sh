#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
T11_RUNNER="$SCRIPT_DIR/../../test_11/scripts/run_test.sh"

echo "=========================================================================="
echo "  DiabFoot Guard — Test 10: System Stability, Uptime & Zero Crash Bench"
echo "=========================================================================="

exec "$T11_RUNNER" "$@"
