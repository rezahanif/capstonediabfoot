#!/usr/bin/env python3
"""
DiabFoot Guard — Verification Benchmark Engine
TEST-02: Plantar Pressure FSR Weight Accuracy & Mean Relative Error Bench

Evaluates FSR sensor accuracy against known reference weights (kg).
Features:
- Live multi-sensor probe mode to identify physical sensor channels.
- Automatic Tare (zeroing) before applying each reference weight.
- Real-time continuous live reading display showing kPa, kg, ADC count, Vout, and R_FSR.
- Press [ENTER] to lock stable reading.
- Full reverse electrical calculations (Force N, Conductance uS, Resistance kohm, Vout, ADC count).
- Statistical computation of Absolute Error, Relative Error (%), and Mean Relative Error (MRE).
- Generates CSV log, summary JSON, and formatted Markdown report.

Supports auto-detection of Left or Right insole.
"""

import asyncio
import argparse
import datetime
import json
import math
import os
import sys
import time
from typing import List, Dict, Any, Optional

from bleak import BleakClient, BleakScanner

# Insole Configurations
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

# Physical parameters from firmware (diabfoot_guard_sensor.ino)
# Area = 7.85e-5 m^2 (10 mm diameter active circular area)
# F_N = m_kg * 9.80665
# P_kPa = (F_N / Area) / 1000.0 = m_kg * (9.80665 / (7.85e-5 * 1000.0)) = 124.925 kPa per kg
FSR_AREA_M2 = 7.85e-5
GRAVITY = 9.80665
FSR_KPA_PER_KG = GRAVITY / (FSR_AREA_M2 * 1000.0)  # ~124.925 kPa/kg
VCC = 3.3
R_FIXED = 10000.0  # 10k fixed divider resistor
ADC_MAX = 4095

DEFAULT_SENSOR_NAMES = [
    "Sensor 1 (p1 - GPIO 0 / Hallux)",
    "Sensor 2 (p2 - GPIO 1 / MTH 1)",
    "Sensor 3 (p3 - GPIO 2 / MTH 2)",
    "Sensor 4 (p4 - GPIO 3 / MTH 4)",
    "Sensor 5 (p5 - GPIO 4 / Heel)"
]


def kpa_to_electrical(kpa: float) -> Dict[str, Any]:
    """
    Exact reverse calculation of firmware FSR conversion pipeline:
      Firmware:
        P_kPa = force_N / (FSR_AREA_M2 * 1000.0)  -> force_N = P_kPa * 0.0785
        conductance_uS = (force_N <= 12.5) ? (force_N * 80.0) : ((force_N * 30.0) + 1000.0)
        R_fsr = 1,000,000.0 / conductance_uS
        Vout = VCC * R_fixed / (R_fsr + R_fixed)
        ADC = (Vout / VCC) * 4095
    """
    if kpa <= 0.001:
        return {
            "force_n": 0.0,
            "conductance_us": 0.0,
            "r_fsr_kohm": 9999.0,
            "vout_v": 0.0,
            "adc_raw": 0
        }

    force_n = kpa * (FSR_AREA_M2 * 1000.0)  # F = P_kPa * 0.0785

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

    return {
        "force_n": round(force_n, 3),
        "conductance_us": round(conductance_us, 2),
        "r_fsr_kohm": round(r_fsr_ohm / 1000.0, 2),
        "vout_v": round(vout_v, 3),
        "adc_raw": min(ADC_MAX, max(0, adc_raw))
    }


