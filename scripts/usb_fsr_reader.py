#!/usr/bin/env python3
"""
DiabFoot Guard — High-Rate USB Serial FSR Sensitivity Reader
Reads real-time ADC and Voltage from single ESP32-C3 over USB.

Features:
- Configurable Reference Resistor (R_ref: 10k, 22k, 47k, 100k, or custom ohms).
- Real-time in-place multimeter display (ADC, Vout, R_fsr, Force N, Mass kg, Bar Gauge).
- Automatic port detection (/dev/cu.usbmodem* / /dev/cu.usbserial*).
- Live session peak tracking (Peak ADC, Min R_fsr).
- Auto-saves high-resolution CSV log.
"""

import argparse
import datetime
import glob
import math
import os
import sys
import time
from typing import Optional

try:
    import serial
    import serial.tools.list_ports
except ImportError:
    print("[-] pyserial not found. Run using: uv run --with pyserial python3 usb_fsr_reader.py")
    sys.exit(1)

VCC = 3.3
ADC_MAX = 4095.0

PRESETS = {
    "10k": 10000.0,
    "22k": 22000.0,
    "47k": 47000.0,
    "100k": 100000.0,
}


def find_esp32_port() -> Optional[str]:
    """Auto-detect ESP32-C3 USB CDC / serial port on macOS/Linux."""
    # Preferred macOS pattern for ESP32-C3 built-in USB JTAG/serial
    patterns = ["/dev/cu.usbmodem*", "/dev/cu.usbserial*", "/dev/ttyACM*", "/dev/ttyUSB*"]
    for pat in patterns:
        matches = glob.glob(pat)
        if matches:
            return sorted(matches)[0]

    # Fallback to serial.tools.list_ports
    ports = serial.tools.list_ports.comports()
    for p in ports:
        desc = (p.description or "").lower()
        if "esp" in desc or "usb" in desc or "jtag" in desc or "ch340" in desc or "cp210" in desc:
            return p.device
        if p.device.startswith("/dev/cu.usb"):
            return p.device

    return None


def calculate_virtual_resistors(r_fsr_ohm: float):
    """
    Simulate what the ADC and Vout would be for 22k, 47k, and 100k
    given the actual physical R_fsr measured from the physical circuit.
    """
    results = {}
    virtual_refs = [("10k (Real)", 10000.0), ("22k (Virt)", 22000.0), ("47k (Virt)", 47000.0), ("100k (Virt)", 100000.0)]
    for label, r_ref_val in virtual_refs:
        if math.isinf(r_fsr_ohm) or r_fsr_ohm > 50_000_000.0:
            results[label] = {"adc": 0, "vout": 0.0, "pct": 0.0}
        else:
            vout = VCC * (r_ref_val / (r_fsr_ohm + r_ref_val))
            adc = int(round((vout / VCC) * ADC_MAX))
            adc = max(0, min(int(ADC_MAX), adc))
            results[label] = {
                "adc": adc,
                "vout": round(vout, 3),
                "pct": round((adc / ADC_MAX) * 100.0, 1)
            }
    return results


def calculate_electrical(adc_val: int, r_ref: float):
    """
    Compute Vout, R_fsr, Conductance, and Estimated Force from raw ADC and R_ref.
      Circuit: 3.3V ─── [FSR] ───┬─── [R_ref] ─── GND
                                 │
                                [ADC]
      Vout = VCC * R_ref / (R_fsr + R_ref)
      R_fsr = R_ref * (VCC / Vout - 1)
    """
    adc = max(0, min(int(ADC_MAX), adc_val))
    vout = (adc / ADC_MAX) * VCC

    if vout <= 0.005:
        return {
            "adc": adc,
            "vout": 0.000,
            "r_fsr_ohm": float("inf"),
            "r_fsr_display": "> 10 MΩ (Idle)",
            "conductance_us": 0.0,
            "force_n": 0.0,
            "mass_kg": 0.0
        }

    # Inverse divider equation
    r_fsr = r_ref * ((VCC / vout) - 1.0)
    r_fsr = max(0.1, r_fsr)

    conductance_us = 1_000_000.0 / r_fsr

    # Standard commercial FSR response approximation:
    # Under 1000 uS (light touch): Force (N) ≈ G / 80
    if conductance_us <= 1000.0:
        force_n = conductance_us / 80.0
    else:
        force_n = (conductance_us - 1000.0) / 30.0 + 12.5

    force_n = max(0.0, force_n)
    mass_kg = force_n / 9.80665

    if r_fsr >= 1_000_000:
        r_str = f"{r_fsr / 1_000_000.0:.2f} MΩ"
    elif r_fsr >= 1_000:
        r_str = f"{r_fsr / 1_000.0:.2f} kΩ"
    else:
        r_str = f"{r_fsr:.1f} Ω"

    return {
        "adc": adc,
        "vout": round(vout, 3),
        "r_fsr_ohm": r_fsr,
        "r_fsr_display": r_str,
        "conductance_us": round(conductance_us, 1),
        "force_n": round(force_n, 3),
        "mass_kg": round(mass_kg, 3)
    }


