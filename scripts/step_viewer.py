#!/usr/bin/env python3
"""
DiabFoot Guard — Step-by-Step Controlled BLE Gait Benchmark & CSV Logger

Workflow:
  1. Check & establish BLE connection to Smart_Insole_Left.
  2. Ask target number of steps (e.g. 5, 10).
  3. Interactive Step Loop:
     - Ketik [1] -> Countdown 3-2-1 -> Mulai rekam langkah ke-n.
     - Ketik [2] -> Stop rekam langkah ke-n (hitung & tampilkan rekap parameter gait).
     - Ketik [3] -> Simpan langkah ke-n ke CSV & persiapan langkah ke-(n+1).
       (Jika langkah terakhir sudah tercapai, ketik [3] langsung mengakhiri sesi dan menampilkan laporan akhir).
"""

import sys
import os
import time
import math
import asyncio
from datetime import datetime
from typing import List, Dict, Optional

try:
    from bleak import BleakScanner, BleakClient
except ImportError:
    print("[-] Error: bleak is required. Run via: uv run --with bleak python3 step_viewer.py")
    sys.exit(1)

# BLE Configuration
SERVICE_UUID        = "4fa2c732-ca9a-4c20-9492-c167df3c942b"
CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
DEVICE_NAME         = "Smart_Insole_Left"
GRAVITY             = 9.80665
RAD_TO_DEG          = 180.0 / math.pi


def make_gauge(val: int, max_val: int = 4095, width: int = 8) -> str:
    clamped = max(0, min(max_val, val))
    filled = int((clamped / float(max_val)) * width)
    return "#" * filled + "-" * (width - filled)


def analyze_step_data(step_index: int, frames: List[Dict]) -> Dict:
    """
    Computes continuous biomechanical parameters for a single recorded step trial.
    """
    if not frames:
        return {}

    duration_ms = frames[-1]["t_ms"] - frames[0]["t_ms"]
    if duration_ms <= 0:
        duration_ms = len(frames) * 20 # 50 Hz fallback

    def calc_stats(key):
        vals = [f[key] for f in frames]
        return {
            "min": min(vals),
            "avg": int(sum(vals) / len(vals)),
            "peak": max(vals)
        }

    heel_stats = calc_stats("heel")
    mth1_stats = calc_stats("mth1")
    hallux_stats = calc_stats("hallux")
    mth2_stats = calc_stats("mth2")
    mth4_stats = calc_stats("mth4")

    peak_heel = heel_stats["peak"]
    peak_hallux = hallux_stats["peak"]
    peak_impact_g = max(f["accel_mag_g"] for f in frames)
    peak_swing_dps = max(f["gyro_mag_dps"] for f in frames)

    t_heel_peak = next(f["t_ms"] for f in frames if f["heel"] == peak_heel)
    t_hallux_peak = next(f["t_ms"] for f in frames if f["hallux"] == peak_hallux)
    rollover_ms = max(0, t_hallux_peak - t_heel_peak) if t_heel_peak <= t_hallux_peak else 0

    stance_frames = sum(1 for f in frames if f["heel"] > 150 or f["hallux"] > 150 or f["mth1"] > 150 or f["mth2"] > 150 or f["mth4"] > 150)
    stance_ms = int((stance_frames / float(len(frames))) * duration_ms)
    swing_ms = duration_ms - stance_ms

    # Extract 5 sample slices across the step timeline (0%, 25%, 50%, 75%, 100%)
    slices = []
    n_frames = len(frames)
    indices = [0, int(n_frames * 0.25), int(n_frames * 0.50), int(n_frames * 0.75), n_frames - 1]
    seen = set()
    for idx in indices:
        if idx not in seen and idx < n_frames:
            seen.add(idx)
            f = frames[idx]
            rel_t = f["t_ms"] - frames[0]["t_ms"]
            slices.append({
                "rel_ms": rel_t,
                "heel": f["heel"],
                "mth": int((f["mth1"] + f["mth2"] + f["mth4"]) / 3.0),
                "hallux": f["hallux"],
                "impact_g": f["accel_mag_g"],
                "gyro_dps": f["gyro_mag_dps"]
            })

    return {
        "step_num": step_index,
        "duration_ms": duration_ms,
        "stance_ms": stance_ms,
        "swing_ms": swing_ms,
        "frames_count": len(frames),
        "heel_stats": heel_stats,
        "mth1_stats": mth1_stats,
        "hallux_stats": hallux_stats,
        "mth2_stats": mth2_stats,
        "mth4_stats": mth4_stats,
        "heel_peak": peak_heel,
        "mth1_peak": mth1_stats["peak"],
        "hallux_peak": peak_hallux,
        "mth2_peak": mth2_stats["peak"],
        "mth4_peak": mth4_stats["peak"],
        "impact_g": round(peak_impact_g, 2),
        "swing_dps": round(peak_swing_dps, 1),
        "rollover_ms": rollover_ms,
        "trajectory_slices": slices
    }


