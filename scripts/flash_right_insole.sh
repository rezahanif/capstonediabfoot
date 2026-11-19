#!/usr/bin/env bash
# ==============================================================================
# flash_right_insole.sh — One-Touch Compile, Flash, & Diagnostic for Right Insole
# Firmware: firmware/insole_preproduction/
# Target: Smart_Insole_Right (ESP32-C3)
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
FIRMWARE_DIR="$REPO_ROOT/firmware/insole_preproduction"
FQBN="esp32:esp32:esp32c3:CDCOnBoot=cdc,PartitionScheme=huge_app"

echo "=========================================================================="
echo "  DiabFoot Guard — Right Insole Flash & Diagnostic Utility"
echo "  Firmware: insole_preproduction.ino (Right Insole / Custom Pin Map)"
echo "=========================================================================="

find_esp_port() {
    for p in /dev/cu.usbmodem* /dev/cu.usbserial* /dev/cu.wchusbserial*; do
        if [ -e "$p" ]; then
            echo "$p"
            return 0
        fi
    done
    return 1
}

PORT=$(find_esp_port || true)

if [ -z "$PORT" ]; then
    echo "[!] ESP32-C3 is not currently plugged in."
    echo "[*] Waiting for USB connection (plug in the insole now)..."
    while true; do
        PORT=$(find_esp_port || true)
        if [ -n "$PORT" ]; then
            break
        fi
        sleep 1
    done
fi

echo "[+] ESP32-C3 detected on port: $PORT"

echo ""
echo "[*] Step 1/3: Compiling and uploading Right Insole firmware..."
arduino-cli compile --upload -p "$PORT" --fqbn "$FQBN" "$FIRMWARE_DIR"

echo ""
echo "[+] Step 2/3: Flash completed successfully!"
echo "[*] Step 3/3: Opening diagnostic monitor (press any FSR to test response)..."
echo "    [Press Ctrl+C to exit monitor anytime]"
echo "--------------------------------------------------------------------------"

sleep 1

if command -v uv >/dev/null 2>&1; then
    uv run --with pyserial python3 -c '
import sys, serial, time

port = sys.argv[1]
try:
    ser = serial.Serial(port, 115200, timeout=1.0)
    ser.dtr = True
    ser.rts = False
    print(f"[+] Listening on {port} (115200 baud)...\n")
    start = time.time()
    while True:
        line = ser.readline().decode("utf-8", errors="replace").strip()
        if line:
            print(line)
except KeyboardInterrupt:
    print("\n[+] Diagnostic monitor stopped.")
except Exception as e:
    print(f"[-] Serial error: {e}")
' "$PORT"
else
    echo "[*] To monitor serial live: screen $PORT 115200"
fi
