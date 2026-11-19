#!/usr/bin/env python3
"""
Wireless BLE Sensor CSV Tracker & Logger for DiabFoot Smart Insole
Connects to ESP32-C3 via Bluetooth Low Energy (CoreBluetooth on macOS),
subscribes to the 5 Hz sensor stream and 1/min temperature updates,
displays real-time readings, and logs to a timestamped CSV file.
"""

import sys
import os
import time
import asyncio
import argparse
from datetime import datetime

try:
    from bleak import BleakScanner, BleakClient
except ImportError:
    print("Error: bleak is required. Run via 'uv run --with bleak python3 ...'")
    sys.exit(1)

# Insole BLE Contract (docs/plans/TD3-TD4-PLAN.md Part A4)
INSOLE_CONFIG = {
    "left": {
        "name": "Smart_Insole_Left",
        "service_uuid": "4fa2c732-ca9a-4c20-9492-c167df3c942b",
        "char_uuid": "beb5483e-36e1-4688-b7f5-ea07361b26a8",
    },
    "right": {
        "name": "Smart_Insole_Right",
        "service_uuid": "4fa2c732-ca9a-4c20-9492-c167df3c942c",
        "char_uuid": "beb5483e-36e1-4688-b7f5-ea07361b26c9",
    },
}

CSV_HEADER = (
    "timestamp_iso,elapsed_ms,"
    "ax,ay,az,gx,gy,gz,"
    "p1_hallux_kpa,p2_mth1_kpa,p3_mth2_kpa,p4_mth4_kpa,p5_heel_kpa,"
    "temp_c,bat_pct"
)


def safe_float(val, default=0.0):
    """Safely convert string to float with fallback."""
    try:
        return float(val)
    except (ValueError, TypeError):
        return default