def print_step_card(step_stats: Dict):
    print("\n" + "-" * 82)
    print(f"\033[1;32m[✓] REKAP LANGKAH #{step_stats['step_num']:02d} — REKAMAN KONTINU (50 Hz):\033[0m")
    duration = max(1, step_stats['duration_ms'])
    stance_pct = (step_stats['stance_ms'] / duration * 100.0)
    swing_pct = 100.0 - stance_pct
    print(f"    • Total Data Kontinu  : \033[1;36m{step_stats['frames_count']} frame\033[0m (~{step_stats['duration_ms']} ms, sampled tiap 20 ms)")
    print(f"    • Fase Langkah        : Stance {step_stats['stance_ms']} ms ({stance_pct:.1f}%) | Swing {step_stats['swing_ms']} ms ({swing_pct:.1f}%)")
    print(f"    • Profil Kontinu 5 FSR (Min -> Rata-rata -> Puncak):")
    
    hs = step_stats["heel_stats"]
    m1 = step_stats["mth1_stats"]
    hx = step_stats["hallux_stats"]
    m2 = step_stats["mth2_stats"]
    m4 = step_stats["mth4_stats"]
    
    print(f"      1. Heel   (GPIO 0) : Min {hs['min']:4d}  ->  Avg {hs['avg']:4d}  ->  \033[1;33mPeak {hs['peak']:4d}\033[0m / 4095 [{make_gauge(hs['peak'])}]")
    print(f"      2. MTH1   (GPIO 1) : Min {m1['min']:4d}  ->  Avg {m1['avg']:4d}  ->  \033[1;33mPeak {m1['peak']:4d}\033[0m / 4095 [{make_gauge(m1['peak'])}]")
    print(f"      3. Hallux (GPIO 2) : Min {hx['min']:4d}  ->  Avg {hx['avg']:4d}  ->  \033[1;33mPeak {hx['peak']:4d}\033[0m / 4095 [{make_gauge(hx['peak'])}]")
    print(f"      4. MTH2   (GPIO 3) : Min {m2['min']:4d}  ->  Avg {m2['avg']:4d}  ->  \033[1;33mPeak {m2['peak']:4d}\033[0m / 4095 [{make_gauge(m2['peak'])}]")
    print(f"      5. MTH4   (GPIO 4) : Min {m4['min']:4d}  ->  Avg {m4['avg']:4d}  ->  \033[1;33mPeak {m4['peak']:4d}\033[0m / 4095 [{make_gauge(m4['peak'])}]")
    
    print(f"    • Dinamika Kinematika IMU:")
    print(f"      - Puncak Hentakan Impak (Landing) : {step_stats['impact_g']} g")
    print(f"      - Puncak Kecepatan Ayun Kaki      : {step_stats['swing_dps']} °/s")

    if step_stats.get("trajectory_slices"):
        print(f"    • Perjalanan Trajektori Waktu (Perubahan ADC Sepanjang Langkah):")
        for sl in step_stats["trajectory_slices"]:
            print(f"      - [+{sl['rel_ms']:4d} ms] Heel:{sl['heel']:4d} | MTH(avg):{sl['mth']:4d} | Hallux:{sl['hallux']:4d} | |a|:{sl['impact_g']:4.2f}g | Gyro:{sl['gyro_dps']:5.1f}°/s")

    if step_stats['rollover_ms'] > 0:
        print(f"    • Transisi Tumit -> Jempol   : {step_stats['rollover_ms']} ms (Roll-over Biomekanik Normal)")
    else:
        print(f"    • Transisi Tumit -> Jempol   : Pendaratan datar atau dorongan bersamaan")
    print("-" * 82)


