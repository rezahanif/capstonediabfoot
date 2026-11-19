#!/usr/bin/env python3
"""
DiabFoot Guard — 5-Channel Plantar Pressure FSR Continuous Electrical Monitor
Simultaneously monitors all 5 FSR channels in real-time (20 Hz):
  - Channel 1 (p1): GPIO 0 (Hallux / Jempol)
  - Channel 2 (p2): GPIO 1 (MTH 1)
  - Channel 3 (p3): GPIO 2 (MTH 2)
  - Channel 4 (p4): GPIO 3 (MTH 4)
  - Channel 5 (p5): GPIO 4 (Heel / Tumit)

Displays live:
  ADC Count (0-4095), Vout (V), Resistance (Ω/kΩ/MΩ), Force (N), Pressure (kPa)
"""

import asyncio
import argparse
import datetime
import math
import os
import sys
import time
from typing import Optional, Dict, Any, List

from bleak import BleakClient, BleakScanner

# Hardware constants
VCC = 3.3
R_FIXED = 10000.0    # 10k fixed divider resistor
ADC_MAX = 4095.0
FSR_AREA_M2 = 7.85e-5

SENSOR_META = [
    {"id": 1, "name": "Hallux", "gpio": 0, "idx": 6},
    {"id": 2, "name": "MTH 1",  "gpio": 1, "idx": 7},
    {"id": 3, "name": "MTH 2",  "gpio": 2, "idx": 8},
    {"id": 4, "name": "MTH 4",  "gpio": 3, "idx": 9},
    {"id": 5, "name": "Heel",   "gpio": 4, "idx": 10},
]

