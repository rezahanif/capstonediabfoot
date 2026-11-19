#!/usr/bin/env python3
"""
Sensor Live CSV Tracker & Logger for ESP32-C3
Reads real-time sensor data from USB CDC serial, displays it in terminal,
and logs it to a timestamped CSV file.
"""

import sys
import os
import glob
import time
import argparse
from datetime import datetime

try:
    import serial
except ImportError:
    print("Error: pyserial is required. Run via 'uv run --with pyserial python3 ...'")
    sys.exit(1)


def find_esp32_port():
    """Auto-detect ESP32 USB modem device on macOS."""
    candidates = glob.glob("/dev/cu.usbmodem*")
    if candidates:
        return sorted(candidates)[0]
    return None


def safe_float(val, default=0.0):
    """Safely convert string to float with fallback."""
    try:
        return float(val)
    except (ValueError, TypeError):
        return default


def clear_port_locks(port):
    """Check if any process is holding the port and prompt or terminate."""
    try:
        import subprocess
        res = subprocess.run(["lsof", port], capture_output=True, text=True)
        if res.stdout:
            lines = res.stdout.strip().split("\n")
            if len(lines) > 1:
                print(f"[!] Warning: Port {port} is held by another process:")
                print(res.stdout)
                for line in lines[1:]:
                    parts = line.split()
                    if len(parts) > 1:
                        pid = parts[1]
                        print(f"[*] Terminating conflicting process PID {pid}...")
                        subprocess.run(["kill", "-9", pid])
                time.sleep(0.5)
    except Exception as e:
        print(f"[*] Note: Port check: {e}")


def main():
    parser = argparse.ArgumentParser(description="Track and log ESP32 sensor output to CSV")
    parser.add_argument("--port", "-p", help="Serial port (default: auto-detect /dev/cu.usbmodem*)")
    parser.add_argument("--baud", "-b", type=int, default=115200, help="Baud rate (default: 115200)")
    parser.add_argument("--output", "-o", help="Target CSV file path (default: sensor_record_TIMESTAMP.csv)")
    args = parser.parse_args()

    port = args.port or find_esp32_port()
    if not port:
        print("[-] Error: No ESP32 device found on /dev/cu.usbmodem*.")
        print("    Please ensure your ESP32-C3 is plugged into a USB-C port.")
        sys.exit(1)

    # Output file setup
    if args.output:
        csv_filename = args.output
    else:
        timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        csv_filename = f"sensor_record_{timestamp}.csv"

    print("==================================================")
    print("  ESP32-C3 Live Sensor CSV Tracker & Logger")
    print("==================================================")
    print(f"[*] Target Port : {port}")
    print(f"[*] Baud Rate   : {args.baud}")
    print(f"[*] Log File    : {os.path.abspath(csv_filename)}")
    print("==================================================")

    clear_port_locks(port)

    try:
        ser = serial.Serial(
            port=port,
            baudrate=args.baud,
            timeout=1,
            rtscts=False,
            dsrdtr=False
        )
        # Operational USB CDC mode: RTS=False (run/no reset), DTR=True (terminal ready)
        ser.rts = False
        ser.dtr = True
        time.sleep(0.1)
    except Exception as e:
        print(f"[-] Failed to open port {port}: {e}")
        print("    If Arduino IDE or another monitor is running, please close it.")
        sys.exit(1)

    print("[+] Serial port opened successfully! Listening for data...")
    print("    FSR Mapping: [F1:Hallux, F2:MTH1, F3:MTH2, F4:MTH4, F5:Heel] in kPa")
    print("[*] Press Ctrl+C at any time to stop logging.\n")

    STANDARD_HEADER = (
        "time_ms,"
        "fsr1_v,fsr1_r,fsr1_forceN,fsr1_pressurekPa,"
        "fsr2_v,fsr2_r,fsr2_forceN,fsr2_pressurekPa,"
        "fsr3_v,fsr3_r,fsr3_forceN,fsr3_pressurekPa,"
        "fsr4_v,fsr4_r,fsr4_forceN,fsr4_pressurekPa,"
        "fsr5_v,fsr5_r,fsr5_forceN,fsr5_pressurekPa,"
        "ntc_v_ads,ntc_r,ntc_tempC,"
        "mpu_status,ax,ay,az,gx,gy,gz,mpu_tempC"
    )

    row_count = 0
    header_written = False

    try:
        with open(csv_filename, "w", encoding="utf-8", buffering=1) as f:
            # Pre-write standard header so the CSV file is always valid from byte 0
            f.write(STANDARD_HEADER + "\n")
            f.flush()
            header_written = True

            while True:
                raw = ser.readline()
                if not raw:
                    continue

                line = raw.decode("utf-8", errors="replace").strip()
                if not line:
                    continue

                # Check if line is CSV data or diagnostic message
                fields = line.split(",")

                if len(fields) >= 20:
                    if "time_ms" in fields[0]:
                        # Skip duplicate header if firmware prints it
                        print(f"\033[1;36m[HEADER] {line}\033[0m")
                        continue

                    # Valid data row
                    f.write(line + "\n")
                    f.flush()
                    row_count += 1
                    time_str = fields[0]
                    imu_status = fields[24] if len(fields) > 24 else "N/A"
                    ntc_temp = fields[23] if len(fields) > 23 else "N/A"
                    status_color = "\033[32m" if "OK" in imu_status else "\033[31m"

                    p1 = safe_float(fields[4]) if len(fields) > 4 else 0.0
                    p2 = safe_float(fields[8]) if len(fields) > 8 else 0.0
                    p3 = safe_float(fields[12]) if len(fields) > 12 else 0.0
                    p4 = safe_float(fields[16]) if len(fields) > 16 else 0.0
                    p5 = safe_float(fields[20]) if len(fields) > 20 else 0.0
                    az = safe_float(fields[27]) if len(fields) > 27 else 0.0

                    print(
                        f"\033[32m[{row_count:05d}]\033[0m "
                        f"t={time_str:>7}ms | "
                        f"IMU={status_color}{imu_status:<4}\033[0m | "
                        f"FSR=[{p1:>5.1f}, {p2:>5.1f}, {p3:>5.1f}, {p4:>5.1f}, {p5:>5.1f}] kPa | "
                        f"NTC={ntc_temp:>5}°C | "
                        f"az={az:>6.2f}"
                    )
                else:
                    # Informational / Diagnostic line
                    print(f"\033[90m[DIAG] {line}\033[0m")

    except KeyboardInterrupt:
        print("\n\n[*] Stopping sensor logger...")
    finally:
        ser.close()
        print("==================================================")
        print(f"[+] Total data rows recorded: {row_count}")
        print(f"[+] Log successfully saved to: {os.path.abspath(csv_filename)}")
        print("==================================================")


if __name__ == "__main__":
    main()
