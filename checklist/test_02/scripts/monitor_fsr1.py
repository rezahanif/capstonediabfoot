#!/usr/bin/env python3
"""
DiabFoot Guard — FSR 1 (GPIO 0 / Hallux) Continuous Electrical Monitor
Live multimeter-style monitor streaming:
- 12-bit ADC Count (0 to 4095)
- Output Voltage Vout (0.000 V to 3.300 V)
- FSR Resistance R_fsr (Ohms / kOhms)
- Conductance G (uS)
- Insole Firmware Pressure (kPa)
- Estimated Force (N)

Hardware Circuit:
  3.3V ─── [FSR 1] ───┬─── [10k R_fixed] ─── GND
                      │
                      └─── [10k LPF Resistor] ─── [GPIO 0 / ADC1_CH0]
                                                  │
                                                  └─── [1uF LPF Cap] ─── GND
"""

import asyncio
import argparse
import datetime
import math
import os
import signal
import sys
import time
from typing import Optional, Dict, Any, List

from bleak import BleakClient, BleakScanner

# Hardware constants
VCC = 3.3
R_FIXED = 10000.0    # 10k fixed divider resistor
ADC_MAX = 4095.0
FSR_AREA_M2 = 7.85e-5

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
            "r_fsr_display": "> 10 MΩ (Open/Idle)",
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


