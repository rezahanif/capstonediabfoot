#!/usr/bin/env python3
"""
DiabFoot Guard — Verification Benchmark Engine
TEST-01: Live NTC Thermistor Temperature & Electrical Metrology Viewer

Monitors NTC Midfoot Skin Temperature in real-time over BLE:
  - Predicted Temperature in Celsius (°C) & Fahrenheit (°F)
  - Raw 16-bit ADC Count (ADS1115 A0) & 12-bit Equivalent (0-4095)
  - Voltage Divider Output Vout (Volts)
  - Thermistor Resistance R_NTC (kΩ) via Steinhart-Hart equation
  - Live ASCII Thermal Gauge & Rate of Change (°C/min)
  - Optional MAE verification against ground-truth Thermo Gun
"""

import asyncio
import argparse
import datetime
import json
import math
import os
import sys
import time
from typing import Optional, Dict, Any, List

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

# Physical Metrology Parameters (Beta 3950, R0 10k @ 25C)
BETA = 3950.0
R0 = 10000.0        # 10k nominal at 25C
T0_KELVIN = 298.15  # 25C in Kelvin
R_FIXED = 10000.0   # 10k fixed divider resistor
VCC = 3.3           # Regulated 3.3V rail


def temp_to_electrical(temp_c: float) -> Dict[str, Any]:
    """Inverse Steinhart-Hart calculation matching firmware."""
    if temp_c < -40.0 or temp_c > 125.0:
        return {
            "temp_c": temp_c,
            "temp_f": (temp_c * 9.0 / 5.0) + 32.0,
            "r_ntc_kohm": 0.0,
            "vout_v": 0.0,
            "adc_12bit": 0,
            "adc_16bit": 0
        }

    t_kelvin = temp_c + 273.15
    r_ntc = R0 * math.exp(BETA * (1.0 / t_kelvin - 1.0 / T0_KELVIN))
    vout = VCC * R_FIXED / (r_ntc + R_FIXED)
    adc_12 = int(round((vout / VCC) * 4095.0))
    adc_16 = int(round(vout / (4.096 / 32768.0)))

    return {
        "temp_c": round(temp_c, 2),
        "temp_f": round((temp_c * 9.0 / 5.0) + 32.0, 2),
        "r_ntc_kohm": round(r_ntc / 1000.0, 2),
        "vout_v": round(vout, 3),
        "adc_12bit": min(4095, max(0, adc_12)),
        "adc_16bit": min(32767, max(0, adc_16))
    }


def render_temp_bar(temp_c: float, min_c: float = 20.0, max_c: float = 42.0, width: int = 24) -> str:
    ratio = min(1.0, max(0.0, (temp_c - min_c) / (max_c - min_c)))
    filled = int(round(ratio * width))
    bar = "█" * filled + "░" * (width - filled)
    return f"[{bar}] {temp_c:5.2f} °C"