def print_final_summary(steps_summary: List[Dict], csv_path: str):
    print("\n" + "=" * 115)
    print("                              DIABFOOT GUARD — GAIT & STEP BENCHMARK REPORT")
    print("=" * 115)
    print(f" {'Step':^5} | {'Total':^8} | {'Stance':^8} | {'Heel(G0)':^9} | {'MTH1(G1)':^9} | {'Hallux(G2)':^10} | {'MTH2(G3)':^9} | {'MTH4(G4)':^9} | {'Impact':^8} | {'Swing':^9}")
    print("-" * 115)

    total_time = 0
    total_stance = 0
    total_swing = 0
    total_impact = 0.0
    total_swing_speed = 0.0

    for s in steps_summary:
        total_time += s["duration_ms"]
        total_stance += s["stance_ms"]
        total_swing += s["swing_ms"]
        total_impact += s["impact_g"]
        total_swing_speed += s["swing_dps"]
        print(
            f"  #{s['step_num']:02d}  | {s['duration_ms']:5d}ms | {s['stance_ms']:5d}ms | "
            f"ADC {s['heel_peak']:4d} | ADC {s['mth1_peak']:4d} |  ADC {s['hallux_peak']:4d}  | "
            f"ADC {s['mth2_peak']:4d} | ADC {s['mth4_peak']:4d} | {s['impact_g']:5.2f}g | {s['swing_dps']:6.1f}°/s"
        )

    n = len(steps_summary)
    if n > 0:
        avg_total = total_time / n
        avg_stance = total_stance / n
        avg_swing = total_swing / n
        avg_impact = total_impact / n
        avg_swing_speed = total_swing_speed / n
        cadence = (60000.0 / avg_total) if avg_total > 0 else 0

        print("-" * 115)
        print("  RATA-RATA BIOMEKANIKA GAIT:")
        print(f"    • Total Langkah Dicatat      : {n} langkah")
        print(f"    • Rata-rata Durasi Langkah   : {avg_total:.0f} ms (Kadensi: {cadence:.1f} langkah/menit)")
        print(f"    • Rasio Stance / Swing       : {avg_stance:.0f} ms ({avg_stance/avg_total*100:.1f}%) stance / {avg_swing:.0f} ms ({avg_swing/avg_total*100:.1f}%) swing")
        print(f"    • Rata-rata Impak Hentakan   : {avg_impact:.2f} g")
        print(f"    • Rata-rata Kecepatan Ayun   : {avg_swing_speed:.1f} °/s")

    print(f"    • File Log CSV 50 Hz Tersimpan: {os.path.abspath(csv_path)}")
    print("=" * 115 + "\n")


async def get_user_cmd(prompt: str) -> str:
    """Async prompt for non-blocking stdin reads."""
    loop = asyncio.get_running_loop()
    return await loop.run_in_executor(None, input, prompt)