DEVICE_PROFILES = {
    "left": {
        "name": "Smart_Insole_Left",
        "service_uuid": "4fa2c732-ca9a-4c20-9492-c167df3c942b",
        "char_uuid": "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    },
    "right": {
        "name": "Smart_Insole_Right",
        "service_uuid": "4fa2c732-ca9a-4c20-9492-c167df3c942c",
        "char_uuid": "beb5483e-36e1-4688-b7f5-ea07361b26c9"
    }
}


def kpa_to_electrical(kpa: float) -> Dict[str, Any]:
    """
    Inverse calculation from firmware pressure pipeline:
      P_kPa = (force_N / 7.85e-5) / 1000.0  -> force_N = P_kPa * 0.0785398
      force_N = (G <= 1000) ? (G / 80.0) : ((G - 1000.0) / 30.0)
      R_fsr = 1,000,000.0 / G
      Vout = VCC * R_fixed / (R_fsr + R_fixed)
      ADC = (Vout / VCC) * 4095
    """
    if kpa <= 0.005:
        return {
            "kpa": 0.0,
            "force_n": 0.0,
            "conductance_us": 0.0,
            "r_fsr_ohm": float("inf"),
            "r_fsr_display": "> 10 MΩ (Idle)",
            "vout_v": 0.000,
            "adc_raw": 0
        }

    force_n = kpa * (FSR_AREA_M2 * 1000.0)

    if force_n <= 12.5:
        conductance_us = force_n * 80.0
    else:
        conductance_us = (force_n * 30.0) + 1000.0

    if conductance_us > 0.01:
        r_fsr_ohm = 1_000_000.0 / conductance_us
    else:
        r_fsr_ohm = 9_999_999.0

    vout_v = VCC * R_FIXED / (r_fsr_ohm + R_FIXED)
    adc_raw = int(round((vout_v / VCC) * ADC_MAX))
    adc_raw = min(4095, max(0, adc_raw))

    if r_fsr_ohm >= 1_000_000:
        r_display = f"{r_fsr_ohm / 1_000_000.0:.2f} MΩ"
    elif r_fsr_ohm >= 1_000:
        r_display = f"{r_fsr_ohm / 1_000.0:.2f} kΩ"
    else:
        r_display = f"{r_fsr_ohm:.1f} Ω"

    return {
        "kpa": round(kpa, 2),
        "force_n": round(force_n, 3),
        "conductance_us": round(conductance_us, 1),
        "r_fsr_ohm": r_fsr_ohm,
        "r_fsr_display": r_display,
        "vout_v": round(vout_v, 3),
        "adc_raw": adc_raw
    }


class AllFSRContinuousMonitor:
    def __init__(self, output_dir: str = "../results", scroll: bool = False,
                 side: str = "auto", duration: float = 0.0):
        self.output_dir = output_dir
        self.scroll = scroll
        self.side_pref = side.lower()
        self.duration = duration
        os.makedirs(output_dir, exist_ok=True)

        self.timestamp_str = datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        self.csv_path = os.path.join(output_dir, f"fsr_all5_continuous_log_{self.timestamp_str}.csv")

        self.client: Optional[BleakClient] = None
        self.total_samples = 0
        self.start_time = 0.0
        self.last_display_time = 0.0

        # Stats per channel (0 to 4)
        self.peak_adc = [0] * 5
        self.min_adc = [4095] * 5
        self.peak_vout = [0.0] * 5
        self.min_r_ohm = [float("inf")] * 5
        self.peak_kpa = [0.0] * 5

        self.latest_readings: Optional[List[Dict[str, Any]]] = None
        self.latest_timestamp = ""
        self.csv_file = None
        self.has_drawn_grid = False

    def on_notification(self, sender, data: bytearray):
        raw = data.decode("utf-8", errors="replace").strip()
        lines = raw.split("\n")

        now_str = datetime.datetime.now().strftime("%H:%M:%S.%f")[:-3]

        for line in lines:
            line = line.strip()
            if not line or line.startswith("T,"):
                continue

            parts = line.split(",")
            if len(parts) >= 11:
                try:
                    channels_data = []
                    for i in range(5):
                        kpa_val = float(parts[6 + i])
                        elec = kpa_to_electrical(kpa_val)
                        channels_data.append(elec)

                        adc = elec["adc_raw"]
                        vout = elec["vout_v"]
                        r_ohm = elec["r_fsr_ohm"]
                        kpa = elec["kpa"]

                        if adc > self.peak_adc[i]:
                            self.peak_adc[i] = adc
                        if adc < self.min_adc[i] and adc > 0:
                            self.min_adc[i] = adc
                        if vout > self.peak_vout[i]:
                            self.peak_vout[i] = vout
                        if r_ohm < self.min_r_ohm[i] and r_ohm > 0:
                            self.min_r_ohm[i] = r_ohm
                        if kpa > self.peak_kpa[i]:
                            self.peak_kpa[i] = kpa

                    self.total_samples += 1
                    self.latest_readings = channels_data
                    self.latest_timestamp = now_str

                    if self.csv_file:
                        row_vals = [str(self.total_samples), now_str]
                        for c in channels_data:
                            r_val = "" if math.isinf(c["r_fsr_ohm"]) else f"{c['r_fsr_ohm']:.1f}"
                            row_vals.extend([
                                str(c["adc_raw"]),
                                f"{c['vout_v']:.3f}",
                                r_val,
                                f"{c['kpa']:.2f}"
                            ])
                        self.csv_file.write(",".join(row_vals) + "\n")
                        self.csv_file.flush()

                except ValueError:
                    pass

    async def run(self):
        print("=" * 105)
        print("  DiabFoot Guard — 5-Channel Plantar Pressure FSR Continuous Electrical Monitor")
        print("  Hardware: 5x High-Side FSR with 10k Divider & RC LPF into ESP32-C3 ADC")
        print(f"  Live CSV Log: {self.csv_path}")
        if self.duration > 0:
            print(f"  Duration Limit: {self.duration} seconds")
        print("=" * 105)

        # Open CSV log
        self.csv_file = open(self.csv_path, "w")
        header = ["sample_id", "timestamp"]
        for i in range(1, 6):
            header.extend([f"s{i}_adc", f"s{i}_vout_v", f"s{i}_r_ohm", f"s{i}_kpa"])
        self.csv_file.write(",".join(header) + "\n")

        # Scan BLE
        target_names = []
        if self.side_pref == "left":
            target_names = [DEVICE_PROFILES["left"]["name"]]
        elif self.side_pref == "right":
            target_names = [DEVICE_PROFILES["right"]["name"]]
        else:
            target_names = [DEVICE_PROFILES["left"]["name"], DEVICE_PROFILES["right"]["name"]]

        print(f"[*] Scanning for insole ({', '.join(target_names)})...")

        def match_insole(d, adv):
            name = d.name or adv.local_name or ""
            return any(t in name for t in target_names)

        device = await BleakScanner.find_device_by_filter(match_insole, timeout=10.0)

        if not device:
            print(f"[-] ERROR: Insole device not found. Ensure insole is powered on.")
            return

        dev_name = device.name or ""
        profile = DEVICE_PROFILES["right"] if "Right" in dev_name else DEVICE_PROFILES["left"]
        print(f"[+] Connected to {device.name} ({device.address})")
        print(f"[+] Subscribing to Stream Characteristic: {profile['char_uuid']}")
        print("=" * 105)

        self.start_time = time.time()

        async with BleakClient(device, timeout=15.0) as client:
            self.client = client
            await client.start_notify(profile["char_uuid"], self.on_notification)

            if self.scroll:
                print(f"{'Time':12} | {'S1 ADC (Hallux)':15} | {'S2 ADC (MTH1)':15} | {'S3 ADC (MTH2)':15} | {'S4 ADC (MTH4)':15} | {'S5 ADC (Heel)':15}")
                print("-" * 105)

            try:
                while True:
                    await asyncio.sleep(0.05)  # 20 Hz UI refresh
                    if self.duration > 0 and (time.time() - self.start_time) >= self.duration:
                        break

                    if not self.latest_readings:
                        continue

                    readings = self.latest_readings
                    ts = self.latest_timestamp

                    if self.scroll:
                        now = time.time()
                        if now - self.last_display_time >= 0.20:
                            s1 = f"{readings[0]['adc_raw']} ({readings[0]['kpa']:.1f}k)"
                            s2 = f"{readings[1]['adc_raw']} ({readings[1]['kpa']:.1f}k)"
                            s3 = f"{readings[2]['adc_raw']} ({readings[2]['kpa']:.1f}k)"
                            s4 = f"{readings[3]['adc_raw']} ({readings[3]['kpa']:.1f}k)"
                            s5 = f"{readings[4]['adc_raw']} ({readings[4]['kpa']:.1f}k)"
                            print(f"{ts:12} | {s1:>15} | {s2:>15} | {s3:>15} | {s4:>15} | {s5:>15}")
                            self.last_display_time = now
                    else:
                        # Multimeter Grid in-place dashboard
                        lines = []
                        elapsed = time.time() - self.start_time
                        rate = self.total_samples / max(0.001, elapsed)

                        lines.append(f"[{ts}]  Active Stream: 20 Hz | Total Samples: {self.total_samples} ({rate:.1f} pkts/s) | Press Ctrl+C to Stop")
                        lines.append(" Ch | Anatomical  | Pin  | ADC (0-4095) | Vout (V) | Resistance    | Force (N) | Pressure  | Visual Load Gauge")
                        lines.append("----+-------------+------+--------------+----------+---------------+-----------+-----------+--------------------")

                        for i, meta in enumerate(SENSOR_META):
                            d = readings[i]
                            adc = d["adc_raw"]
                            pct = (adc / 4095.0) * 100.0
                            vout = d["vout_v"]
                            r_str = d["r_fsr_display"]
                            fn = d["force_n"]
                            kpa = d["kpa"]

                            gauge_len = int((adc / 4095.0) * 20)
                            gauge = "#" * gauge_len + "-" * (20 - gauge_len)

                            lines.append(
                                f" S{meta['id']} | {meta['name']:11} | GP {meta['gpio']} | "
                                f"{adc:4d} ({pct:4.1f}%) | {vout:6.3f} V | {r_str:>13} | "
                                f"{fn:6.2f} N  | {kpa:6.1f}kPa | [{gauge}]"
                            )

                        lines.append("-----------------------------------------------------------------------------------------------------")

                        output = "\n".join(lines)
                        if not self.has_drawn_grid:
                            sys.stdout.write(output + "\n")
                            self.has_drawn_grid = True
                        else:
                            # Move cursor up by (len(lines)) lines
                            num_lines = len(lines)
                            sys.stdout.write(f"\033[{num_lines}A\r" + output + "\n")
                        sys.stdout.flush()

            except (asyncio.CancelledError, KeyboardInterrupt):
                pass
            finally:
                await client.stop_notify(profile["char_uuid"])

        if self.csv_file:
            self.csv_file.close()

        print("\n" + "=" * 105)
        print("  5-CHANNEL MONITORING SESSION SUMMARY")
        print("=" * 105)
        print(f"  Total Packets Captured: {self.total_samples}")
        print("  Channel Breakdown:")
        for i, meta in enumerate(SENSOR_META):
            min_r_str = f"{self.min_r_ohm[i]/1000.0:.2f} kΩ" if self.min_r_ohm[i] < float('inf') else "N/A"
            print(
                f"    - S{meta['id']} ({meta['name']} - GPIO {meta['gpio']}): "
                f"Peak ADC = {self.peak_adc[i]:4d}/4095 ({self.peak_adc[i]/4095*100:4.1f}%) | "
                f"Peak Vout = {self.peak_vout[i]:.3f} V | "
                f"Min R = {min_r_str} | Peak kPa = {self.peak_kpa[i]:.1f} kPa"
            )
        print(f"\n  Saved Comprehensive 5-Channel CSV Log: {self.csv_path}")
        print("=" * 105)


def main():
    parser = argparse.ArgumentParser(description="Continuous Live Electrical Monitor for all 5 FSRs")
    parser.add_argument("--output-dir", default="../results", help="Directory to save CSV logs")
    parser.add_argument("--scroll", action="store_true", help="Scroll output line-by-line instead of dashboard in-place")
    parser.add_argument("--side", default="auto", choices=["auto", "left", "right"], help="Insole side")
    parser.add_argument("--duration", type=float, default=0.0, help="Run for duration in seconds (0 = indefinite)")
    args = parser.parse_args()

    monitor = AllFSRContinuousMonitor(
        output_dir=args.output_dir,
        scroll=args.scroll,
        side=args.side,
        duration=args.duration
    )

    try:
        asyncio.run(monitor.run())
    except KeyboardInterrupt:
        pass


if __name__ == "__main__":
    main()