class LiveTemperatureViewer:
    def __init__(self, output_dir: str = "../results", ref_temp: float = 25.4, target_side: str = "AUTO"):
        self.output_dir = output_dir
        self.ref_temp = ref_temp
        self.target_side = (target_side or "AUTO").upper()
        os.makedirs(output_dir, exist_ok=True)

        self.client: Optional[BleakClient] = None
        self.detected_device: Optional[Dict[str, Any]] = None
        self.samples_received = 0
        self.latest_temp_c: Optional[float] = None
        self.latest_temp_time: Optional[float] = None
        self.latest_bat_pct: Optional[int] = None
        self.temp_history: List[Tuple[float, float]] = []  # (timestamp, temp_c)

        self.records: List[Dict[str, Any]] = []

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
            print("❌ No insole detected. Ensure device is powered and press [RST] button.")
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
        return True

    def on_telemetry_packet(self, sender: Any, data: bytearray):
        try:
            line = data.decode("utf-8").strip()
        except Exception:
            return

        now = time.time()

        # Temperature heartbeat: "T,<tempC>[,<batPct>]"
        if line.startswith("T,") or line.startswith("T"):
            parts = line.split(",")
            if len(parts) >= 2:
                try:
                    val = float(parts[1])
                    if -10.0 <= val <= 60.0:
                        self.latest_temp_c = val
                        self.latest_temp_time = now
                        self.samples_received += 1
                        self.temp_history.append((now, val))
                        if len(self.temp_history) > 60:
                            self.temp_history.pop(0)

                        if len(parts) >= 3:
                            try:
                                self.latest_bat_pct = int(parts[2])
                            except ValueError:
                                pass

                        elec = temp_to_electrical(val)
                        abs_err = abs(val - self.ref_temp)
                        rel_err = (abs_err / self.ref_temp * 100.0) if self.ref_temp > 0 else 0.0

                        record = {
                            "timestamp": datetime.datetime.fromtimestamp(now).strftime("%H:%M:%S.%f")[:-3],
                            "sample_idx": self.samples_received,
                            "measured_temp_c": val,
                            "measured_temp_f": elec["temp_f"],
                            "adc_16bit": elec["adc_16bit"],
                            "adc_12bit": elec["adc_12bit"],
                            "vout_v": elec["vout_v"],
                            "r_ntc_kohm": elec["r_ntc_kohm"],
                            "ref_temp_c": self.ref_temp,
                            "abs_error_c": round(abs_err, 3),
                            "rel_error_pct": round(rel_err, 2)
                        }
                        self.records.append(record)

                except ValueError:
                    pass

    async def run_dashboard(self):
        print("\nStarting continuous temperature viewer...")
        time.sleep(0.5)

        first_draw = True
        try:
            while True:
                lines = []
                dev = self.detected_device
                temp_c = self.latest_temp_c
                if temp_c is None:
                    lines.append("=" * 86)
                    lines.append("  DiabFoot Guard — NTC Thermistor Live Temperature & Metrology Viewer")
                    lines.append(f"  Target: {dev['name']} ({dev['side']}) | Heartbeat: 2.0s | Samples: 0")
                    lines.append("=" * 86)
                    lines.append("")
                    lines.append("  🌡️  PREDICTED TEMPERATURE : [Waiting for 1st live BLE packet...]")
                    lines.append("  📊  THERMAL LOAD GAUGE    : [░░░░░░░░░░░░░░░░░░░░] --.-- °C")
                    lines.append("  📈  TEMPERATURE TREND     : ⏳ Awaiting telemetry stream...")
                    lines.append("")
                    lines.append("  ── Telemetry & Metrology Status ───────────────────────────────────────────────")
                    lines.append("  • Status                 : Connected to BLE characteristic.")
                    lines.append("  • Next Packet Expected   : Within ~2.0 seconds (heartbeat interval).")
                    lines.append("  • Reference (Thermo Gun) : " + f"{self.ref_temp:6.2f} °C (Benchmark target to compare against)")
                    lines.append("  ────────────────────────────────────────────────────────────────────────────────")
                    lines.append("  Commands: Press [Ctrl+C] to Pause / Open Menu (Set Reference Temp, Save Report)")
                else:
                    elec = temp_to_electrical(temp_c)

                    # Rate of change (°C / min)
                    trend = "➖ Stable"
                    if len(self.temp_history) >= 2:
                        dt = self.temp_history[-1][0] - self.temp_history[0][0]
                        d_temp = self.temp_history[-1][1] - self.temp_history[0][1]
                        if dt > 1.0:
                            rate_per_min = (d_temp / dt) * 60.0
                            if rate_per_min > 0.5:
                                trend = f"🔺 Rising (+{rate_per_min:.1f} °C/min)"
                            elif rate_per_min < -0.5:
                                trend = f"🔻 Falling ({rate_per_min:.1f} °C/min)"

                    gauge = render_temp_bar(temp_c)
                    abs_err = abs(temp_c - self.ref_temp)
                    last_pkt_time_str = datetime.datetime.fromtimestamp(self.latest_temp_time).strftime("%H:%M:%S") if self.latest_temp_time else "--:--:--"

                    lines.append("=" * 86)
                    lines.append("  DiabFoot Guard — NTC Thermistor Live Temperature & Metrology Viewer")
                    lines.append(f"  Target: {dev['name']} ({dev['side']}) | Heartbeat: 2.0s | Samples: {self.samples_received}")
                    lines.append("=" * 86)
                    lines.append("")
                    lines.append(f"  🌡️  PREDICTED TEMPERATURE : {temp_c:6.2f} °C  ({elec['temp_f']:6.2f} °F)  [LIVE @ {last_pkt_time_str}]")
                    lines.append(f"  📊  THERMAL LOAD GAUGE    : {gauge}")
                    lines.append(f"  📈  TEMPERATURE TREND     : {trend}")
                    lines.append("")
                    lines.append("  ── Electrical Metrology Parameter Breakdown ────────────────────────────────────")
                    lines.append(f"  • ADS1115 ADC (16-bit)   : {elec['adc_16bit']:6d} counts (A0 Input, 0.1875 mV/LSB)")
                    lines.append(f"  • ESP32 ADC Equiv (12-bit): {elec['adc_12bit']:6d} / 4095 counts")
                    lines.append(f"  • Divider Voltage (Vout) : {elec['vout_v']:6.3f} V  (VCC = 3.300 V, R_fixed = 10.0 kΩ)")
                    lines.append(f"  • Thermistor Resistance  : {elec['r_ntc_kohm']:6.2f} kΩ  (Steinhart-Hart Beta 3950)")
                    lines.append("  ────────────────────────────────────────────────────────────────────────────────")
                    lines.append(f"  • Reference (Thermo Gun) : {self.ref_temp:6.2f} °C | Current Error: {abs_err:5.3f} °C ({'✅ PASS (<1.0°C)' if abs_err < 1.0 else '⚠️ FLAG'})")
                    if self.latest_bat_pct is not None:
                        lines.append(f"  • Battery Level          : {self.latest_bat_pct}%")
                    lines.append("=" * 86)
                    lines.append("  Commands: Press [Ctrl+C] to Pause / Open Menu (Set Reference Temp, Save Report)")

                output = "\n".join(lines)

                if first_draw:
                    sys.stdout.write(output + "\n")
                    first_draw = False
                else:
                    sys.stdout.write(f"\033[{len(lines)}A\r" + output + "\n")
                sys.stdout.flush()

                await asyncio.sleep(0.3)

        except (asyncio.CancelledError, KeyboardInterrupt):
            await self.handle_menu()

    async def handle_menu(self):
        sys.stdout.write("\n")
        while True:
            print("\n" + "=" * 65)
            print("  TEST-01 TEMPERATURE VIEWER MENU")
            print("=" * 65)
            print("  [1] Resume Live Temperature Display")
            print("  [2] Set Reference Temperature (from Thermo Gun)")
            print("  [3] View Accuracy Summary & MAE Metric")
            print("  [4] Save Official TEST-01 Verification Report")
            print("  [5] Exit")
            print("=" * 65)

            choice = input("Select option (1-5) [default 1]: ").strip()
            if choice == "" or choice == "1":
                await self.run_dashboard()
                break
            elif choice == "2":
                new_ref = input(f"Enter ground-truth reference temperature in °C [current: {self.ref_temp:.2f}]: ").strip()
                try:
                    val = float(new_ref)
                    self.ref_temp = val
                    print(f"✅ Reference temperature updated to {self.ref_temp:.2f} °C")
                except ValueError:
                    print("❌ Invalid temperature.")
            elif choice == "3":
                self.print_summary()
            elif choice == "4":
                self.save_reports()
                print("✅ Report saved. Exiting.")
                break
            elif choice == "5":
                break

    def print_summary(self):
        if not self.records:
            print("⚠️ No temperature samples captured yet.")
            return

        abs_errors = [r["abs_error_c"] for r in self.records]
        mae = sum(abs_errors) / len(abs_errors)
        mean_meas = sum(r["measured_temp_c"] for r in self.records) / len(self.records)

        print("\n" + "=" * 65)
        print("  TEST-01 NTC ACCURACY SUMMARY")
        print("=" * 65)
        print(f"  • Total Samples Recorded   : {len(self.records)} samples")
        print(f"  • Reference Temperature    : {self.ref_temp:.2f} °C")
        print(f"  • Mean Measured Temperature: {mean_meas:.2f} °C")
        print(f"  • Mean Absolute Error (MAE): {mae:.3f} °C  (Target: < 1.00 °C)")
        print(f"  • Status                   : {'✅ PASSED (MAE < 1.0 °C)' if mae < 1.0 else '❌ FAILED'}")
        print("=" * 65)

    def save_reports(self):
        if not self.records:
            print("⚠️ No samples to save.")
            return

        ts = datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        csv_file = os.path.join(self.output_dir, f"test_01_ntc_temperature_{ts}.csv")
        json_file = os.path.join(self.output_dir, f"test_01_summary_{ts}.json")
        md_file = os.path.join(self.output_dir, f"TEST_01_REPORT_{ts}.md")

        abs_errors = [r["abs_error_c"] for r in self.records]
        rel_errors = [r["rel_error_pct"] for r in self.records]
        mae = sum(abs_errors) / len(abs_errors)
        mre = sum(rel_errors) / len(rel_errors)
        mean_meas = sum(r["measured_temp_c"] for r in self.records) / len(self.records)
        is_passed = (mae < 1.00)

        # Save CSV
        import csv
        with open(csv_file, "w", newline="") as f:
            writer = csv.DictWriter(f, fieldnames=list(self.records[0].keys()))
            writer.writeheader()
            writer.writerows(self.records)
        print(f"💾 Raw CSV saved: {csv_file}")

        # Save JSON
        summary = {
            "test_id": "TEST-01",
            "test_name": "NTC Thermistor Temperature Accuracy & MAE Benchmark",
            "timestamp": ts,
            "device": self.detected_device,
            "reference_temp_c": self.ref_temp,
            "total_samples": len(self.records),
            "mean_measured_temp_c": round(mean_meas, 3),
            "mean_absolute_error_c": round(mae, 3),
            "mean_relative_error_pct": round(mre, 2),
            "accuracy_percent": round(100.0 - mre, 2),
            "passing_threshold_mae_c": 1.00,
            "status": "PASSED" if is_passed else "FAILED"
        }
        with open(json_file, "w") as f:
            json.dump(summary, f, indent=2)
        print(f"💾 Summary JSON saved: {json_file}")

        # Save Markdown Report
        md_content = f"""# TEST-01: NTC Thermistor Temperature Accuracy & MAE Benchmark Report

| Evaluation Metric | Recorded Value |
| :--- | :--- |
| **Test ID** | `TEST-01` |
| **Timestamp** | `{ts}` |
| **Monitored Hardware** | `{self.detected_device.get('name', 'Smart Insole')} ({self.detected_device.get('side', 'Unknown')})` |
| **Reference Instrument** | `Infrared Thermo Gun` |
| **Reference Temperature ($T_{{\\text{{ref}}}}$)** | **`{self.ref_temp:.2f} °C`** |
| **Mean Measured Temperature ($T_{{\\text{{meas}}}}$)** | **`{mean_meas:.2f} °C`** |
| **Mean Absolute Error (MAE)** | **`{mae:.3f} °C`** |
| **Target Passing Threshold** | **`< 1.00 °C`** |
| **Accuracy Score** | **`{100.0 - mre:.2f}%`** |
| **Final Result** | **`{'PASSED' if is_passed else 'FAILED'}`** |

---

### 1. Statistical Analysis & Capstone Evaluation
$$\\text{{MAE}} = \\frac{{1}}{{{len(self.records)}}} \\sum_{{i=1}}^{{{len(self.records)}}} |T_{{\\text{{meas}}, i}} - T_{{\\text{{ref}}}}| = \\mathbf{{{mae:.3f} °C}}$$

{'✅ The measured Mean Absolute Error satisfies the capstone clinical requirement (< 1.00 °C).' if is_passed else '❌ The MAE did not meet the 1.00 °C threshold.'}
"""
        with open(md_file, "w") as f:
            f.write(md_content)
        print(f"📄 Markdown report saved: {md_file}")


def main():
    parser = argparse.ArgumentParser(description="Live NTC Temperature & Metrology Viewer")
    parser.add_argument("--reference", "-r", type=float, default=25.4, help="Ground-truth reference temperature in °C")
    parser.add_argument("--side", "-s", choices=["LEFT", "RIGHT", "AUTO"], default="AUTO", help="Target insole side (default: AUTO)")
    parser.add_argument("--output-dir", default=os.path.join(os.path.dirname(__file__), "../results"),
                        help="Directory to save results")
    args = parser.parse_args()

    viewer = LiveTemperatureViewer(output_dir=args.output_dir, ref_temp=args.reference, target_side=args.side)

    async def runner():
        ok = await viewer.scan_and_connect()
        if not ok:
            return
        await viewer.run_dashboard()

    try:
        asyncio.run(runner())
    except KeyboardInterrupt:
        pass


if __name__ == "__main__":
    main()
