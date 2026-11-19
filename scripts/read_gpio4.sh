#!/usr/bin/env bash
# ==============================================================================
# read_gpio4.sh — Live ESP32-C3 GPIO 4 ADC Serial Reader
# ==============================================================================

PORT="${1:-/dev/cu.usbmodem101}"
BAUD="${2:-115200}"

echo "=========================================================="
echo "  Reading ESP32-C3 GPIO 4 ADC on $PORT @ $BAUD baud"
echo "  Press [Ctrl+C] to exit"
echo "=========================================================="

if command -v uv >/dev/null 2>&1; then
    uv run --with pyserial python3 -c '
import sys, serial, glob

port = sys.argv[1] if len(sys.argv) > 1 else None
if not port or port == "auto":
    matches = glob.glob("/dev/cu.usbmodem*") + glob.glob("/dev/cu.usbserial*")
    port = matches[0] if matches else "/dev/cu.usbmodem101"

baud = int(sys.argv[2]) if len(sys.argv) > 2 else 115200
ser = serial.Serial(port, baud, timeout=1.0)
print(f"[+] Connected to {port}")

try:
    while True:
        line = ser.readline().decode("utf-8", errors="replace").strip()
        if line:
            print(line)
except KeyboardInterrupt:
    print("\n[+] Stopped.")
finally:
    ser.close()
' "$PORT" "$BAUD"
else
    python3 -m serial.tools.miniterm "$PORT" "$BAUD"
fi
