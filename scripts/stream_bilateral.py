#!/usr/bin/env python3
"""
Bilateral (Both Feet) Smart Insole USB Telemetry Streamer for DiabFoot Video Demo.
Streams 5 Hz real-time synchronized telemetry for BOTH LEFT and RIGHT feet
via USB (ADB bridge) directly into the DiabFoot Android app.

Usage:
    python3 scripts/stream_bilateral.py
"""

import sys
import time
import math
import socket
import select
import subprocess

PORT = 9999

def ensure_adb_forward():
    """Configures adb forward so localhost:9999 reaches the phone over USB."""
    adb_paths = [
        "adb",
        "/Users/reza/Library/Android/sdk/platform-tools/adb",
        "~/Library/Android/sdk/platform-tools/adb"
    ]
    adb_cmd = None
    for p in adb_paths:
        expanded = subprocess.os.path.expanduser(p)
        if subprocess.os.path.exists(expanded) or p == "adb":
            try:
                subprocess.run([expanded, "version"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
                adb_cmd = expanded
                break
            except Exception:
                continue

    if not adb_cmd:
        print("[!] Warning: ADB not found on default paths. Please run 'adb forward tcp:9999 tcp:9999' manually.")
        return False

    print(f"[*] Setting up USB forward via ADB ({adb_cmd})...")
    res = subprocess.run([adb_cmd, "forward", f"tcp:{PORT}", f"tcp:{PORT}"], capture_output=True, text=True)
    if res.returncode == 0:
        print(f"[✓] Port {PORT} successfully forwarded via USB.")
        return True
    else:
        print(f"[!] ADB forward returned: {res.stderr.strip()}")
        return False


class BilateralStreamer:
    def __init__(self):
        self.sock = None
        self.running = True
        
        # Scenario modes: 'rest' (standing default), 'walk', 'thermal_spike', 'high_pressure', 'uneven'
        self.mode = "rest"
        self.phase_left = 0.0
        self.phase_right = math.pi  # Opposite phase for natural alternating walking!
        self.left_steps = 0
        self.right_steps = 0
        self.left_temp_c = 31.2
        self.right_temp_c = 31.0
        self.packets_sent = 0

    def connect(self):
        print(f"[*] Connecting to DiabFoot app on localhost:{PORT}...")
        for attempt in range(10):
            try:
                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                s.connect(("127.0.0.1", PORT))
                self.sock = s
                print("[✓] CONNECTED TO DIABFOOT APP OVER USB!")
                return True
            except ConnectionRefusedError:
                time.sleep(1.0)
                print(f"    Waiting for app... (Make sure DiabFoot app is open on the phone) [{attempt+1}/10]")
        return False

    def generate_foot_packet(self, phase: float, is_left: bool) -> str:
        """
        Generates 5 Hz v2 CSV telemetry line:
        ax,ay,az,gx,gy,gz,p1_hallux,p2_mth1,p3_mth2,p4_mth4,p5_heel
        """
        if self.mode == "rest":
            ax, ay, az = 0.02, -0.98, -0.15
            gx, gy, gz = 0.1, 0.2, -0.1
            p1, p2, p3, p4, p5 = 20.0, 40.0, 40.0, 30.0, 60.0
            return f"{ax:.2f},{ay:.2f},{az:.2f},{gx:.1f},{gy:.1f},{gz:.1f},{p1:.0f},{p2:.0f},{p3:.0f},{p4:.0f},{p5:.0f}"

        sin_p = math.sin(phase)
        cos_p = math.cos(phase)

        # Stance impact vs swing phase (unweighting)
        is_stance = sin_p > 0.0

        if is_stance:
            # Stance impact acceleration & gyro (gravity along -Y, forward kick along Z)
            ax = 0.25 * sin_p
            ay = -0.95 + 0.20 * cos_p
            az = -0.25 + 0.35 * sin_p
            gx = 15.0 * cos_p
            gy = 30.0 * sin_p
            gz = 25.0 * cos_p

            # Rolling plantar pressure: Heel strike -> Midstance -> Toe off
            heel_phase = math.sin(phase)
            mid_phase = math.sin(phase + 0.9)
            toe_phase = math.sin(phase + 1.8)

            heel_p = max(0.0, heel_phase) * 450.0
            mth4_p = max(0.0, mid_phase) * 320.0
            mth2_p = max(0.0, mid_phase) * 360.0
            mth1_p = max(0.0, toe_phase) * 480.0
            hallux_p = max(0.0, toe_phase) * 400.0

            if (self.mode in ["high_pressure", "valid_swell"]) and is_left:
                # Exceed 2,000 kPa warning threshold on left foot (~2,560 kPa peak)
                heel_p *= 2.4
                mth1_p *= 2.4
                mth2_p *= 2.4
                mth4_p *= 2.4
            elif self.mode == "uneven":
                if is_left:
                    heel_p *= 1.3
                    mth1_p *= 1.3
                else:
                    heel_p *= 0.5
                    mth1_p *= 0.5
        else:
            # Swing phase: foot lifted in the air (unweighted < 40 kPa) + forward leg swing motion
            ax = 0.35 * cos_p
            ay = -0.65 + 0.30 * sin_p
            az = 0.10 * sin_p
            gx = -35.0 * cos_p
            gy = 60.0 * sin_p
            gz = -120.0 * cos_p

            heel_p = 0.0
            mth4_p = 0.0
            mth2_p = 0.0
            mth1_p = 0.0
            hallux_p = 0.0

        return f"{ax:.2f},{ay:.2f},{az:.2f},{gx:.1f},{gy:.1f},{gz:.1f},{hallux_p:.0f},{mth1_p:.0f},{mth2_p:.0f},{mth4_p:.0f},{heel_p:.0f}"

    def run(self):
        if not ensure_adb_forward():
            return

        if not self.connect():
            print("[!] Could not connect to phone. Make sure DiabFoot app is running on the screen.")
            return

        print("=" * 70)
        print("  DIABFOOT BILATERAL (BOTH FEET) USB STREAMER")
        print("=" * 70)
        print("Status: Both Left & Right feet streaming synchronized gait @ 5 Hz.")
        print("-" * 70)
        print("INTERACTIVE DEMO CONTROLS (Type key + Enter anytime during video):")
        print(" [1] Normal Walk         : Alternating steps, balanced pressure, ΔT = 0.2°C (All Clear)")
        print(" [2] Thermal Alert (ΔT)  : Left rises to 34.2°C (ΔT = 3.2°C ≥ 2.2°C -> 'Time to check')")
        print(" [3] High Pressure Alert : Left foot peak spikes > 2,000 kPa (Warning prompt)")
        print(" [4] Uneven Gait Balance : Left foot heavily loaded vs Right (Shows 'Uneven' balance)")
        print(" [5] Standing / Rest     : Zero steps, insole resting")
        print(" [q] Quit / Disconnect")
        print("=" * 70)

        heartbeat_counter = 0

        try:
            while self.running:
                # Check user input non-blocking
                rlist, _, _ = select.select([sys.stdin], [], [], 0)
                if rlist:
                    cmd = sys.stdin.readline().strip().lower()
                    if cmd in ["1", "w", "walk"]:
                        self.mode = "walk"
                        self.left_temp_c = 31.2
                        self.right_temp_c = 31.0
                        print("\n>>> [1] Normal Bilateral Walk: Steps active, Balanced ΔT=0.2°C")
                    elif cmd in ["2", "t", "temp", "thermal"]:
                        self.mode = "thermal_spike"
                        self.left_temp_c = 34.2
                        self.right_temp_c = 31.0
                        print(f"\n>>> [2] Thermal Asymmetry Alert: Left={self.left_temp_c}°C, Right={self.right_temp_c}°C (ΔT = 3.2°C ≥ 2.2°C)")
                    elif cmd in ["3", "p", "pressure"]:
                        self.mode = "high_pressure"
                        self.left_temp_c = 31.2
                        self.right_temp_c = 31.0
                        print("\n>>> [3] High Pressure Alert: Left foot load > 2,000 kPa (ΔT reset to 0.2°C)")
                    elif cmd in ["4", "u", "uneven"]:
                        self.mode = "uneven"
                        self.left_temp_c = 31.2
                        self.right_temp_c = 31.0
                        print("\n>>> [4] Uneven Gait Balance: Left heavily loaded, Right light (ΔT reset to 0.2°C)")
                    elif cmd in ["5", "s", "rest"]:
                        self.mode = "rest"
                        self.left_temp_c = 31.2
                        self.right_temp_c = 31.0
                        print("\n>>> [5] Standing / Rest: Static (ΔT reset to 0.2°C)")
                    elif cmd in ["q", "quit", "exit"]:
                        print("\nDisconnecting...")
                        self.sock.sendall(b"DISCONNECT\n")
                        self.running = False
                        break

                # Advance gait phases (opposite phase for Left and Right)
                if self.mode != "rest":
                    self.phase_left = (self.phase_left + (2.0 * math.pi / 5.0)) % (2.0 * math.pi)
                    self.phase_right = (self.phase_right + (2.0 * math.pi / 5.0)) % (2.0 * math.pi)
                    if math.sin(self.phase_left) > 0.8:
                        self.left_steps += 1
                    if math.sin(self.phase_right) > 0.8:
                        self.right_steps += 1

                # Generate 5 Hz telemetry packets for both feet
                if self.mode != "single_right":
                    left_line = f"LEFT:{self.generate_foot_packet(self.phase_left, is_left=True)}\n"
                    self.sock.sendall(left_line.encode("utf-8"))
                    self.packets_sent += 1

                if self.mode != "single_left":
                    right_line = f"RIGHT:{self.generate_foot_packet(self.phase_right, is_left=False)}\n"
                    self.sock.sendall(right_line.encode("utf-8"))
                    self.packets_sent += 1

                # Send temperature heartbeats every ~10 iterations (2 seconds)
                heartbeat_counter += 1
                if heartbeat_counter >= 10:
                    heartbeat_counter = 0
                    if self.mode != "single_right":
                        t_left = f"LEFT:T,{self.left_temp_c:.1f},95\n"
                        self.sock.sendall(t_left.encode("utf-8"))
                    if self.mode != "single_left":
                        t_right = f"RIGHT:T,{self.right_temp_c:.1f},95\n"
                        self.sock.sendall(t_right.encode("utf-8"))

                delta_t = abs(self.left_temp_c - self.right_temp_c)
                sys.stdout.write(
                    f"\rMode: {self.mode.upper():<14} | Packets: {self.packets_sent:<5} | "
                    f"Steps L:{self.left_steps:<3} R:{self.right_steps:<3} | "
                    f"Temp L:{self.left_temp_c:.1f}°C R:{self.right_temp_c:.1f}°C (Δ{delta_t:.1f}°C)"
                )
                sys.stdout.flush()

                time.sleep(0.2)  # 5 Hz cadence

        except (BrokenPipeError, ConnectionResetError):
            print("\n[!] Connection lost: App was closed or phone disconnected.")
        finally:
            if self.sock:
                try: self.sock.close()
                except Exception: pass
            print("\nStream closed.")


if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description="DiabFoot Bilateral Telemetry Streamer")
    parser.add_argument(
        "--condition",
        choices=["swell", "hotspot", "pressure", "normal", "single", "rest"],
        default="walk",
        help="Initial clinical scenario condition to stream"
    )
    args = parser.parse_args()

    streamer = BilateralStreamer()
    if args.condition == "swell":
        streamer.mode = "valid_swell"
        streamer.left_temp_c = 34.2
        streamer.right_temp_c = 31.0
        print("[!] Pre-setting condition: VALID SWELL (High Pressure + Thermal Spike on Left)")
    elif args.condition == "hotspot":
        streamer.mode = "walk"
        streamer.left_temp_c = 34.2
        streamer.right_temp_c = 31.0
        print("[!] Pre-setting condition: SUSTAINED HOTSPOT (Thermal Spike on Left, Normal Pressure)")
    elif args.condition == "pressure":
        streamer.mode = "high_pressure"
        streamer.left_temp_c = 31.2
        streamer.right_temp_c = 31.0
        print("[!] Pre-setting condition: MECHANICAL OVERLOAD (High Pressure on Left, Balanced Temp)")
    elif args.condition == "single":
        streamer.mode = "single_left"
        streamer.left_temp_c = 31.2
        streamer.right_temp_c = 31.0
        print("[!] Pre-setting condition: SINGLE INSOLE (Left Only Connected, Right Offline)")
    elif args.condition == "rest":
        streamer.mode = "rest"
        streamer.left_temp_c = 31.2
        streamer.right_temp_c = 31.0
        print("[!] Pre-setting condition: STANDING / REST (Zero walking steps)")
    else:
        streamer.mode = "walk"
        streamer.left_temp_c = 31.2
        streamer.right_temp_c = 31.0

    try:
        streamer.run()
    except KeyboardInterrupt:
        print("\nInterrupted by user.")
