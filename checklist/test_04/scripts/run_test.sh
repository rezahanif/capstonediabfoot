#!/usr/bin/env bash
set -e

# Resolve script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

echo "=========================================================="
echo "  DiabFoot Guard — Launching TEST-04: Step Count Accuracy "
echo "=========================================================="

cd "$PROJECT_ROOT"

# Run using uv with bleak dependency
uv run --with bleak python3 "$SCRIPT_DIR/test_04_step_accuracy.py" "$@"