class BleSensorLogger:
    def __init__(self, side="left", target_addr=None, output_csv=None, scan_timeout=10.0):
        self.side = side.lower()
        self.config = INSOLE_CONFIG.get(self.side, INSOLE_CONFIG["left"])
        self.target_addr = target_addr
        self.scan_timeout = scan_timeout
        self.output_csv = output_csv
        self.current_temp = "N/A"
        self.current_bat = "N/A"
        self.packet_count = 0
        self.start_time = None
        self.csv_file = None
        self.running = True

    async def find_device(self):
        print(f"[*] Scanning for BLE insole: '{self.config['name']}' (Service: {self.config['service_uuid']})...")
        try:
            # Fast scan filtered by insole service UUID (optimal for macOS CoreBluetooth)
            devices = await BleakScanner.discover(
                timeout=self.scan_timeout,
                service_uuids=[self.config["service_uuid"]]
            )
            for d in devices:
                if self.target_addr and d.address.lower() == self.target_addr.lower():
                    return d
                return d
        except Exception as e:
            print(f"[*] Note on filtered scan: {e}")

        # Fallback to general scan
        print("[*] Running broad scan...")
        devices = await BleakScanner.discover(timeout=self.scan_timeout)
        for d in devices:
            if self.target_addr and d.address.lower() == self.target_addr.lower():
                return d
            if d.name and self.config["name"].lower() in d.name.lower():
                return d

        return None

    def notification_handler(self, sender, data: bytearray):
        line = data.decode("utf-8", errors="replace").strip()
        now = time.time()
        elapsed_ms = int((now - self.start_time) * 1000)
        iso_str = datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3]

        if line.startswith("T,"):
            # Temperature packet: "T,<tempC>" or "T,<tempC>,<batPct>"
            parts = line.split(",")
            if len(parts) >= 2:
                self.current_temp = parts[1]
            if len(parts) >= 3:
                self.current_bat = parts[2]
            print(f"\033[1;35m[BLE TEMP] {line} | Temp: {self.current_temp}°C | Bat: {self.current_bat}%\033[0m")
            return

        fields = line.split(",")
        if len(fields) == 11:
            # 11-field stream: ax,ay,az,gx,gy,gz,p1,p2,p3,p4,p5
            self.packet_count += 1
            ax, ay, az, gx, gy, gz = fields[0:6]
            p1, p2, p3, p4, p5 = fields[6:11]

            # Write row to CSV
            row = (
                f"{iso_str},{elapsed_ms},"
                f"{ax},{ay},{az},{gx},{gy},{gz},"
                f"{p1},{p2},{p3},{p4},{p5},"
                f"{self.current_temp},{self.current_bat}"
            )
            if self.csv_file:
                self.csv_file.write(row + "\n")
                self.csv_file.flush()

            # Terminal display
            # Check IMU validity (non-zero)
            imu_ok = (ax != "0.000" or ay != "0.000" or az != "0.000")
            imu_badge = "\033[32mIMU:OK\033[0m" if imu_ok else "\033[31mIMU:0\033[0m"

            p1_f = safe_float(p1)
            p2_f = safe_float(p2)
            p3_f = safe_float(p3)
            p4_f = safe_float(p4)
            p5_f = safe_float(p5)
            az_f = safe_float(az)

            print(
                f"\033[32m[{self.packet_count:05d}]\033[0m "
                f"+{elapsed_ms:>6}ms | "
                f"{imu_badge} | "
                f"FSR=[{p1_f:>5.1f}, {p2_f:>5.1f}, {p3_f:>5.1f}, {p4_f:>5.1f}, {p5_f:>5.1f}] kPa | "
                f"az={az_f:>5.2f}m/s² | "
                f"T={self.current_temp}°C"
            )

    async def run(self):
        device = await self.find_device()
        if not device:
            print(f"[-] Could not find '{self.config['name']}' within {self.scan_timeout}s.")
            print("    Please ensure the ESP32 is powered on and within Bluetooth range.")
            print("    Note: If LightBlue is currently connected, disconnect it first!")
            return

        print(f"[+] Found {device.name} [{device.address}]!")
        print(f"[*] Connecting to {device.address}...")

        if not self.output_csv:
            ts = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
            self.output_csv = f"sensor_record_ble_{self.side}_{ts}.csv"

        print(f"[*] Logging stream to: {os.path.abspath(self.output_csv)}")

        with open(self.output_csv, "w", encoding="utf-8", buffering=1) as f:
            self.csv_file = f
            f.write(CSV_HEADER + "\n")
            f.flush()

            async with BleakClient(device) as client:
                connected = client.is_connected
                if not connected:
                    print("[-] Failed to establish BLE connection.")
                    return

                print(f"[+] Connected to {device.name}!")
                print(f"[*] Subscribing to characteristic {self.config['char_uuid']}...")

                try:
                    await client.start_notify(self.config["char_uuid"], self.notification_handler)
                    print("[+] Subscribed to stream notifications!")
                except Exception as e:
                    print(f"[*] Notification subscription note: {e}")

                print("[+] Live 5 Hz BLE stream active!")
                print("    FSR Mapping: [p1:Hallux, p2:MTH1, p3:MTH2, p4:MTH4, p5:Heel] in kPa")
                print("[*] Press Ctrl+C at any time to stop and save CSV.\n")

                self.start_time = time.time()
                last_count = self.packet_count

                try:
                    while self.running and client.is_connected:
                        await asyncio.sleep(0.2)
                        # Seamless fallback: if no notification arrived in the last 250ms, read char directly
                        if self.packet_count == last_count:
                            try:
                                data = await client.read_gatt_char(self.config["char_uuid"])
                                self.notification_handler(None, data)
                            except Exception:
                                pass
                        last_count = self.packet_count
                except asyncio.CancelledError:
                    pass
                finally:
                    try:
                        await client.stop_notify(self.config["char_uuid"])
                    except Exception:
                        pass

        print("\n==================================================")
        print(f"[+] Recording finished! Total stream packets: {self.packet_count}")
        print(f"[+] Data saved to: {os.path.abspath(self.output_csv)}")
        print("==================================================")


def main():
    parser = argparse.ArgumentParser(description="Track DiabFoot Smart Insole via BLE")
    parser.add_argument("--side", choices=["left", "right"], default="left", help="Insole side (default: left)")
    parser.add_argument("--address", "-a", help="Explicit BLE MAC / CoreBluetooth UUID (optional)")
    parser.add_argument("--output", "-o", help="Target CSV output file")
    parser.add_argument("--timeout", "-t", type=float, default=10.0, help="Scan timeout in seconds (default: 10)")
    args = parser.parse_args()

    logger = BleSensorLogger(
        side=args.side,
        target_addr=args.address,
        output_csv=args.output,
        scan_timeout=args.timeout
    )

    try:
        asyncio.run(logger.run())
    except KeyboardInterrupt:
        print("\n[*] Exiting cleanly...")


if __name__ == "__main__":
    main()
