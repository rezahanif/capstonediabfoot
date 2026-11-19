#!/usr/bin/env python3
"""
DiabFoot Guard — Spec v1 BLE Telemetry Client & Logger
Connects to ESP32-C3 running Spec v1 binary firmware over BLE, executes
the 2-stage handshake (HELLO -> START), manages cumulative ACKs, unpacks
full binary sensor batches (5 FSRs, NTC temperature, 3-axis MPU accel & gyro),
and logs to timestamped CSV.
"""

import sys
import os
import time
import struct
import asyncio
import argparse
from datetime import datetime
from bleak import BleakScanner, BleakClient

# Spec v1 GATT UUIDs
SVC_DIABFOOT = "64696162-666f-6f74-a001-202610230001"
CHR_CTRL     = "64696162-666f-6f74-a002-202610230001"
CHR_DATA     = "64696162-666f-6f74-a003-202610230001"
CHR_STATUS   = "64696162-666f-6f74-a004-202610230001"
CHR_CONFIG   = "64696162-666f-6f74-a005-202610230001"

CSV_HEADER = (
    "timestamp_iso,elapsed_ms,seq,sample_time_ms,"
    "p1_hallux_kpa,p2_mth1_kpa,p3_mth2_kpa,p4_mth4_kpa,p5_heel_kpa,"
    "ntc_temp_c,ax_g,ay_g,az_g,ax_ms2,ay_ms2,az_ms2,"
    "gx_dps,gy_dps,gz_dps,imu_status,battery_pct"
)