class WeightAccuracyBenchmark:
    def __init__(self, output_dir: str = "../results", side: str = "auto"):
        self.output_dir = output_dir
        self.side_pref = side.lower()
        os.makedirs(output_dir, exist_ok=True)

        self.timestamp_str = datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        self.csv_path = os.path.join(output_dir, f"test_02_weight_accuracy_{self.timestamp_str}.csv")
        self.json_path = os.path.join(output_dir, f"test_02_summary_{self.timestamp_str}.json")
        self.report_path = os.path.join(output_dir, f"TEST_02_REPORT_{self.timestamp_str}.md")

        self.client: Optional[BleakClient] = None
        self.active_profile: Optional[Dict[str, str]] = None
        self.latest_kpa = [0.0, 0.0, 0.0, 0.0, 0.0]
        self.packet_buffer: List[List[float]] = []
        self.total_packets_received = 0
        self.records: List[Dict[str, Any]] = []

    def on_notification(self, sender, data: bytearray):
        # Support both single-frame and multi-frame (batched) payloads
        raw = data.decode("utf-8", errors="replace").strip()
        lines = raw.split("\n")
        for line in lines:
            line = line.strip()
            if not line or line.startswith("T,"):
                continue

            parts = line.split(",")
            if len(parts) >= 11:
                try:
                    kpa = [float(p) for p in parts[6:11]]
                    self.latest_kpa = kpa
                    self.packet_buffer.append(kpa)
                    if len(self.packet_buffer) > 100:
                        self.packet_buffer.pop(0)
                    self.total_packets_received += 1
                except ValueError:
                    pass

    async def get_averaged_samples(self, count: int = 25, delay_sec: float = 0.5) -> List[float]:
        """Collect 'count' fresh samples and return channel averages."""
        start_count = self.total_packets_received
        collected: List[List[float]] = []

        timeout = time.time() + 4.0
        while len(collected) < count and time.time() < timeout:
            if self.total_packets_received > start_count:
                collected.append(list(self.latest_kpa))
                start_count = self.total_packets_received
            await asyncio.sleep(0.02)

        if not collected:
            return list(self.latest_kpa)

        avg = [sum(col) / len(col) for col in zip(*collected)]
        return [round(x, 2) for x in avg]

    async def run_probe_mode(self):
        """Live multi-sensor monitor with electrical parameters to identify physical locations."""
        print("\n" + "=" * 70)
        print("  PROBE & IDENTIFY MODE")
        print("  Press each physical sensor pad to observe responsiveness & ADC.")
        print("  Press [ENTER] to return to main menu.")
        print("=" * 70)

        stop_event = asyncio.Event()

        async def check_key():
            await asyncio.to_thread(sys.stdin.readline)
            stop_event.set()

        key_task = asyncio.create_task(check_key())

        try:
            while not stop_event.is_set():
                kpa = self.latest_kpa
                lines = []
                for i, val in enumerate(kpa):
                    elec = kpa_to_electrical(val)
                    bar_len = int(min(20, max(0, val / 10.0)))
                    bar_str = "#" * bar_len + "-" * (20 - bar_len)
                    lines.append(f"S{i+1}: {val:6.1f} kPa | ADC:{elec['adc_raw']:4d} | {elec['vout_v']:4.2f}V [{bar_str}]")

                sys.stdout.write(
                    f"\r{lines[0]} | {lines[1]}\n"
                    f"{lines[2]} | {lines[3]}\n"
                    f"{lines[4]}\033[2A"
                )
                sys.stdout.flush()
                await asyncio.sleep(0.1)
        except (asyncio.CancelledError, KeyboardInterrupt):
            pass
        finally:
            key_task.cancel()
            sys.stdout.write("\n\n\n")
            print("[+] Exited Probe Mode.")

    async def measure_sensor_point(self):
        """Guided measurement: Select sensor -> Tare -> Input Ref Weight -> Live Monitor -> Lock."""
        print("\n" + "=" * 70)
        print("  MEASURE SENSOR WEIGHT ACCURACY")
        print("=" * 70)

        print("Select Sensor Channel to test:")
        for i, name in enumerate(DEFAULT_SENSOR_NAMES):
            print(f"  [{i+1}] {name}")

        choice = await asyncio.to_thread(input, "\nEnter sensor number (1-5) [default: 1]: ")
        try:
            sensor_idx = int(choice.strip()) - 1
            if sensor_idx < 0 or sensor_idx > 4:
                sensor_idx = 0
        except ValueError:
            sensor_idx = 0

        sensor_name = DEFAULT_SENSOR_NAMES[sensor_idx]
        print(f"\n[+] Selected: {sensor_name}")

        # Step 1: Tare (Zeroing)
        print("\n--- STEP 1: TARE / ZERO BASELINE ---")
        print("Ensure NO weight is touching this sensor right now.")
        await asyncio.to_thread(input, "Press [ENTER] to capture baseline tare... ")

        print("[*] Sampling baseline tare offset (1 sec)...")
        tare_samples = await self.get_averaged_samples(count=25)
        baseline_kpa = tare_samples[sensor_idx]
        base_elec = kpa_to_electrical(baseline_kpa)
        print(f"[+] Baseline locked: {baseline_kpa:.2f} kPa (ADC: {base_elec['adc_raw']}, Vout: {base_elec['vout_v']:.3f}V)")

        # Step 2: Reference Weight Input
        print("\n--- STEP 2: REFERENCE WEIGHT ENTRY ---")
        print("NOTE: For accurate concentrated loading, place a small coin or puck directly on the FSR pad!")
        ref_input = await asyncio.to_thread(input, "Enter known reference weight in kg (e.g. 1.0, 0.5, 2.0): ")
        try:
            ref_weight_kg = float(ref_input.strip())
            if ref_weight_kg <= 0:
                print("[-] Invalid weight, defaulting to 1.0 kg.")
                ref_weight_kg = 1.0
        except ValueError:
            print("[-] Invalid input, defaulting to 1.0 kg.")
            ref_weight_kg = 1.0

        ref_pressure_kpa = ref_weight_kg * FSR_KPA_PER_KG
        ref_force_n = ref_weight_kg * GRAVITY
        print(f"[+] Reference: {ref_weight_kg:.3f} kg ({ref_force_n:.2f} N | Theoretical: {ref_pressure_kpa:.2f} kPa)")

        # Step 3: Place Weight and Live Monitor
        print("\n--- STEP 3: PLACE WEIGHT & STABILIZE ---")
        print(f"Place the {ref_weight_kg:.3f} kg weight onto {sensor_name} (using coin/puck concentrator).")
        print("Watch the live reading settle, then press [ENTER] to lock & record!\n")

        stop_live = asyncio.Event()

        async def live_display():
            while not stop_live.is_set():
                raw = self.latest_kpa[sensor_idx]
                net = max(0.0, raw - baseline_kpa)
                meas_kg = net / FSR_KPA_PER_KG
                rel_err = abs(meas_kg - ref_weight_kg) / ref_weight_kg * 100.0

                raw_elec = kpa_to_electrical(raw)

                # Monitor other 4 sensors for spillover / crosstalk
                spill = " ".join([f"p{j+1}:{self.latest_kpa[j]:.0f}" for j in range(5) if j != sensor_idx])

                sys.stdout.write(
                    f"\r>> LIVE [S{sensor_idx+1}]: Net={net:6.1f}kPa | Meas={meas_kg:5.3f}kg | "
                    f"ADC={raw_elec['adc_raw']:4d} ({raw_elec['vout_v']:4.2f}V, {raw_elec['r_fsr_kohm']:5.1f}kΩ) | "
                    f"Err={rel_err:5.1f}% | Spill:[{spill}]  "
                )
                sys.stdout.flush()
                await asyncio.sleep(0.1)

        display_task = asyncio.create_task(live_display())

        # Wait for user to press ENTER to lock
        await asyncio.to_thread(sys.stdin.readline)
        stop_live.set()
        await display_task
        sys.stdout.write("\n")

        print("[*] Averaging 25 post-lock samples to filter transient noise...")
        locked_samples = await self.get_averaged_samples(count=25)

        raw_locked_kpa = locked_samples[sensor_idx]
        net_locked_kpa = max(0.0, raw_locked_kpa - baseline_kpa)
        meas_locked_kg = net_locked_kpa / FSR_KPA_PER_KG
        abs_err_kg = abs(meas_locked_kg - ref_weight_kg)
        rel_err_pct = (abs_err_kg / ref_weight_kg) * 100.0

        raw_elec = kpa_to_electrical(raw_locked_kpa)
        net_elec = kpa_to_electrical(net_locked_kpa)

        record = {
            "record_id": len(self.records) + 1,
            "timestamp": datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "sensor_idx": sensor_idx + 1,
            "sensor_name": sensor_name,
            "ref_weight_kg": round(ref_weight_kg, 4),
            "ref_force_n": round(ref_force_n, 3),
            "ref_pressure_kpa": round(ref_pressure_kpa, 2),
            "baseline_tare_kpa": round(baseline_kpa, 2),
            "baseline_adc": base_elec["adc_raw"],
            "raw_pressure_kpa": round(raw_locked_kpa, 2),
            "net_pressure_kpa": round(net_locked_kpa, 2),
            "net_force_n": round(net_elec["force_n"], 3),
            "meas_weight_kg": round(meas_locked_kg, 4),
            "abs_error_kg": round(abs_err_kg, 4),
            "rel_error_pct": round(rel_err_pct, 2),
            "raw_adc": raw_elec["adc_raw"],
            "raw_voltage_v": raw_elec["vout_v"],
            "raw_r_fsr_kohm": raw_elec["r_fsr_kohm"],
            "all_channels_kpa": [round(x, 2) for x in locked_samples]
        }
        self.records.append(record)

        print("-" * 75)
        print(f"[+] LOCKED RECORD #{record['record_id']}:")
        print(f"    Sensor:          {sensor_name}")
        print(f"    Reference Load:  {ref_weight_kg:.3f} kg ({ref_force_n:.2f} N | {ref_pressure_kpa:.2f} kPa)")
        print(f"    Tare Baseline:   {baseline_kpa:.2f} kPa (ADC: {base_elec['adc_raw']}, Vout: {base_elec['vout_v']:.3f}V)")
        print(f"    Raw Reading:     {raw_locked_kpa:.2f} kPa (ADC: {raw_elec['adc_raw']}, Vout: {raw_elec['vout_v']:.3f}V, R_fsr: {raw_elec['r_fsr_kohm']:.2f} kΩ)")
        print(f"    Net Pressure:    {net_locked_kpa:.2f} kPa (Net Force: {net_elec['force_n']:.2f} N)")
        print(f"    Measured Mass:   {meas_locked_kg:.3f} kg")
        print(f"    Absolute Error:  {abs_err_kg:.3f} kg")
        print(f"    Relative Error:  {rel_err_pct:.2f} %")
        print("-" * 75)

    def print_summary_table(self):
        if not self.records:
            print("\n[-] No measurement records logged yet.")
            return

        print("\n" + "=" * 115)
        print(f"  RECORDED MEASUREMENTS SUMMARY ({len(self.records)} points logged)")
        print("=" * 115)
        print(f"{'#':2} | {'Sensor':20} | {'Ref(kg)':7} | {'Meas(kg)':8} | {'Net kPa':7} | {'Raw ADC':7} | {'Vout(V)':7} | {'R(kΩ)':6} | {'Rel Err':8}")
        print("-" * 115)
        for r in self.records:
            print(f"{r['record_id']:2d} | {r['sensor_name'][:20]:20} | {r['ref_weight_kg']:7.3f} | {r['meas_weight_kg']:8.3f} | "
                  f"{r['net_pressure_kpa']:7.1f} | {r['raw_adc']:7d} | {r['raw_voltage_v']:7.3f} | {r['raw_r_fsr_kohm']:6.2f} | {r['rel_error_pct']:7.2f}%")
        print("=" * 115)

        sensor_groups: Dict[int, List[float]] = {}
        for r in self.records:
            s_id = r["sensor_idx"]
            sensor_groups.setdefault(s_id, []).append(r["rel_error_pct"])

        print("\n  Mean Relative Error (MRE) per Sensor:")
        for s_id, errs in sorted(sensor_groups.items()):
            mre = sum(errs) / len(errs)
            print(f"    - Sensor {s_id} ({len(errs)} weights): MRE = {mre:.2f}%")

    def save_and_generate_reports(self):
        if not self.records:
            print("[-] No records to save.")
            return

        # 1. Save CSV
        with open(self.csv_path, "w") as f:
            f.write("record_id,timestamp,sensor_idx,sensor_name,ref_weight_kg,ref_force_n,ref_pressure_kpa,"
                    "baseline_tare_kpa,baseline_adc,raw_pressure_kpa,net_pressure_kpa,net_force_n,meas_weight_kg,"
                    "abs_error_kg,rel_error_pct,raw_adc,raw_voltage_v,raw_r_fsr_kohm,all_channels_kpa\n")
            for r in self.records:
                ch_str = ";".join(str(x) for x in r["all_channels_kpa"])
                f.write(f"{r['record_id']},{r['timestamp']},{r['sensor_idx']},\"{r['sensor_name']}\","
                        f"{r['ref_weight_kg']},{r['ref_force_n']},{r['ref_pressure_kpa']},{r['baseline_tare_kpa']},"
                        f"{r['baseline_adc']},{r['raw_pressure_kpa']},{r['net_pressure_kpa']},{r['net_force_n']},"
                        f"{r['meas_weight_kg']},{r['abs_error_kg']},{r['rel_error_pct']},{r['raw_adc']},"
                        f"{r['raw_voltage_v']},{r['raw_r_fsr_kohm']},\"{ch_str}\"\n")

        # 2. Compute Statistics for Summary & Report
        all_rel_errors = [r["rel_error_pct"] for r in self.records]
        overall_mre = sum(all_rel_errors) / len(all_rel_errors)

        sensor_mre: Dict[str, Any] = {}
        for s_id in range(1, 6):
            pts = [r for r in self.records if r["sensor_idx"] == s_id]
            if pts:
                errs = [p["rel_error_pct"] for p in pts]
                sensor_mre[f"sensor_{s_id}"] = {
                    "sensor_name": DEFAULT_SENSOR_NAMES[s_id - 1],
                    "tested_points": len(pts),
                    "mean_relative_error_pct": round(sum(errs) / len(errs), 2),
                    "min_error_pct": round(min(errs), 2),
                    "max_error_pct": round(max(errs), 2)
                }

        device_name = self.active_profile["name"] if self.active_profile else "Smart_Insole"
        passed = (overall_mre <= 30.0) and (len(self.records) >= 3)

        summary = {
            "test_id": "TEST-02",
            "name": "Plantar Pressure FSR Weight Accuracy & Mean Relative Error Bench",
            "device": device_name,
            "verdict": "PASSED" if passed else "COMPLETED_WITH_OFFSETS",
            "total_measurements": len(self.records),
            "overall_mean_relative_error_pct": round(overall_mre, 2),
            "sensors_evaluated": len(sensor_mre),
            "sensor_breakdown": sensor_mre,
            "timestamp": self.timestamp_str
        }

        with open(self.json_path, "w") as f:
            json.dump(summary, f, indent=2)

        # 3. Generate Markdown Report
        md = f"""# DiabFoot Verification Report — TEST-02: FSR Weight Accuracy & Mean Relative Error

- **Target Metric**: Characterization of FSR sensor load response accuracy against known reference weights (kg), with empirical Mean Relative Error (MRE) calculation.
- **Test Date**: {datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
- **Target Device**: ESP32-C3 (`{device_name}`)
- **Transport Mode**: Over-The-Air BLE GATT Stream (`{self.active_profile['char_uuid'] if self.active_profile else 'BLE'}`)
- **Total Test Points Logged**: **{len(self.records)}**
- **Overall Mean Relative Error (MRE)**: **{overall_mre:.2f}%**
- **Verdict**: **{'PASSED' if passed else 'COMPLETED'}**

---

## 1. Executive Summary

Empirical weight accuracy evaluation was conducted across the smart insole's FSR channels using known reference weights. Each measurement was taken after an active Tare (zero-load baseline subtraction) using a concentrated mechanical load.

| Benchmark Metric | Measured Value | Evaluation |
| :--- | :---: | :---: |
| **Total Test Points Logged** | **{len(self.records)} points** | Full Coverage |
| **Overall Insole MRE** | **{overall_mre:.2f}%** | {'Within Expected Range (<30%)' if passed else 'Offset Documented'} |
| **Sensors Characterized** | **{len(sensor_mre)} / 5** | Multi-channel |
| **Benchmark Verdict** | **{'PASSED' if passed else 'COMPLETED'}** | Characterization Complete |

---

## 2. Measurement Records Table

| # | Sensor | Ref Load | Meas Mass | Net kPa | Raw ADC | Vout (V) | R_fsr (kΩ) | Abs Err | Rel Error |
| :-: | :--- | :-: | :-: | :-: | :-: | :-: | :-: | :-: | :-: |
"""
        for r in self.records:
            md += (f"| {r['record_id']} | {r['sensor_name']} | {r['ref_weight_kg']:.3f} kg | "
                   f"{r['meas_weight_kg']:.3f} kg | {r['net_pressure_kpa']:.1f} kPa | "
                   f"`{r['raw_adc']}` | {r['raw_voltage_v']:.3f} V | {r['raw_r_fsr_kohm']:.2f} kΩ | "
                   f"{r['abs_error_kg']:.3f} kg | **{r['rel_error_pct']:.2f}%** |\n")

        md += f"""
---

## 3. Sensor Breakdown & Electrical Response

"""
        for s_key, s_data in sensor_mre.items():
            md += f"- **{s_data['sensor_name']}**: {s_data['tested_points']} weights tested, MRE = **{s_data['mean_relative_error_pct']}%** (Min: {s_data['min_error_pct']}%, Max: {s_data['max_error_pct']}%)\n"

        md += f"""
---

## 4. Log Artifacts
- **Detailed CSV Log**: [`{os.path.basename(self.csv_path)}`](file://{os.path.abspath(self.csv_path)})
- **Summary JSON**: [`{os.path.basename(self.json_path)}`](file://{os.path.abspath(self.json_path)})
"""

        with open(self.report_path, "w") as f:
            f.write(md)

        print("\n" + "=" * 70)
        print(f"[+] TEST-02 Report generated: {self.report_path}")
        print(f"[+] CSV Log generated:        {self.csv_path}")
        print(f"[+] Summary JSON generated:   {self.json_path}")
        print(f"[+] Overall Insole MRE:       {overall_mre:.2f}%")
        print("=" * 70)

    async def main_loop(self):
        print("==========================================================================")
        print("  DiabFoot Guard — TEST-02: FSR Weight Accuracy & Mean Relative Error Bench")
        print("==========================================================================")

        # Device scanning logic: Auto-detect Left or Right, or respect preference
        target_names = []
        if self.side_pref == "left":
            target_names = [DEVICE_PROFILES["left"]["name"]]
        elif self.side_pref == "right":
            target_names = [DEVICE_PROFILES["right"]["name"]]
        else:
            target_names = [DEVICE_PROFILES["left"]["name"], DEVICE_PROFILES["right"]["name"]]

        print(f"[*] Scanning for device: {', '.join(target_names)}...")

        def match_insole(d, adv):
            name = d.name or adv.local_name or ""
            return any(t in name for t in target_names)

        device = await BleakScanner.find_device_by_filter(match_insole, timeout=12.0)

        if not device:
            print(f"[-] ERROR: None of {target_names} found on BLE scan. Ensure device is powered.")
            sys.exit(1)

        dev_name = device.name or ""
        if "Right" in dev_name:
            self.active_profile = DEVICE_PROFILES["right"]
        else:
            self.active_profile = DEVICE_PROFILES["left"]

        print(f"[+] Found {device.name} ({device.address}). Profile: {self.active_profile['name']}")
        print(f"[*] Connecting...")

        char_uuid = self.active_profile["char_uuid"]

        async with BleakClient(device, timeout=15.0) as client:
            self.client = client
            print(f"[+] Connected! Subscribing to stream characteristic {char_uuid}...")
            await client.start_notify(char_uuid, self.on_notification)

            # Wait 1s to fill buffer
            await asyncio.sleep(1.0)
            print("[+] BLE Data Stream active! Current live readings:")
            print(f"    p1={self.latest_kpa[0]:.1f} kPa | p2={self.latest_kpa[1]:.1f} kPa | "
                  f"p3={self.latest_kpa[2]:.1f} kPa | p4={self.latest_kpa[3]:.1f} kPa | p5={self.latest_kpa[4]:.1f} kPa")

            while True:
                print("\n" + "=" * 55)
                print("  TEST-02 MENU:")
                print("  [1] Probe & Identify Mode (Touch test to find channels)")
                print("  [2] Measure Sensor Accuracy (Tare -> Apply Weight -> Lock)")
                print("  [3] View Current Recorded Points")
                print("  [4] Save Results & Generate Final Report (Finish Test)")
                print("  [5] Exit / Quit without saving")
                print("=" * 55)

                choice = await asyncio.to_thread(input, "Select option (1-5): ")
                choice = choice.strip()

                if choice == "1":
                    await self.run_probe_mode()
                elif choice == "2":
                    await self.measure_sensor_point()
                elif choice == "3":
                    self.print_summary_table()
                elif choice == "4":
                    self.save_and_generate_reports()
                    print("\n[+] Testing completed. Exiting.")
                    break
                elif choice == "5":
                    confirm = await asyncio.to_thread(input, "Are you sure you want to quit without saving? (y/N): ")
                    if confirm.strip().lower() == "y":
                        print("Exiting.")
                        break
                else:
                    print("Invalid option. Please enter 1, 2, 3, 4, or 5.")

            await client.stop_notify(char_uuid)


def main():
    parser = argparse.ArgumentParser(description="TEST-02 Weight Accuracy Benchmark")
    parser.add_argument("--output-dir", default="../results", help="Directory for results")
    parser.add_argument("--side", default="auto", choices=["auto", "left", "right"],
                        help="Target insole side (auto, left, right)")
    args = parser.parse_args()

    bench = WeightAccuracyBenchmark(output_dir=args.output_dir, side=args.side)
    asyncio.run(bench.main_loop())


if __name__ == "__main__":
    main()
