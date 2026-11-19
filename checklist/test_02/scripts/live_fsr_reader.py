#!/usr/bin/env python3
"""
DiabFoot Guard — Verification Benchmark Engine
TEST-02: 5-Channel FSR Continuous Metrology Reader & MRE Benchmark

Displays live in real-time (5.0 Hz):
  - Raw ADC Count (0-4095)
  - Firmware Piecewise Calibrated Pressure (kPa)
  - Equivalent Measured Weight in kg and grams
  - Real-time ASCII Load Gauge

Allows user-determined weights for Option A:
  - Continuously watch values settle.
  - Optional [L] key to log calibration points against reference weight.
  - Automatically computes Mean Relative Error (MRE) < 15.0%.
  - Generates official TEST-02 Markdown, JSON, and CSV reports.
"""

import asyncio
import argparse
import datetime
import json
import math
import os
import sys
import time
from typing import List, Dict, Any, Optional, Tuple

from bleak import BleakClient, BleakScanner

# Known Insole BLE profiles
DEVICE_PROFILES = [
    {
        "side": "Right",
        "name": "Smart_Insole_Right",
        "service_uuid": "4fa2c732-ca9a-4c20-9492-c167df3c942c",
        "char_uuid": "beb5483e-36e1-4688-b7f5-ea07361b26c9"
    },
    {
        "side": "Left",
        "name": "Smart_Insole_Left",
        "service_uuid": "4fa2c732-ca9a-4c20-9492-c167df3c942b",
        "char_uuid": "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    }
]

# Physical Constants & Firmware Calibration Table
# 11-Node Piecewise LUT from firmware (insole_preproduction.ino lines 73-85)
CAL_LUT = [
    (0,    0,    0.0),   # 0 kg
    (893,  444,  2.0),   # 2 kg  (222.0 kPa/kg)
    (1825, 888,  4.0),   # 4 kg
    (1985, 1332, 6.0),   # 6 kg
    (2263, 1776, 8.0),   # 8 kg
    (2458, 2220, 10.0),  # 10 kg
    (2587, 2664, 12.0),  # 12 kg
    (2716, 3108, 14.0),  # 14 kg
    (2844, 3552, 16.0),  # 16 kg
    (2973, 3996, 18.0),  # 18 kg
    (3102, 4440, 20.0)   # 20 kg
]

SENSOR_CHANNELS = [
    {"id": 1, "name": "Hallux (S1 - Big Toe)",    "raw_field": "p_hallux", "gpio": "ADS A1"},
    {"id": 2, "name": "1st Meta (S2 - MTH 1)",    "raw_field": "p_mth1",   "gpio": "GPIO 1"},
    {"id": 3, "name": "2nd Meta (S3 - MTH 2)",    "raw_field": "p_mth2",   "gpio": "GPIO 3"},
    {"id": 4, "name": "Heel (S5 - Physical Heel)","raw_field": "p_mth4",   "gpio": "GPIO 4"},
    {"id": 5, "name": "4th Meta (S4 - MTH 4)",    "raw_field": "p_heel",   "gpio": "GPIO 0"},
]


def kpa_to_adc_and_weight(kpa: float) -> Tuple[int, float, float]:
    """
    Inverse piecewise interpolation matching firmware:
    Returns: (adc_count, weight_kg, weight_grams)
    """
    if kpa <= 0.001:
        return 0, 0.0, 0.0

    # Piecewise interpolation across 10 segments
    for i in range(len(CAL_LUT) - 1):
        adc_lo, kpa_lo, kg_lo = CAL_LUT[i]
        adc_hi, kpa_hi, kg_hi = CAL_LUT[i + 1]
        if kpa_lo <= kpa <= kpa_hi:
            delta_kpa = kpa_hi - kpa_lo
            frac = (kpa - kpa_lo) / delta_kpa if delta_kpa > 0 else 0.0
            adc = adc_lo + frac * (adc_hi - adc_lo)
            kg = kg_lo + frac * (kg_hi - kg_lo)
            return int(round(adc)), round(kg, 3), round(kg * 1000.0, 1)

    # Extrapolation above 20 kg (constant slope: 3.442 kPa/ADC)
    adc_last, kpa_last, kg_last = CAL_LUT[-1]
    excess_kpa = kpa - kpa_last
    adc = adc_last + (excess_kpa / 3.442)
    kg = kg_last + (excess_kpa / 222.0)
    return int(round(adc)), round(kg, 3), round(kg * 1000.0, 1)


def render_gauge(adc: int, max_adc: int = 3500, width: int = 18) -> str:
    ratio = min(1.0, max(0.0, adc / max_adc))
    filled = int(round(ratio * width))
    return "#" * filled + "-" * (width - filled)


class LiveFSRMetrologyReader:
    def __init__(self, output_dir: str = "../results", target_side: str = "AUTO", swap_halux_meta2: Optional[bool] = None):
        self.output_dir = output_dir
        self.target_side = (target_side or "AUTO").upper()
        self.swap_halux_meta2 = swap_halux_meta2
        os.makedirs(output_dir, exist_ok=True)

        self.client: Optional[BleakClient] = None
        self.detected_device: Optional[Dict[str, Any]] = None
        self.packets_received = 0
        self.latest_kpa = [0.0, 0.0, 0.0, 0.0, 0.0]
        self.tare_offsets = [0.0, 0.0, 0.0, 0.0, 0.0]
        self.sync_event = asyncio.Event()

        # Recorded benchmark points
        self.logged_points: List[Dict[str, Any]] = []

    async def scan_and_connect(self) -> bool:
        print(f"\n🔍 Scanning for DiabFoot Smart Insole (Side: {self.target_side}, 5 seconds)...")
        devices = await BleakScanner.discover(timeout=5.0, return_adv=True)

        candidates = []
        for d, adv in devices.values():
            name = adv.local_name or d.name or ""
            uuids = [str(u).lower() for u in (adv.service_uuids or [])]
            for prof in DEVICE_PROFILES:
                if prof["name"].lower() in name.lower() or prof["service_uuid"].lower() in uuids:
                    candidates.append({"profile": prof, "device": d, "rssi": adv.rssi})
                    break

        if not candidates:
            print("❌ No insole detected. Ensure power switch is ON.")
            return False

        if self.target_side != "AUTO":
            filtered = [c for c in candidates if c["profile"]["side"].upper() == self.target_side]
            if filtered:
                candidates = filtered
            else:
                print(f"⚠️ Requested side '{self.target_side}' not in candidate list. Available: {[c['profile']['side'] for c in candidates]}")

        selected = max(candidates, key=lambda c: c["rssi"])
        prof = selected["profile"]
        dev = selected["device"]
        print(f"✅ Discovered: {prof['name']} ({prof['side']}) | RSSI: {selected['rssi']} dBm")

        print(f"🔗 Connecting to {dev.address}...")
        client = BleakClient(dev.address)
        try:
            await client.connect(timeout=10.0)
            print("🎉 Connected! Subscribing to telemetry stream...")
            await client.start_notify(prof["char_uuid"], self.on_telemetry_packet)
        except Exception as e:
            print(f"❌ Connection failed: {e}")
            return False

        self.detected_device = {**prof, "address": dev.address, "rssi": selected["rssi"]}
        self.client = client

        # Wait for 5 packets to verify sync
        try:
            await asyncio.wait_for(self.sync_event.wait(), timeout=6.0)
            print("✅ Telemetry synchronized at 5.0 Hz!")
            return True
        except asyncio.TimeoutError:
            print("❌ Telemetry sync timeout. Characteristic did not stream packets.")
            return False

    def on_telemetry_packet(self, sender: Any, data: bytearray):
        try:
            line = data.decode("utf-8").strip()
        except Exception:
            return

        if line.startswith("T"):
            return

        parts = line.split(",")
        if len(parts) < 11:
            return

        try:
            # parts[6..10] = p_hallux, p_mth1, p_mth2, p_mth4, p_heel
            kpa_vals = [float(parts[6]), float(parts[7]), float(parts[8]), float(parts[9]), float(parts[10])]
        except ValueError:
            return

        # On Left insole: Hallux (S1) and Second Meta (S3) flex traces are physically swapped
        is_left = (self.detected_device and self.detected_device.get("side") == "Left") or self.target_side == "LEFT"
        should_swap = self.swap_halux_meta2 if self.swap_halux_meta2 is not None else is_left
        if should_swap:
            kpa_vals[0], kpa_vals[2] = kpa_vals[2], kpa_vals[0]

        self.latest_kpa = kpa_vals
        self.packets_received += 1
        if self.packets_received == 5:
            self.sync_event.set()

    def set_tare(self):
        """Zeroes all channels to current resting baseline."""
        self.tare_offsets = list(self.latest_kpa)

    async def run_live_dashboard(self):
        """Main continuous live dashboard loop."""
        print("\nStarting continuous metrology display...")
        time.sleep(0.5)

        first_draw = True
        try:
            while True:
                lines = []
                dev = self.detected_device
                is_left = (dev and dev.get("side") == "Left") or self.target_side == "LEFT"
                is_swapped = self.swap_halux_meta2 if self.swap_halux_meta2 is not None else is_left
                swap_notice = " | 🔄 Swap Active: Hallux (S1) <-> 2nd Meta (S3)" if is_swapped else ""

                lines.append("=" * 105)
                lines.append(f"  DiabFoot Guard — 5-Channel FSR Continuous Metrology Reader (TEST-02 Bench)")
                lines.append(f"  Target: {dev['name']} ({dev['side']}){swap_notice} | Packets: {self.packets_received} | Stream: 5.0 Hz")
                lines.append("=" * 105)
                lines.append("  Ch | Sensor Anatomical Zone  | Hardware Pin    | Raw ADC | Pressure (kPa) | Weight (kg) | Weight (g) | Gauge")
                lines.append("  ---+-------------------------+-----------------+---------+----------------+-------------+------------+--------------------")

                total_kpa = 0.0
                total_kg = 0.0

                for i, meta in enumerate(SENSOR_CHANNELS):
                    raw_kpa = self.latest_kpa[i]
                    net_kpa = max(0.0, raw_kpa - self.tare_offsets[i])
                    adc, kg, g = kpa_to_adc_and_weight(net_kpa)
                    gauge = render_gauge(adc)

                    total_kpa += net_kpa
                    total_kg += kg

                    pin_label = meta['gpio']
                    if is_swapped:
                        if i == 0:
                            pin_label = "GPIO 3 (swapped)"
                        elif i == 2:
                            pin_label = "ADS A1 (swapped)"

                    lines.append(
                        f"  S{meta['id']} | {meta['name']:23s} | {pin_label:15s} | "
                        f"{adc:5d}   | {net_kpa:9.1f} kPa  | {kg:7.3f} kg  | {g:7.1f} g  | [{gauge}]"
                    )

                lines.append("  ---+-------------------------+-----------------+---------+----------------+-------------+------------+--------------------")
                lines.append(f"  Σ  | Total Plantar Load      | All Zones       |         | {total_kpa:9.1f} kPa  | {total_kg:7.3f} kg  | {total_kg*1000:7.1f} g  |")
                lines.append("=" * 105)

                num_points = len(self.logged_points)
                lines.append(f"  Logged Points: {num_points} recorded | Tare Status: {'Active' if any(t > 0 for t in self.tare_offsets) else 'None (0.0)'}")
                lines.append("  Commands: Press [Ctrl+C] to open Interactive Menu (Log Point, Tare, Save Report)")

                output = "\n".join(lines)

                if first_draw:
                    sys.stdout.write(output + "\n")
                    first_draw = False
                else:
                    sys.stdout.write(f"\033[{len(lines)}A\r" + output + "\n")
                sys.stdout.flush()

                await asyncio.sleep(0.15)

        except (asyncio.CancelledError, KeyboardInterrupt):
            await self.handle_menu()

    async def handle_menu(self):
        """Interactive menu when user pauses live stream."""
        sys.stdout.write("\n")
        while True:
            print("\n" + "=" * 65)
            print("  TEST-02 BENCHMARK INTERACTIVE MENU")
            print("=" * 65)
            print("  [1] Resume Continuous Live Display")
            print("  [2] Log / Record a Benchmark Point (Option A)")
            print("  [3] Set Tare / Zero Current Baseline")
            print("  [4] View Currently Logged Benchmark Points & MRE")
            print("  [5] Save Final Results & Generate Official Report")
            print("  [6] Exit without saving")
            print("=" * 65)

            choice = input("Select option (1-6) [default 1]: ").strip()
            if choice == "" or choice == "1":
                await self.run_live_dashboard()
                break
            elif choice == "2":
                await self.interactive_log_point()
            elif choice == "3":
                self.set_tare()
                print("✅ Tare baseline locked! All channels zeroed.")
            elif choice == "4":
                self.display_logged_summary()
            elif choice == "5":
                self.save_reports()
                print("✅ Test session completed. Exiting.")
                break
            elif choice == "6":
                confirm = input("Exit without saving? (y/N): ").strip().lower()
                if confirm == "y":
                    break

    async def interactive_log_point(self):
        """Records a point against user's custom reference weight."""
        print("\n--- LOG BENCHMARK POINT ---")
        print("Select Sensor Channel you loaded:")
        for meta in SENSOR_CHANNELS:
            print(f"  [{meta['id']}] {meta['name']}")

        ch_in = input("Enter sensor number (1-5): ").strip()
        try:
            ch_idx = int(ch_in) - 1
            if ch_idx < 0 or ch_idx > 4:
                print("❌ Invalid channel.")
                return
        except ValueError:
            print("❌ Invalid input.")
            return

        ref_in = input("Enter your known reference weight in kg (e.g. 0.500, 1.250): ").strip()
        try:
            ref_kg = float(ref_in)
            if ref_kg <= 0:
                print("❌ Weight must be positive.")
                return
        except ValueError:
            print("❌ Invalid weight format.")
            return

        # Sample latest reading
        raw_kpa = self.latest_kpa[ch_idx]
        net_kpa = max(0.0, raw_kpa - self.tare_offsets[ch_idx])
        adc, meas_kg, meas_g = kpa_to_adc_and_weight(net_kpa)

        ref_kpa = ref_kg * 222.0  # 222 kPa per kg in piecewise table
        abs_err_kg = abs(meas_kg - ref_kg)
        rel_err_pct = (abs_err_kg / ref_kg * 100.0) if ref_kg > 0 else 0.0

        point = {
            "timestamp": datetime.datetime.now().strftime("%H:%M:%S"),
            "channel_id": SENSOR_CHANNELS[ch_idx]["id"],
            "sensor_name": SENSOR_CHANNELS[ch_idx]["name"],
            "ref_weight_kg": round(ref_kg, 3),
            "ref_pressure_kpa": round(ref_kpa, 1),
            "meas_adc": adc,
            "meas_pressure_kpa": round(net_kpa, 1),
            "meas_weight_kg": round(meas_kg, 3),
            "meas_weight_g": round(meas_g, 1),
            "abs_error_kg": round(abs_err_kg, 3),
            "relative_error_pct": round(rel_err_pct, 2),
            "is_pass": rel_err_pct < 15.0
        }
        self.logged_points.append(point)

        status_icon = "✅" if rel_err_pct < 15.0 else "⚠️"
        print(f"\n{status_icon} Recorded: {point['sensor_name']}")
        print(f"   • Reference Weight   : {ref_kg:.3f} kg ({ref_kpa:.1f} kPa)")
        print(f"   • Measured Reading   : {meas_kg:.3f} kg ({net_kpa:.1f} kPa | ADC: {adc})")
        print(f"   • Relative Error     : {rel_err_pct:.2f}% (Threshold: < 15.0%)")

        running_mre = sum(p["relative_error_pct"] for p in self.logged_points) / len(self.logged_points)
        print(f"   • Current Running MRE: {running_mre:.2f}% across {len(self.logged_points)} points")

    def display_logged_summary(self):
        if not self.logged_points:
            print("\n⚠️ No points recorded yet.")
            return

        print("\n" + "=" * 90)
        print(f"  CURRENT LOGGED POINTS ({len(self.logged_points)} points recorded)")
        print("=" * 90)
        print("  Idx | Sensor Zone             | Ref (kg) | Meas (kg) | Meas (kPa) | Rel Error (%) | Status")
        print("  ----+-------------------------+----------+-----------+------------+---------------+-------")
        for i, p in enumerate(self.logged_points, 1):
            stat = "PASS" if p["is_pass"] else "FAIL"
            print(f"  {i:3d} | {p['sensor_name']:23s} | {p['ref_weight_kg']:6.3f}kg | {p['meas_weight_kg']:7.3f}kg | {p['meas_pressure_kpa']:7.1f}kPa | {p['relative_error_pct']:10.2f}% | {stat}")

        mre = sum(p["relative_error_pct"] for p in self.logged_points) / len(self.logged_points)
        print("  ----+-------------------------+----------+-----------+------------+---------------+-------")
        print(f"  Overall Mean Relative Error (MRE): {mre:.2f}%  (Passing Target: < 15.0%)")
        print("=" * 90)

    def save_reports(self):
        if not self.logged_points:
            print("⚠️ No points logged to save.")
            return

        ts = datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        csv_file = os.path.join(self.output_dir, f"test_02_weight_accuracy_{ts}.csv")
        json_file = os.path.join(self.output_dir, f"test_02_summary_{ts}.json")
        md_file = os.path.join(self.output_dir, f"TEST_02_REPORT_{ts}.md")

        overall_mre = sum(p["relative_error_pct"] for p in self.logged_points) / len(self.logged_points)
        is_passed = overall_mre < 15.0

        # Save CSV
        import csv
        with open(csv_file, "w", newline="") as f:
            writer = csv.DictWriter(f, fieldnames=list(self.logged_points[0].keys()))
            writer.writeheader()
            writer.writerows(self.logged_points)
        print(f"💾 Raw CSV saved: {csv_file}")

        # Save JSON
        summary = {
            "test_id": "TEST-02",
            "test_name": "Plantar Pressure FSR Weight Accuracy & Mean Relative Error (MRE)",
            "timestamp": ts,
            "device": self.detected_device,
            "total_points": len(self.logged_points),
            "overall_mre_pct": round(overall_mre, 2),
            "passing_threshold_pct": 15.0,
            "status": "PASSED" if is_passed else "FAILED",
            "records": self.logged_points
        }
        with open(json_file, "w") as f:
            json.dump(summary, f, indent=2)
        print(f"💾 Summary JSON saved: {json_file}")

        # Save Markdown Report
        md_rows = ""
        for p in self.logged_points:
            stat = "PASSED" if p["is_pass"] else "FLAGGED"
            md_rows += f"| **{p['sensor_name']}** | `{p['ref_weight_kg']:.3f} kg` | `{p['meas_weight_kg']:.3f} kg` | `{p['meas_pressure_kpa']:.1f} kPa` | `{p['meas_adc']}` | **`{p['relative_error_pct']:.2f}%`** | {stat} |\n"

        md_content = f"""# TEST-02: Plantar Pressure FSR Weight Accuracy & MRE Benchmark Report

| Evaluation Metric | Recorded Value |
| :--- | :--- |
| **Test ID** | `TEST-02` |
| **Timestamp** | `{ts}` |
| **Monitored Hardware** | `{self.detected_device.get('name', 'Smart Insole')} ({self.detected_device.get('side', 'Unknown')})` |
| **Total Test Points** | **`{len(self.logged_points)} points`** |
| **Overall Mean Relative Error (MRE)** | **`{overall_mre:.2f}%`** |
| **Target Passing Threshold** | **`< 15.0%`** |
| **Final Result** | **`{'PASSED' if is_passed else 'FAILED'}`** |

---

### 1. Empirical Calibration Verification Matrix
| Sensor Channel | Known Reference ($W_{{\\text{{ref}}}}$) | Measured Weight ($W_{{\\text{{meas}}}}$) | Measured Pressure | ADC Count | Relative Error (%) | Status |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
{md_rows}

---

### 2. Statistical Analysis & Capstone Evaluation
$$\\text{{Relative Error}}_i = \\frac{{|W_{{\\text{{meas}}, i}} - W_{{\\text{{ref}}, i}}|}}{{W_{{\\text{{ref}}, i}}}} \\times 100\\%$$

$$\\text{{MRE}} = \\frac{{1}}{{{len(self.logged_points)}}} \\sum_{{i=1}}^{{{len(self.logged_points)}}} \\text{{Relative Error}}_i = \\mathbf{{{overall_mre:.2f}\\%}}$$

{'✅ The measured Mean Relative Error (MRE) satisfies the capstone accuracy requirement (< 15.0%).' if is_passed else '❌ The measured MRE did not satisfy the 15.0% threshold.'}
"""
        with open(md_file, "w") as f:
            f.write(md_content)
        print(f"📄 Markdown report saved: {md_file}")


def main():
    parser = argparse.ArgumentParser(description="Live Continuous FSR Metrology Reader & MRE Benchmark")
    parser.add_argument("--side", "-s", choices=["LEFT", "RIGHT", "AUTO"], default="AUTO",
                        help="Target insole side (default: AUTO)")
    parser.add_argument("--swap-halux-meta2", dest="swap_halux_meta2", action="store_true", default=None,
                        help="Swap Hallux (S1) and 2nd Meta (S3) channels (default: True for LEFT insole)")
    parser.add_argument("--no-swap-halux-meta2", dest="swap_halux_meta2", action="store_false",
                        help="Disable swapping of Hallux and 2nd Meta")
    parser.add_argument("--output-dir", default=os.path.join(os.path.dirname(__file__), "../results"),
                        help="Directory to save results")
    args = parser.parse_args()

    engine = LiveFSRMetrologyReader(output_dir=args.output_dir, target_side=args.side, swap_halux_meta2=args.swap_halux_meta2)

    async def runner():
        ok = await engine.scan_and_connect()
        if not ok:
            return
        await engine.run_live_dashboard()

    try:
        asyncio.run(runner())
    except KeyboardInterrupt:
        pass


if __name__ == "__main__":
    main()
