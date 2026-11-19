#!/usr/bin/env bash
# Quick launcher for DiabFoot Bilateral (Both Feet) USB Streamer
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "Starting DiabFoot Bilateral USB Telemetry Streamer..."
python3 "$SCRIPT_DIR/stream_bilateral.py" "$@"