def main():
    parser = argparse.ArgumentParser(description="USB Serial FSR Sensitivity Bench Reader")
    parser.add_argument("--port", default=None, help="Serial port (auto-detected if omitted)")
    parser.add_argument("--baud", type=int, default=115200, help="Baud rate (default: 115200)")
    parser.add_argument("--r-ref", type=float, default=10000.0,
                        help="Value of fixed pull-down resistor R_ref in Ohms (e.g. 10000, 22000, 47000, 100000)")
    parser.add_argument("--preset", choices=list(PRESETS.keys()), default=None,
                        help="Preset resistor value: 10k, 22k, 47k, or 100k")
    parser.add_argument("--compare", action="store_true",
                        help="Show real-time virtual simulation for 22k, 47k, and 100k resistors side-by-side based on physical 10k")
    parser.add_argument("--scroll", action="store_true", help="Print scroll lines instead of in-place screen")
    parser.add_argument("--output-dir", default="../results", help="Directory for CSV logs")
    args = parser.parse_args()

    r_ref = PRESETS[args.preset] if args.preset else args.r_ref
    os.makedirs(args.output_dir, exist_ok=True)

    timestamp_str = datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    csv_path = os.path.join(args.output_dir, f"fsr_usb_bench_{int(r_ref)}ohm_{timestamp_str}.csv")

    port = args.port or find_esp32_port()
    if not port:
        print("[-] ERROR: No ESP32 serial port detected.")
        print("    Plug the ESP32 USB cable into your Mac and re-run, or pass --port /dev/cu.xxx")
        sys.exit(1)

    print("=" * 85)
    print("  DiabFoot Guard — USB Serial FSR Sensitivity Bench Reader")
    print(f"  Target Port:         {port} @ {args.baud} baud")
    print(f"  Reference Resistor:  {r_ref:,.0f} Ω ({r_ref/1000:.1f} kΩ)")
    print(f"  Live CSV Log:        {csv_path}")
    print("=" * 85)

    try:
        ser = serial.Serial(port, args.baud, timeout=1.0)
    except Exception as e:
        print(f"[-] ERROR: Failed to open serial port {port}: {e}")
        print("    If another monitor/process is open, close it first.")
        sys.exit(1)

    time.sleep(1.0) # Allow port reset settle
    ser.reset_input_buffer()

    csv_file = open(csv_path, "w")
    csv_file.write("sample_id,timestamp_ms,raw_adc,vout_v,r_ref_ohm,r_fsr_ohm,conductance_us,force_n,mass_kg\n")

    print("[+] Serial connected! Streaming live telemetry (Press [Ctrl+C] to stop)...")
    if args.scroll:
        print(f"{'Time':12} | {'ADC (0-4095)':12} | {'Vout (V)':10} | {'R_FSR':16} | {'Force (N)':10} | {'Mass (kg)':10} | Gauge")
        print("-" * 85)

    sample_id = 0
    peak_adc = 0
    peak_vout = 0.0
    min_r_fsr = float("inf")
    peak_force = 0.0
    last_scroll_time = 0.0

    try:
        while True:
            line = ser.readline().decode("utf-8", errors="replace").strip()
            if not line:
                continue

            # Parse line: DATA,<time_ms>,<adc>,<vout_mv>
            if line.startswith("DATA,"):
                parts = line.split(",")
                if len(parts) >= 4:
                    try:
                        time_ms = int(parts[1])
                        raw_adc = int(parts[2])
                        vout_mv = int(parts[3])

                        elec = calculate_electrical(raw_adc, r_ref)
                        sample_id += 1

                        adc = elec["adc"]
                        vout = elec["vout"]
                        r_ohm = elec["r_fsr_ohm"]
                        fn = elec["force_n"]
                        mass = elec["mass_kg"]
                        r_str = elec["r_fsr_display"]

                        if adc > peak_adc: peak_adc = adc
                        if vout > peak_vout: peak_vout = vout
                        if r_ohm < min_r_fsr and r_ohm > 0: min_r_fsr = r_ohm
                        if fn > peak_force: peak_force = fn

                        # Write CSV
                        r_val = "" if math.isinf(r_ohm) else f"{r_ohm:.1f}"
                        csv_file.write(f"{sample_id},{time_ms},{adc},{vout:.3f},{r_ref:.0f},{r_val},{elec['conductance_us']},{fn:.3f},{mass:.3f}\n")
                        if sample_id % 20 == 0:
                            csv_file.flush()

                        # Visual bar gauge
                        gauge_len = int((adc / 4095.0) * 20)
                        gauge = "#" * gauge_len + "-" * (20 - gauge_len)

                        if args.compare:
                            v = calculate_virtual_resistors(r_ohm)
                            r10 = v["10k (Real)"]["adc"]
                            r22 = v["22k (Virt)"]["adc"]
                            r47 = v["47k (Virt)"]["adc"]
                            r100 = v["100k (Virt)"]["adc"]
                            if args.scroll:
                                now = time.time()
                                if now - last_scroll_time >= 0.10:
                                    now_str = datetime.datetime.now().strftime("%H:%M:%S.%f")[:-3]
                                    print(f"{now_str} | Real 10k: {r10:4d} | Virt 22k: {r22:4d} | Virt 47k: {r47:4d} | Virt 100k: {r100:4d} | R_FSR: {r_str:>12}")
                                    last_scroll_time = now
                            else:
                                sys.stdout.write(
                                    f"\r[COMPARE] Real 10k:{r10:4d} ({v['10k (Real)']['pct']:4.1f}%) | "
                                    f"Virt 22k:{r22:4d} ({v['22k (Virt)']['pct']:4.1f}%) | "
                                    f"Virt 47k:{r47:4d} ({v['47k (Virt)']['pct']:4.1f}%) | "
                                    f"Virt 100k:{r100:4d} ({v['100k (Virt)']['pct']:4.1f}%) | "
                                    f"R_fsr:{r_str:>12}  "
                                )
                                sys.stdout.flush()
                        elif args.scroll:
                            now = time.time()
                            if now - last_scroll_time >= 0.10:
                                now_str = datetime.datetime.now().strftime("%H:%M:%S.%f")[:-3]
                                print(f"{now_str:12} | {adc:5d} / 4095  | {vout:6.3f} V   | {r_str:16} | {fn:6.2f} N    | {mass:6.3f} kg  | [{gauge}]")
                                last_scroll_time = now
                        else:
                            # In-place multimeter display
                            sys.stdout.write(
                                f"\r[FSR BENCH R={r_ref/1000:.0f}k] ADC:{adc:4d}/4095 ({adc/4095*100:4.1f}%) | "
                                f"Vout:{vout:5.3f}V | R_fsr:{r_str:>14} | F:{fn:5.2f}N ({mass:5.3f}kg) | [{gauge}]  "
                            )
                            sys.stdout.flush()

                    except ValueError:
                        pass

    except KeyboardInterrupt:
        pass
    finally:
        ser.close()
        csv_file.close()

    print("\n" + "=" * 85)
    print("  FSR SENSITIVITY BENCH SUMMARY")
    print("=" * 85)
    print(f"  Total Samples Logged:  {sample_id}")
    print(f"  Reference Resistor:    {r_ref:,.0f} Ω ({r_ref/1000:.1f} kΩ)")
    print(f"  Peak ADC Count:        {peak_adc} / 4095 ({peak_adc/4095*100:.1f}%)")
    print(f"  Peak Output Voltage:   {peak_vout:.3f} V")
    min_r_str = f"{min_r_fsr/1000.0:.2f} kΩ" if min_r_fsr < float('inf') else "N/A"
    print(f"  Minimum FSR Resistance:{min_r_str} (Peak Load)")
    print(f"  Peak Force Estimated:  {peak_force:.2f} N ({peak_force/9.80665:.3f} kg)")
    if min_r_fsr < float('inf'):
        print("-" * 85)
        print("  VIRTUAL RESISTOR PROJECTION (At Your Peak Measured Load):")
        v_peak = calculate_virtual_resistors(min_r_fsr)
        for label, val in v_peak.items():
            print(f"    • {label:14}: ADC {val['adc']:4d} / 4095 ({val['pct']:4.1f}%) | Vout: {val['vout']:.3f} V")
    print(f"  Log File:              {csv_path}")
    print("=" * 85)


if __name__ == "__main__":
    main()
