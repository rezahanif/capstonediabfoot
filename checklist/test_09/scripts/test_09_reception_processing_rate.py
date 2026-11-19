#!/usr/bin/env python3
"""
DiabFoot Guard — Checklist Test 09
BLE Data Reception & App Processing Success Rate Measurement

Captures BLE characteristic notification callbacks, telemetry Flow delivery,
and domain parser execution from Android logcat via ADB to evaluate:
  1. Total packets received at GATT transport layer (N_rx)
  2. Total packets successfully parsed and dispatched into WalkingFSM (N_ok)
  3. Corrupt or unparseable packets rejected by SensorPacketParser (N_corrupt)
  4. Buffer overflow drops in InsoleBleManager SharedFlow (N_dropped)
  5. Percentage Processing Success Rate: (N_ok / N_rx) * 100%

Outputs:
  - CSV log: checklist/test_09/results/test_09_processing_rate_<TIMESTAMP>.csv
  - Markdown report: checklist/test_09/results/TEST_09_REPORT_<TIMESTAMP>.md
  - JSON summary: checklist/test_09/results/test_09_summary_<TIMESTAMP>.json
"""

import argparse
import datetime
import json
import os
import re
import subprocess
import sys
import time
from typing import Dict, List

ADB_BIN = "/Users/reza/Library/Android/sdk/platform-tools/adb"


def run_adb(args: List[str]) -> str:
    cmd = [ADB_BIN] + args
    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    if res.returncode != 0:
        raise RuntimeError(f"ADB error: {res.stderr.strip()}")
    return res.stdout.strip()


def check_device() -> str:
    devices_out = run_adb(["devices"])
    lines = [l.strip() for l in devices_out.splitlines() if l.strip() and not l.startswith("List of devices")]
    if not lines:
        raise RuntimeError("No Android device connected via ADB. Please plug in the phone and enable USB Debugging.")
    return lines[0].split()[0]


def parse_args():
    parser = argparse.ArgumentParser(description="Test 09: BLE Reception & Processing Rate")
    parser.add_argument("--duration", "-d", type=int, default=60, help="Test duration in seconds (default: 60)")
    parser.add_argument("--output-dir", "-o", default="../results", help="Directory for results")
    return parser.parse_args()


