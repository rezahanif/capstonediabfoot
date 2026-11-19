#!/usr/bin/env python3
"""
Simulate Insole BLE Peripheral for DiabFoot Video Demo.
Broadcasts a real BLE GATT peripheral from macOS that the DiabFoot Android app
can discover and connect to over Bluetooth Low Energy.

Usage:
    uv run --with bless python3 scripts/simulate_insole_ble.py [--side left|right]
"""

import sys
import time
import math
import asyncio
import argparse
import select
from bless import (
    BlessServer,
    GATTCharacteristicProperties,
    GATTAttributePermissions
)

# UUIDs matching ESP32 firmware and Android app (InsoleUUIDs.kt)
INSOLE_PROFILES = {
    "left": {
        "name": "Smart_Insole_Left",
        "service_uuid": "4fa2c732-ca9a-4c20-9492-c167df3c942b",
        "char_uuid": "beb5483e-36e1-4688-b7f5-ea07361b26a8",
    },
    "right": {
        "name": "Smart_Insole_Right",
        "service_uuid": "4fa2c732-ca9a-4c20-9492-c167df3c942c",
        "char_uuid": "beb5483e-36e1-4688-b7f5-ea07361b26c9",
    }
}

class InsoleSimulator:
    def __init__(self, side: str = "left"):
        self.side = side.lower()
        self.profile = INSOLE_PROFILES[self.side]
        self.server = None
        self.running = True
        
        # State machine modes: 'walk', 'high_pressure', 'thermal_spike', 'rest'
        self.mode = "walk"
        self.step_count = 0
        self.phase = 0.0  # 0.0 to 2*pi
        self.temp_c = 31.2
        self.packets_sent = 0
        
    def generate_packet(self) -> str:
        """
        Generates realistic 5 Hz v2 CSV telemetry line:
        ax,ay,az,gx,gy,gz,p1_hallux,p2_mth1,p3_mth2,p4_mth4,p5_heel
        Pressures in kPa.
        """
        if self.mode == "rest":
            ax, ay, az = 0.02, 0.05, 0.98
            gx, gy, gz = 0.1, 0.2, -0.1
            p1 = 40.0
            p2 = 80.0
            p3 = 75.0
            p4 = 70.0
            p5 = 120.0
            return f"{ax:.2f},{ay:.2f},{az:.2f},{gx:.1f},{gy:.1f},{gz:.1f},{p1:.0f},{p2:.0f},{p3:.0f},{p4:.0f},{p5:.0f}"

        # Gait cycle advancement (5 Hz -> 5 samples per cycle ~ 1 sec / step)
        self.phase = (self.phase + (2.0 * math.pi / 5.0)) % (2.0 * math.pi)
        sin_p = math.sin(self.phase)
        cos_p = math.cos(self.phase)
        
        if sin_p > 0.8:
            self.step_count += 1

        # Accelerometer swing for WalkingFSM (triggers step detection above 0.15g threshold)
        ax = 0.15 * sin_p
        ay = 0.10 * cos_p
        az = 1.0 + (0.35 * sin_p)  # Accelerometer swing ~1.35g peak
        gx = 15.0 * cos_p
        gy = 45.0 * sin_p
        gz = 5.0 * cos_p

        # Dynamic 5-zone Plantar Pressure (Heel -> Midfoot -> Toe roll)
        heel_phase = math.sin(self.phase)
        mid_phase = math.sin(self.phase + 1.0)
        toe_phase = math.sin(self.phase + 2.0)

        heel_p = max(0.0, heel_phase) * 600.0 + 80.0
        mth4_p = max(0.0, mid_phase) * 450.0 + 60.0
        mth2_p = max(0.0, mid_phase) * 500.0 + 70.0
        mth1_p = max(0.0, toe_phase) * 650.0 + 90.0
        hallux_p = max(0.0, toe_phase) * 550.0 + 50.0

        if self.mode == "high_pressure":
            # Exceed 2,000 kPa warning threshold (e.g. 2,250 kPa peak)
            heel_p *= 2.4
            mth1_p *= 2.2
            mth2_p *= 2.1
            
        return f"{ax:.2f},{ay:.2f},{az:.2f},{gx:.1f},{gy:.1f},{gz:.1f},{hallux_p:.0f},{mth1_p:.0f},{mth2_p:.0f},{mth4_p:.0f},{heel_p:.0f}"

    async def run(self):
        print("=" * 65)
        print(f"  DiabFoot BLE Peripheral Simulator — {self.profile['name']}")
        print("=" * 65)
        print(f"Service UUID : {self.profile['service_uuid']}")
        print(f"Char UUID    : {self.profile['char_uuid']}")
        print("-" * 65)
        print("Initializing BLE peripheral on macOS...")

        self.server = BlessServer(name=self.profile["name"])
        await self.server.add_new_service(self.profile["service_uuid"])
        
        flags = GATTCharacteristicProperties.read | GATTCharacteristicProperties.notify
        perms = GATTAttributePermissions.readable
        await self.server.add_new_characteristic(
            self.profile["service_uuid"],
            self.profile["char_uuid"],
            flags,
            None,
            perms
        )

        await self.server.start()
        print(f"[*] BLE Peripheral is ADVERTISING as '{self.profile['name']}'!")
        print("-" * 65)
        print("INSTRUCTIONS FOR YOUR VIDEO DEMO:")
        print(" 1. In DiabFoot app, go to Settings -> 'Paired insoles' (or tap Home insole card).")
        print(f" 2. Tap Refresh — '{self.profile['name']}' will appear in the list.")
        print(" 3. Tap 'Connect' to pair!")
        print("-" * 65)
        print("INTERACTIVE DEMO CONTROLS (Type hotkey + Enter anytime):")
        print(" [1] Normal Walk         : Steps count up, normal pressure (<1,800 kPa), temp 31.2°C")
        print(" [2] Pressure Alert      : Spikes pressure > 2,000 kPa (triggers High Pressure prompt)")
        print(" [3] Thermal Spike Alert : Raises temp to 34.0°C (ΔT > 2.2°C triggers 'Time to check')")
        print(" [4] Stand / Rest        : Insole at rest (0 steps)")
        print(" [q] Quit Simulator")
        print("=" * 65)

        char = self.server.get_characteristic(self.profile["char_uuid"])
        loop = asyncio.get_event_loop()
        heartbeat_counter = 0

        while self.running:
            # Check keyboard input non-blocking
            rlist, _, _ = select.select([sys.stdin], [], [], 0)
            if rlist:
                line = sys.stdin.readline().strip().lower()
                if line in ["1", "w", "walk"]:
                    self.mode = "walk"
                    self.temp_c = 31.2
                    print("\n>>> Switched to: [1] Normal Walk (Steps active, balanced temp)")
                elif line in ["2", "p", "pressure"]:
                    self.mode = "high_pressure"
                    print("\n>>> Switched to: [2] High Pressure Alert (Peak > 2000 kPa)")
                elif line in ["3", "t", "temp", "thermal"]:
                    self.mode = "thermal_spike"
                    self.temp_c = 34.2
                    print("\n>>> Switched to: [3] Thermal Spike Alert (Temp = 34.2°C -> ΔT > 2.2°C)")
                elif line in ["4", "s", "rest"]:
                    self.mode = "rest"
                    print("\n>>> Switched to: [4] Standing / Rest (Static)")
                elif line in ["q", "quit", "exit"]:
                    print("\nStopping simulator...")
                    self.running = False
                    break

            # 5 Hz Stream: 1 telemetry packet every 200ms
            telemetry = self.generate_packet()
            char.value = telemetry.encode("utf-8")
            self.server.update_value(self.profile["service_uuid"], self.profile["char_uuid"])
            self.packets_sent += 1

            # Send Temperature Heartbeat "T,<tempC>" every ~20 packets (4 seconds)
            heartbeat_counter += 1
            if heartbeat_counter >= 20:
                heartbeat_counter = 0
                t_packet = f"T,{self.temp_c:.1f},95"
                char.value = t_packet.encode("utf-8")
                self.server.update_value(self.profile["service_uuid"], self.profile["char_uuid"])

            # Console live status line
            sys.stdout.write(
                f"\rMode: {self.mode.upper():<14} | Packets: {self.packets_sent:<5} | "
                f"Est Steps: {self.step_count:<4} | Temp: {self.temp_c:.1f}°C"
            )
            sys.stdout.flush()

            await asyncio.sleep(0.2)  # 5 Hz

        await self.server.stop()
        print("\nBLE Server stopped gracefully.")


def main():
    parser = argparse.ArgumentParser(description="DiabFoot Smart Insole BLE Simulator")
    parser.add_argument(
        "--side",
        choices=["left", "right"],
        default="left",
        help="Insole foot side to simulate (default: left)"
    )
    args = parser.parse_args()

    simulator = InsoleSimulator(side=args.side)
    try:
        asyncio.run(simulator.run())
    except KeyboardInterrupt:
        print("\nInterrupted by user.")

if __name__ == "__main__":
    main()