async def main_interactive():
    print("=" * 80)
    print("   DiabFoot Guard — Wireless BLE Step-by-Step Gait Benchmark")
    print("=" * 80)

    # 1. Check Connection First
    print(f"[*] Mengecek koneksi BLE ke '{DEVICE_NAME}' (UUID: {SERVICE_UUID})...")
    devices = await BleakScanner.discover(timeout=5.0)
    target_dev = None
    for d in devices:
        name = d.name or ""
        if DEVICE_NAME.lower() in name.lower():
            target_dev = d
            break

    if not target_dev:
        print(f"\n[-] ERROR: '{DEVICE_NAME}' tidak ditemukan via BLE.")
        print("    Pastikan ESP32 menyala dan LED berkedip.")
        sys.exit(1)

    print(f"[+] Ditemukan: {target_dev.name} ({target_dev.address})!")
    print("[*] Menghubungkan ke insole...")

    async with BleakClient(target_dev) as client:
        print(f"[+] \033[1;32mBLE Connected! Insole siap digunakan.\033[0m\n")

        # 2. Select Target Steps
        try:
            steps_input = await get_user_cmd("[?] Mau berapa langkah yang ingin dicatat? [default: 5]: ")
            steps_input = steps_input.strip()
            target_steps = int(steps_input) if steps_input.isdigit() and int(steps_input) > 0 else 5
        except (ValueError, EOFError):
            target_steps = 5

        print(f"[+] Target sesi: {target_steps} langkah.\n")

        # Open CSV file
        timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        os.makedirs("results", exist_ok=True)
        csv_path = f"results/gait_trial_{target_steps}steps_{timestamp}.csv"
        csv_file = open(csv_path, "w", buffering=1, encoding="utf-8")
        csv_file.write(
            "iso_timestamp,sample_ms,step_index,hallux_adc,mth1_adc,mth2_adc,mth4_adc,heel_adc,"
            "ax_ms2,ay_ms2,az_ms2,accel_mag_g,gx_rads,gy_rads,gz_rads,gyro_mag_dps\n"
        )

        steps_summary: List[Dict] = []
        current_step_frames: List[Dict] = []
        is_recording_step = False
        current_step_num = 1
        last_hud_time = 0

        # BLE Notification Callback
        def notification_handler(sender, data: bytearray):
            nonlocal last_hud_time
            line = data.decode("utf-8", errors="replace").strip()
            if not line.startswith("DATA,"):
                return
            parts = line.split(",")
            if len(parts) < 14:
                return

            try:
                ax = float(parts[7])
                ay = float(parts[8])
                az = float(parts[9])
                gx = float(parts[10])
                gy = float(parts[11])
                gz = float(parts[12])
                accel_mag = math.sqrt(ax * ax + ay * ay + az * az) / GRAVITY
                gyro_mag = math.sqrt(gx * gx + gy * gy + gz * gz) * RAD_TO_DEG

                frame = {
                    "iso_timestamp": datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3],
                    "t_ms": int(parts[1]),
                    "step_index": current_step_num,
                    "hallux": int(parts[2]),
                    "mth1": int(parts[3]),
                    "mth2": int(parts[4]),
                    "mth4": int(parts[5]),
                    "heel": int(parts[6]),
                    "ax": ax,
                    "ay": ay,
                    "az": az,
                    "accel_mag_g": accel_mag,
                    "gx": gx,
                    "gy": gy,
                    "gz": gz,
                    "gyro_mag_dps": gyro_mag
                }

                if is_recording_step:
                    current_step_frames.append(frame)
                    if csv_file:
                        csv_file.write(
                            f"{frame['iso_timestamp']},{frame['t_ms']},{frame['step_index']},"
                            f"{frame['hallux']},{frame['mth1']},{frame['mth2']},{frame['mth4']},{frame['heel']},"
                            f"{frame['ax']:.3f},{frame['ay']:.3f},{frame['az']:.3f},{frame['accel_mag_g']:.3f},"
                            f"{frame['gx']:.3f},{frame['gy']:.3f},{frame['gz']:.3f},{frame['gyro_mag_dps']:.1f}\n"
                        )

                    # Live HUD during recording
                    now = time.time()
                    if now - last_hud_time >= 0.05:
                        sys.stdout.write(
                            f"\r[REC #{current_step_num}] "
                            f"H(G0):{frame['heel']:4d} | "
                            f"M1(G1):{frame['mth1']:4d} | "
                            f"Hx(G2):{frame['hallux']:4d} | "
                            f"M2(G3):{frame['mth2']:4d} | "
                            f"M4(G4):{frame['mth4']:4d} | "
                            f"|a|:{accel_mag:4.2f}g | Ketik [2] lalu Enter untuk STOP  "
                        )
                        sys.stdout.flush()
                        last_hud_time = now

            except (ValueError, IndexError):
                pass

        await client.start_notify(CHARACTERISTIC_UUID, notification_handler)

        try:
            # 3. Step Loop: 1 -> 2 -> 3
            while current_step_num <= target_steps:
                # --- ACTION 1: START STEP N ---
                while True:
                    cmd1 = await get_user_cmd(
                        f"\n\033[1;36m[LANGKAH {current_step_num} / {target_steps}]\033[0m Ketik [\033[1;32m1\033[0m] lalu Enter untuk MULAI: "
                    )
                    if cmd1.strip() == "1":
                        break
                    elif cmd1.strip() == "q":
                        print("[*] Dibatalkan oleh user.")
                        return

                # Countdown 3-2-1
                print(f"[*] Bersiap untuk LANGKAH #{current_step_num}...")
                for count in range(3, 0, -1):
                    print(f"    [{count}] Bersiap melangkah dengan kaki KIRI...")
                    await asyncio.sleep(1.0)

                print(f"    \033[1;33m>>> [!!!] MULAI MELANGKAH SEKARANG! <<<\033[0m")
                print(f"    \033[90m(Saat langkah selesai, ketik '2' lalu tekan Enter untuk stop)\033[0m\n")

                current_step_frames.clear()
                is_recording_step = True

                # --- ACTION 2: STOP STEP N ---
                while True:
                    cmd2 = await get_user_cmd("")
                    if cmd2.strip() == "2":
                        is_recording_step = False
                        sys.stdout.write("\r" + " " * 115 + "\r") # Clear HUD line
                        sys.stdout.flush()
                        break

                # Compute step metrics & show rekap card
                step_stats = analyze_step_data(current_step_num, current_step_frames)
                if step_stats:
                    print_step_card(step_stats)
                else:
                    print("[-] Data tidak cukup pada langkah ini.\n")

                # --- ACTION 3: SAVE & PREPARE NEXT (OR FINISH) ---
                if current_step_num < target_steps:
                    prompt3 = f"Ketik [\033[1;33m3\033[0m] lalu Enter untuk SIMPAN langkah #{current_step_num} & PERSIAPAN langkah #{current_step_num + 1}: "
                else:
                    prompt3 = f"Semua {target_steps} langkah selesai! Ketik [\033[1;32m3\033[0m] lalu Enter untuk SIMPAN & SELESAI: "

                while True:
                    cmd3 = await get_user_cmd(prompt3)
                    if cmd3.strip() == "3":
                        csv_file.flush()
                        if step_stats:
                            steps_summary.append(step_stats)

                        print(f"\033[32m[+] Langkah #{current_step_num} ({len(current_step_frames)} frame kontinu) berhasil disimpan ke CSV!\033[0m")
                        break

                current_step_num += 1

            # End of session: print final summary
            csv_file.close()
            print_final_summary(steps_summary, csv_path)

        except (KeyboardInterrupt, asyncio.CancelledError):
            print("\n\n[*] Sesi dihentikan.")
            is_recording_step = False
            csv_file.close()
            if steps_summary:
                print_final_summary(steps_summary, csv_path)
        finally:
            await client.stop_notify(CHARACTERISTIC_UUID)
            print("[+] Disconnected dari insole.")


if __name__ == "__main__":
    try:
        asyncio.run(main_interactive())
    except KeyboardInterrupt:
        print("\n[+] Exited.")