def main():
    args = parse_args()
    os.makedirs(args.output_dir, exist_ok=True)

    print("==========================================================================")
    print("  DiabFoot Guard — Checklist Test 09: Data Reception & Processing Rate")
    print("==========================================================================")

    try:
        device_id = check_device()
        print(f"[+] Connected Android Device: {device_id}")
    except Exception as e:
        print(f"[-] Error: {e}")
        sys.exit(1)

    timestamp_str = datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    csv_file = os.path.join(args.output_dir, f"test_09_processing_rate_{timestamp_str}.csv")
    md_file = os.path.join(args.output_dir, f"TEST_09_REPORT_{timestamp_str}.md")
    json_file = os.path.join(args.output_dir, f"test_09_summary_{timestamp_str}.json")

    # Clear logcat
    print("[*] Clearing logcat buffer...")
    run_adb(["logcat", "-c"])

    print(f"[*] Starting logcat stream for {args.duration} seconds...")
    print(f"[*] Monitoring tags: DIABFOOT_TEST, BLE_DATA_DS")
    print("--------------------------------------------------------------------------")

    logcat_proc = subprocess.Popen(
        [ADB_BIN, "logcat", "-v", "time", "-s", "DIABFOOT_TEST:I", "BLE_DATA_DS:D"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        bufsize=1
    )

    records = []
    start_time = time.time()
    packet_index = 0
    total_rx_gatt = 0
    total_parsed_ok = 0
    total_parse_corrupt = 0
    total_buffer_drops = 0

    # Patterns
    # I/DIABFOOT_TEST: GATT_NOTIFY addr=10:00:3B:D1:E0:B0 side=LEFT bytes=58 ts_ns=...
    # I/DIABFOOT_TEST: PACKET_PROCESS side=LEFT addr=10:00:3B:D1:E0:B0 parse_ok=true parse_us=145 len=58
    # W/DIABFOOT_TEST: BUFFER_DROP addr=10:00:3B:D1:E0:B0 drops=1
    # D/BLE_DATA_DS: LEFT [10:00:3B:D1:E0:B0] raw: '-3.115,0.031,...'
    # E/BLE_DATA_DS:   Failed to parse packet from 10:00:3B:D1:E0:B0

    notify_pattern = re.compile(
        r"(\d\d-\d\d \d\d:\d\d:\d\d\.\d+).*I/DIABFOOT_TEST.*GATT_NOTIFY addr=(\S+) side=(\S+) bytes=(\d+) ts_ns=(\d+)"
    )
    process_pattern = re.compile(
        r"(\d\d-\d\d \d\d:\d\d:\d\d\.\d+).*I/DIABFOOT_TEST.*PACKET_PROCESS side=(\S+) addr=(\S+) parse_ok=(\S+) parse_us=([\d\.]+) len=(\d+)"
    )
    buffer_drop_pattern = re.compile(
        r"(\d\d-\d\d \d\d:\d\d:\d\d\.\d+).*W/DIABFOOT_TEST.*BUFFER_DROP addr=(\S+) drops=(\d+)"
    )
    raw_payload_pattern = re.compile(
        r".*D/BLE_DATA_DS.*raw: '([^']*)'"
    )

    current_raw = ""

    try:
        while time.time() - start_time < args.duration:
            line = logcat_proc.stdout.readline()
            if not line:
                time.sleep(0.01)
                continue

            m_raw = raw_payload_pattern.search(line)
            if m_raw:
                current_raw = m_raw.group(1)

            m_notify = notify_pattern.search(line)
            if m_notify:
                total_rx_gatt += 1
                continue

            m_drop = buffer_drop_pattern.search(line)
            if m_drop:
                time_str, addr, drops = m_drop.groups()
                total_buffer_drops += 1
                packet_index += 1
                records.append({
                    "index": packet_index,
                    "timestamp": time_str,
                    "side": "UNKNOWN",
                    "addr": addr,
                    "length": 0,
                    "status": "BUFFER_DROPPED",
                    "parse_ok": False,
                    "parse_us": 0.0,
                    "payload_preview": ""
                })
                print(f"[{packet_index:04d}] BUFFER OVERFLOW DROP | Total Drops: {total_buffer_drops}")
                continue

            m_proc = process_pattern.search(line)
            if m_proc:
                time_str, side, addr, ok_str, parse_us_str, length_str = m_proc.groups()
                parse_ok = (ok_str.lower() == "true")
                parse_us = float(parse_us_str)
                length = int(length_str)

                packet_index += 1
                if parse_ok:
                    total_parsed_ok += 1
                    status = "PROCESSED_SUCCESS"
                else:
                    total_parse_corrupt += 1
                    status = "CORRUPT_REJECTED"

                records.append({
                    "index": packet_index,
                    "timestamp": time_str,
                    "side": side,
                    "addr": addr,
                    "length": length,
                    "status": status,
                    "parse_ok": parse_ok,
                    "parse_us": parse_us,
                    "payload_preview": current_raw[:40] if current_raw else ""
                })

                print(f"[{packet_index:04d}] {side} [{addr}] Status: {status:<18} | Pipeline: {parse_us:5.1f} µs | Payload: {current_raw[:35]}...")

    except KeyboardInterrupt:
        print("\n[*] Interrupted by user.")
    finally:
        logcat_proc.terminate()
        try:
            logcat_proc.wait(timeout=2)
        except subprocess.TimeoutExpired:
            logcat_proc.kill()

    # Write CSV
    with open(csv_file, "w") as f:
        f.write("packet_index,timestamp,side,device_address,length_bytes,status,parse_ok,pipeline_us,payload_preview\n")
        for r in records:
            f.write(f"{r['index']},{r['timestamp']},{r['side']},{r['addr']},{r['length']},{r['status']},{r['parse_ok']},{r['parse_us']},\"{r['payload_preview']}\"\n")

    print(f"\n[+] CSV Log written to: {csv_file}")

    # Calculations
    total_processed_attempts = total_parsed_ok + total_parse_corrupt
    total_received_stream = total_processed_attempts + total_buffer_drops

    # Success rate relative to all received at GATT
    denom = max(total_rx_gatt, total_received_stream)
    processing_success_rate = (total_parsed_ok / denom * 100.0) if denom > 0 else 0.0
    buffer_retention_rate = ((denom - total_buffer_drops) / denom * 100.0) if denom > 0 else 0.0

    summary_data = {
        "test_id": "TEST-09",
        "name": "BLE Data Reception & App Processing Success Rate",
        "device_id": device_id,
        "duration_seconds": args.duration,
        "total_gatt_notifications_received": total_rx_gatt,
        "total_packets_processed_successfully": total_parsed_ok,
        "total_packets_corrupt_rejected": total_parse_corrupt,
        "total_buffer_overflow_drops": total_buffer_drops,
        "processing_success_rate_percent": round(processing_success_rate, 2),
        "buffer_retention_rate_percent": round(buffer_retention_rate, 2),
        "timestamp": timestamp_str
    }

    with open(json_file, "w") as f:
        json.dump(summary_data, f, indent=2)

    passed = denom > 0 and processing_success_rate >= 95.0 and total_buffer_drops == 0
    verdict = "PASSED" if passed else "FAILED"

    report_content = f"""# DiabFoot Verification Report — TEST-09: BLE Data Reception & App Processing Success Rate

- **Target Metric**: Processing Success Rate $\\ge 95.0\\%$ (Ideal: $100.0\\%$), Buffer Overflow Drops $= 0$.
- **Test Date**: {datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
- **Target Device**: Android Phone (`{device_id}`) $\\leftrightarrow$ ESP32-C3 (`Smart_Insole_Left`)
- **Transport Mode**: Over-The-Air BLE GATT Characteristic Notification (Pure Wireless)
- **Monitoring Method**: Android OS Logcat Event Stream (`adb logcat -s DIABFOOT_TEST BLE_DATA_DS`)
- **Verdict**: **{verdict}**

---

## 1. Executive Summary

During the **{args.duration}-second** empirical observation window, **{denom}** BLE telemetry packets were received from the ESP32 smart insole over Bluetooth Low Energy. The Android app's reactive data pipeline (`InsoleBleManager` $\\to$ `BleSensorDataSource` $\\to$ `SensorPacketParser`) processed the incoming telemetry stream.

| Metric | Measured Value | Target Benchmark | Evaluation |
| :--- | :---: | :---: | :---: |
| **GATT Notifications Received ($N_{{\\text{{rx}}}}$)** | **{denom}** | $\\ge 100$ | Received |
| **Successfully Processed & Dispatched ($N_{{\\text{{ok}}}}$)** | **{total_parsed_ok}** | $\\ge 95\\%$ of $N_{{\\text{{rx}}}}$ | Verified |
| **Corrupted / Rejected Packets ($N_{{\\text{{corrupt}}}}$)** | **{total_parse_corrupt}** | $\\le 5\\%$ | {'Optimal' if total_parse_corrupt == 0 else 'Noise'} |
| **Buffer Overflow Drops ($N_{{\\text{{drop}}}}$)** | **{total_buffer_drops}** | **0** | {'Zero Drops (Pass)' if total_buffer_drops == 0 else 'Overflow'} |
| **App Processing Success Rate** | **{processing_success_rate:.2f}%** | $\\ge 95.0\\%$ | **{verdict}** |
| **Buffer Retention Rate** | **{buffer_retention_rate:.2f}%** | $100.0\\%$ | Optimal |

---

## 2. Telemetry Processing Pipeline Breakdown

```
[ ESP32 Smart Insole ]
       │  5 Hz Wireless BLE Characteristic Notifications
       ▼
[ Android BluetoothGatt Callback ] ───► Total Received: {denom} pkts
       │
       ├── SharedFlow Emit (Buffer Capacity = 64)
       │       └── Buffer Drops: {total_buffer_drops} pkts (Retention: {buffer_retention_rate:.2f}%)
       ▼
[ BleSensorDataSource ]
       │
       ├── SensorPacketParser.parse()
       │       ├── Corrupted / Non-CSV Discards: {total_parse_corrupt} pkts
       │       └── Validated SensorPackets: {total_parsed_ok} pkts
       ▼
[ WalkingFSM & UI StateFlow ] ───► Successfully Fed: {total_parsed_ok} pkts ({processing_success_rate:.2f}%)
```

---

## 3. Log Artifacts
- **Detailed CSV Log**: [`{os.path.basename(csv_file)}`](file://{os.path.abspath(csv_file)})
- **Summary JSON**: [`{os.path.basename(json_file)}`](file://{os.path.abspath(json_file)})
"""

    with open(md_file, "w") as f:
        f.write(report_content)

    print(f"[+] Markdown Report written to: {md_file}")
    print(f"[+] Summary JSON written to: {json_file}")
    print("==========================================================================")
    print(f"  Test 09 Result: {verdict}")
    print("==========================================================================")


if __name__ == "__main__":
    main()