class SpecV1BleLogger:
    def __init__(self, output_csv=None, scan_timeout=60.0):
        self.scan_timeout = scan_timeout
        self.output_csv = output_csv
        self.client = None
        self.csv_file = None
        self.start_time = None
        self.packet_count = 0
        self.sample_count = 0
        self.last_seq = 0
        self.last_acked_seq = 0
        self.current_battery = "N/A"
        self.running = True
        self.hello_event = asyncio.Event()
        self.start_event = asyncio.Event()
        self.hello_info = {}

    async def find_device(self):
        print(f"[*] Scanning for Spec v1 Insole (timeout: {self.scan_timeout}s)...")
        print("    (Tip: If the insole was idle for >5 min, press the RST button on the ESP32 to wake it)")
        start_scan = time.time()
        while time.time() - start_scan < self.scan_timeout:
            devices = await BleakScanner.discover(timeout=4.0, return_adv=True)
            for d, adv in devices.values():
                name = d.name or adv.local_name or ""
                uuids = [str(u).lower() for u in adv.service_uuids]
                if any(SVC_DIABFOOT.lower() in u for u in uuids) or "DIABFOOT" in name.upper() or "SMART_INSOLE" in name.upper():
                    return d, name
            elapsed = int(time.time() - start_scan)
            remaining = max(0, int(self.scan_timeout - elapsed))
            if remaining > 0:
                print(f"    ...still searching ({remaining}s remaining, press RST on ESP32 if not waking)...")
        return None, None

    def status_notification_handler(self, sender, data: bytearray):
        if not data:
            return
        print(f"\033[90m[STATUS NOTIFY] Hex: {data.hex()}\033[0m")
        op = data[0]
        if op == 0x81:
            # HELLO Reply
            proto = data[1] if len(data) > 1 else 0
            fw_maj = data[2] if len(data) > 2 else 0
            fw_min = data[3] if len(data) > 3 else 0
            fw_pat = data[4] if len(data) > 4 else 0
            hw_rev = data[5] if len(data) > 5 else 0
            boot_id = struct.unpack("<I", data[6:10])[0] if len(data) >= 10 else 0
            bitmap = data[10] if len(data) > 10 else 0
            calib_ver = struct.unpack("<H", data[11:13])[0] if len(data) >= 13 else 0
            bat = data[13] if len(data) > 13 else 0xFF
            self.current_battery = f"{bat}%" if bat != 0xFF else "N/A"
            ads_detected = bool(bitmap & 0x01)
            mpu_detected = bool(bitmap & 0x02)
            fsr_cnt = bin((bitmap >> 2) & 0x1F).count("1")
            print(f"[*] Firmware Sensor Bitmap (0x{bitmap:02X}):")
            print(f"    - ADS1115 (NTC ADC) : {'\033[32mOK (Connected)\033[0m' if ads_detected else '\033[31mOFFLINE (Not detected on I2C)\033[0m'}")
            print(f"    - MPU-6050/ICM IMU   : {'\033[32mOK (Connected)\033[0m' if mpu_detected else '\033[31mOFFLINE (Not detected on I2C)\033[0m'}")
            print(f"    - Plantar FSRs       : \033[32m{fsr_cnt}/5 Connected\033[0m")
            self.hello_info = {
                "proto": proto,
                "version": f"{fw_maj}.{fw_min}.{fw_pat}",
                "hw": hw_rev,
                "boot_id": boot_id,
                "calib": calib_ver,
                "battery": self.current_battery,
                "ads_ok": ads_detected,
                "mpu_ok": mpu_detected
            }
            self.hello_event.set()
        elif op == 0x83:
            # START Reply
            status = data[1] if len(data) > 1 else 0xFF
            if status == 0:
                self.start_event.set()
        elif op == 0xEE or (op & 0x80 and len(data) >= 3 and data[1] == 0xEE):
            print(f"\033[31m[STATUS ERR] Firmware reported error: op=0x{op:02X}, code={data[2]}\033[0m")

    def data_notification_handler(self, sender, data: bytearray):
        if len(data) < 14:
            return
        msg_type = data[0]
        frame_ver = data[1]
        if msg_type != 0x01:
            return

        base_seq = struct.unpack("<I", data[2:6])[0]
        base_time = struct.unpack("<I", data[6:10])[0]
        count = data[10]
        flags = data[11]

        self.packet_count += 1
        now = time.time()
        elapsed_ms = int((now - self.start_time) * 1000)
        iso_str = datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3]

        for i in range(count):
            offset = 12 + i * 27
            if offset + 27 > len(data):
                break
            
            self.sample_count += 1
            seq = base_seq + i
            if seq > self.last_seq:
                self.last_seq = seq

            time_delta = struct.unpack("<H", data[offset:offset+2])[0]
            sample_time = base_time + time_delta

            # 5 FSR pressures in kPa
            p1 = struct.unpack("<H", data[offset+2:offset+4])[0]
            p2 = struct.unpack("<H", data[offset+4:offset+6])[0]
            p3 = struct.unpack("<H", data[offset+6:offset+8])[0]
            p4 = struct.unpack("<H", data[offset+8:offset+10])[0]
            p5 = struct.unpack("<H", data[offset+10:offset+12])[0]

            # NTC Temperature (c100)
            raw_ntc = struct.unpack("<h", data[offset+12:offset+14])[0]
            if raw_ntc != 0x7FFF and raw_ntc > -9000:
                temp_c = raw_ntc / 100.0
                temp_str = f"{temp_c:>5.2f}°C"
            else:
                temp_c = ""
                temp_str = " N/A "

            # Accel in mg (1000 mg = 1.0 g = 9.80665 m/s²)
            raw_ax = struct.unpack("<h", data[offset+14:offset+16])[0]
            raw_ay = struct.unpack("<h", data[offset+16:offset+18])[0]
            raw_az = struct.unpack("<h", data[offset+18:offset+20])[0]
            ax_g = raw_ax / 1000.0
            ay_g = raw_ay / 1000.0
            az_g = raw_az / 1000.0
            ax_ms2 = ax_g * 9.80665
            ay_ms2 = ay_g * 9.80665
            az_ms2 = az_g * 9.80665

            # Gyro in 0.1 dps
            raw_gx = struct.unpack("<h", data[offset+20:offset+22])[0]
            raw_gy = struct.unpack("<h", data[offset+22:offset+24])[0]
            raw_gz = struct.unpack("<h", data[offset+24:offset+26])[0]
            gx_dps = raw_gx / 10.0
            gy_dps = raw_gy / 10.0
            gz_dps = raw_gz / 10.0

            status_byte = data[offset+26]
            imu_ok = bool(status_byte & 0x04) or (raw_ax != 0 or raw_ay != 0 or raw_az != 0)
            imu_badge = "\033[32mIMU:OK\033[0m" if imu_ok else "\033[31mIMU:0\033[0m"

            # Write row to CSV
            if self.csv_file:
                row = (
                    f"{iso_str},{elapsed_ms},{seq},{sample_time},"
                    f"{p1},{p2},{p3},{p4},{p5},"
                    f"{temp_c},{ax_g:.3f},{ay_g:.3f},{az_g:.3f},"
                    f"{ax_ms2:.3f},{ay_ms2:.3f},{az_ms2:.3f},"
                    f"{gx_dps:.1f},{gy_dps:.1f},{gz_dps:.1f},"
                    f"{'OK' if imu_ok else 'ERR'},{self.current_battery}"
                )
                self.csv_file.write(row + "\n")
                self.csv_file.flush()

            # Terminal Live Print
            print(
                f"\033[32m[{self.sample_count:05d}]\033[0m "
                f"+{elapsed_ms:>6}ms | "
                f"{imu_badge} | "
                f"NTC={temp_str} | "
                f"FSR=[{p1:>5.1f}, {p2:>5.1f}, {p3:>5.1f}, {p4:>5.1f}, {p5:>5.1f}] kPa | "
                f"a=[{ax_g:>5.2f},{ay_g:>5.2f},{az_g:>5.2f}]g | "
                f"g=[{gx_dps:>5.1f},{gy_dps:>5.1f},{gz_dps:>5.1f}]°/s"
            )

    async def run(self):
        device, dev_name = await self.find_device()
        if not device:
            print(f"[-] Could not discover insole device within {self.scan_timeout}s.")
            print("    Please verify the ESP32 is powered and advertising.")
            return

        print(f"[+] Discovered: {dev_name} [{device.address}]")
        print(f"[*] Establishing BLE link to {device.address}...")

        if not self.output_csv:
            ts = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
            self.output_csv = f"sensor_record_spec_v1_{ts}.csv"

        print(f"[*] Target CSV log: {os.path.abspath(self.output_csv)}")

        with open(self.output_csv, "w", encoding="utf-8", buffering=1) as f:
            self.csv_file = f
            f.write(CSV_HEADER + "\n")
            f.flush()

            async with BleakClient(device) as client:
                self.client = client
                if not client.is_connected:
                    print("[-] Failed to establish connection.")
                    return

                print(f"[+] Connected to {device.name or dev_name}!")

                # Step 1: Subscriptions
                print("[*] Subscribing to CHR_STATUS and CHR_DATA...")
                await client.start_notify(CHR_STATUS, self.status_notification_handler)
                await client.start_notify(CHR_DATA, self.data_notification_handler)
                await asyncio.sleep(0.3)

                # Step 2: Handshake 2 (HELLO)
                print("[*] Initiating Handshake: sending HELLO (0x01)...")
                hello_payload = bytes([0x01, 0x01, 0x00, 0x00, 0x00, 0x00])
                await client.write_gatt_char(CHR_CTRL, hello_payload, response=True)

                try:
                    await asyncio.wait_for(self.hello_event.wait(), timeout=5.0)
                    h = self.hello_info
                    print(
                        f"\033[1;32m[+] Handshake HELLO Confirmed!\033[0m "
                        f"FW: {h.get('version')} | HW: Rev{h.get('hw')} | "
                        f"BootID: {h.get('boot_id')} | Calib: v{h.get('calib')} | "
                        f"Bat: {h.get('battery')}"
                    )
                except asyncio.TimeoutError:
                    print("[-] Timeout waiting for HELLO acknowledgment from firmware.")
                    return

                # Step 3: Start Session
                print("[*] Sending START (0x03) session command...")
                await client.write_gatt_char(CHR_CTRL, bytes([0x03]), response=True)

                try:
                    await asyncio.wait_for(self.start_event.wait(), timeout=5.0)
                    print("\033[1;32m[+] Session active! 2 Hz live stream started.\033[0m")
                except asyncio.TimeoutError:
                    print("[-] Timeout waiting for START acknowledgment from firmware.")
                    return

                print("\n=========================================================================================")
                print("  Live Telemetry Active (All 5 FSRs + ADS NTC + 6-DOF IMU Accel/Gyro)")
                print("  FSR order: [F1:Hallux, F2:MTH1, F3:MTH2, F4:MTH4, F5:Heel] in kPa")
                print("  Press Ctrl+C at any time to disconnect and finalize CSV log.")
                print("=========================================================================================\n")

                self.start_time = time.time()

                # Streaming & Cumulative ACK maintenance loop
                try:
                    while self.running and client.is_connected:
                        await asyncio.sleep(1.0)
                        # Send cumulative ACK every second if new samples arrived
                        if self.last_seq > self.last_acked_seq:
                            ack_payload = bytes([0x09]) + struct.pack("<I", self.last_seq)
                            try:
                                await client.write_gatt_char(CHR_CTRL, ack_payload, response=False)
                                self.last_acked_seq = self.last_seq
                            except Exception:
                                pass
                except asyncio.CancelledError:
                    pass
                finally:
                    # Clean shutdown
                    print("\n[*] Sending STOP session command...")
                    try:
                        await client.write_gatt_char(CHR_CTRL, bytes([0x04]), response=False)
                    except Exception:
                        pass

        print("=========================================================================================")
        print(f"[+] Total samples recorded: {self.sample_count}")
        print(f"[+] Total BLE batches received: {self.packet_count}")
        print(f"[+] CSV log saved to: {os.path.abspath(self.output_csv)}")
        print("=========================================================================================")


def main():
    parser = argparse.ArgumentParser(description="Track & log Spec v1 BLE insole telemetry")
    parser.add_argument("-o", "--output", help="Target CSV file path (default: auto-timestamped)")
    parser.add_argument("-t", "--timeout", type=float, default=60.0, help="Scan timeout in seconds (default: 60)")
    args = parser.parse_args()

    logger = SpecV1BleLogger(output_csv=args.output, scan_timeout=args.timeout)
    try:
        asyncio.run(logger.run())
    except KeyboardInterrupt:
        print("\n[*] Interrupted by user.")


if __name__ == "__main__":
    main()
