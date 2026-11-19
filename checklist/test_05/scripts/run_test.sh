#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

echo "=========================================================="
echo "  DiabFoot Guard — Launching TEST-05: Gait Phase Classifier"
echo "=========================================================="

cd "$PROJECT_ROOT"
uv run --with bleak python3 "$SCRIPT_DIR/test_05_gait_classification.py" "$@"