class FSR1ContinuousMonitor:
    def __init__(self, output_dir: str = "../results", scroll: bool = False, side: str = "auto"):
        self.output_dir = output_dir
        self.scroll = scroll
        self.side_pref = side.lower()
        os.makedirs(output_dir, exist_ok=True)

        self.timestamp_str = datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        self.csv_path = os.path.join(output_dir, f"fsr1_continuous_log_{self.timestamp_str}.csv")

        self.client: Optional[BleakClient] = None
        self.total_samples = 0
        self.peak_adc = 0
        self.min_adc = 4095
        self.peak_vout = 0.0
        self.min_r_ohm = float("inf")
        self.peak_kpa = 0.0
        self.csv_file = None

        self.last_display_time = 0.0
        self.latest_data: Optional[Dict[str, Any]] = None

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
                    # parts[6] is p1 = FSR 1 (GPIO 0 / Hallux)
                    p1_kpa = float(parts[6])
                    elec = kpa_to_electrical(p1_kpa)
                    elec["timestamp"] = now_str

                    self.total_samples += 1
                    adc = elec["adc_raw"]
                    vout = elec["vout_v"]
                    r_ohm = elec["r_fsr_ohm"]
                    kpa = elec["kpa"]

                    if adc > self.peak_adc:
                        self.peak_adc = adc
                    if adc < self.min_adc and adc > 0:
                        self.min_adc = adc
                    if vout > self.peak_vout:
                        self.peak_vout = vout
                    if r_ohm < self.min_r_ohm and r_ohm > 0:
                        self.min_r_ohm = r_ohm
                    if kpa > self.peak_kpa:
                        self.peak_kpa = kpa

                    self.latest_data = elec

                    # Write to CSV log
                    if self.csv_file:
                        r_val = "" if math.isinf(r_ohm) else f"{r_ohm:.1f}"
                        self.csv_file.write(
                            f"{self.total_samples},{now_str},{adc},{vout:.3f},{r_val},"
                            f"{elec['conductance_us']},{elec['force_n']},{kpa}\n"
                        )
                        self.csv_file.flush()

                except ValueError:
                    pass

    async def run(self):
        print("=" * 80)
        print("  DiabFoot Guard — FSR 1 (GPIO 0 / Hallux) Continuous Electrical Monitor")
        print("  Hardware: 10k Divider + RC LPF (10k / 1uF) into ESP32-C3 ADC1_CH0")
        print(f"  Live CSV Log: {self.csv_path}")
        print("=" * 80)

        # Open CSV log
        self.csv_file = open(self.csv_path, "w")
        self.csv_file.write("sample_id,timestamp,adc_raw,vout_v,r_fsr_ohm,conductance_us,force_n,kpa\n")

        # BLE Scan
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
        print("=" * 80)
        print("  STREAMING LIVE ELECTRICAL PARAMETERS (Press Ctrl+C to stop)...")
        if self.scroll:
            print(f"{'Time':12} | {'ADC (0-4095)':12} | {'Vout (V)':10} | {'R_FSR':16} | {'Force (N)':10} | {'Pressure':10} | Gauge")
            print("-" * 80)
        print("=" * 80)

        async with BleakClient(device, timeout=15.0) as client:
            self.client = client
            await client.start_notify(profile["char_uuid"], self.on_notification)

            try:
                while True:
                    await asyncio.sleep(0.05)  # 20 Hz UI update
                    if self.latest_data:
                        d = self.latest_data
                        adc = d["adc_raw"]
                        vout = d["vout_v"]
                        r_str = d["r_fsr_display"]
                        fn = d["force_n"]
                        kpa = d["kpa"]
                        ts = d["timestamp"]

                        # Gauge bar (0 to 4095)
                        gauge_len = int((adc / 4095.0) * 20)
                        gauge = "#" * gauge_len + "-" * (20 - gauge_len)

                        if self.scroll:
                            now = time.time()
                            # Throttle scroll to ~5 lines/sec so it doesn't flood terminal
                            if now - self.last_display_time >= 0.20:
                                print(f"{ts:12} | {adc:5d} / 4095  | {vout:6.3f} V   | {r_str:16} | {fn:6.2f} N    | {kpa:6.1f} kPa | [{gauge}]")
                                self.last_display_time = now
                        else:
                            # Multimeter-style in-place single-line display
                            sys.stdout.write(
                                f"\r[{ts}] FSR1(GPIO 0) | ADC:{adc:4d}/4095 ({adc/4095*100:4.1f}%) | "
                                f"Vout:{vout:5.3f}V | R:{r_str:>16} | F:{fn:5.2f}N | {kpa:5.1f}kPa | [{gauge}]  "
                            )
                            sys.stdout.flush()

            except (asyncio.CancelledError, KeyboardInterrupt):
                pass
            finally:
                await client.stop_notify(profile["char_uuid"])

        if not self.scroll:
            sys.stdout.write("\n")

        if self.csv_file:
            self.csv_file.close()

        print("\n" + "=" * 80)
        print("  MONITORING SESSION SUMMARY — FSR 1 (GPIO 0 / Hallux)")
        print("=" * 80)
        print(f"  Total Samples Captured: {self.total_samples}")
        print(f"  Peak ADC Count:         {self.peak_adc} / 4095 ({self.peak_adc/4095*100:.1f}%)")
        min_adc_str = str(self.min_adc) if self.min_adc < 4095 else "0"
        print(f"  Min Non-Zero ADC:       {min_adc_str} / 4095")
        print(f"  Peak Voltage Vout:      {self.peak_vout:.3f} V")
        min_r_str = f"{self.min_r_ohm/1000.0:.2f} kΩ" if self.min_r_ohm < float('inf') else "N/A"
        print(f"  Minimum Resistance:     {min_r_str} (highest pressure)")
        print(f"  Peak Pressure:          {self.peak_kpa:.2f} kPa")
        print(f"  Saved CSV Log:          {self.csv_path}")
        print("=" * 80)


def main():
    parser = argparse.ArgumentParser(description="Continuous Live Electrical Monitor for FSR 1")
    parser.add_argument("--output-dir", default="../results", help="Directory to save CSV logs")
    parser.add_argument("--scroll", action="store_true", help="Scroll output line-by-line instead of multimeter in-place")
    parser.add_argument("--side", default="auto", choices=["auto", "left", "right"], help="Insole side")
    args = parser.parse_args()

    monitor = FSR1ContinuousMonitor(output_dir=args.output_dir, scroll=args.scroll, side=args.side)

    try:
        asyncio.run(monitor.run())
    except KeyboardInterrupt:
        pass


if __name__ == "__main__":
    main()
