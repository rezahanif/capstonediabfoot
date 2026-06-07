# DiabFoot — IoT Diabetic Foot Monitoring

An Android application that monitors diabetic foot health in real time using a custom BLE-connected smart insole. Built with Kotlin, Jetpack Compose, Clean Architecture, and OOUX ORCA framework.

---

## Overview

DiabFoot connects to a pair of ESP32-based smart insoles via Bluetooth Low Energy to continuously monitor plantar pressure distribution and foot temperature — two critical early indicators of diabetic foot complications. Sensor data is streamed live to the app, visualized on an interactive foot pressure map, and stored via Supabase for longitudinal tracking.

---

## Features

- **Real-time BLE streaming** — Dual-insole connection (left & right) with auto-reconnect
- **Plantar pressure map** — Grid-based pressure visualization per foot
- **Temperature monitoring** — NTC thermistor readings with asymmetry alerts
- **Step counting** — Walking state detection using a dual-foot finite state machine
- **Threshold alerts** — Configurable pressure and temperature alert levels
- **Session history** — Trends and diagnostics stored to Supabase
- **Authentication** — Secure login and registration via Supabase Auth

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose with OOUX ORCA|
| Architecture | Clean Architecture (Presentation → Domain → Data) |
| DI | Hilt |
| BLE | Android BLE API (GATT) |
| Backend | Supabase (Auth + Database) |
| State | StateFlow / SharedFlow |
| Build | Gradle (KTS) |

---

## Architecture

```
app/
├── core/
│   ├── ble/                  # BLE manager, connection, data packet models
│   └── di/                   # Hilt modules
└── features/
    ├── auth/                 # Login, register
    │   ├── data/
    │   ├── domain/
    │   └── presentation/
    ├── sensor/               # Live BLE sensor monitoring
    │   ├── data/
    │   ├── domain/           # Use cases, models (pure Kotlin)
    │   └── presentation/
    ├── tracking/             # Dashboard & step tracking
    ├── trends/               # Historical data & charts
    ├── diagnostics/          # Foot health diagnostics
    ├── notifications/        # Alert management
    └── settings/             # App configuration
```

Each feature follows the same three-layer pattern: `data` (repositories, data sources) → `domain` (use cases, models) → `presentation` (ViewModel, Compose screens).

---

## Hardware

The insole hardware consists of:

- **ESP32-C3** microcontroller
- **MPU-6050** IMU (accelerometer + gyroscope) for step detection
- **FSR (Force Sensitive Resistor)** for plantar pressure
- **NTC thermistor** for skin temperature

The firmware sends comma-delimited BLE notifications at 5 Hz:

```
ax,ay,az,gx,gy,gz,pressure,temperature
```

Example: `0.1,-9.8,0.3,0.0,0.0,0.1,127.0,34.2`

Two devices are used — one per foot — each advertising a unique BLE service UUID.

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android device with BLE support (API 26+)
- Supabase project (for auth and data storage)

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/rezahanif/capstonediabfoot.git
   ```

2. Open in Android Studio and let Gradle sync.

3. Create a `local.properties` file in the project root and add your SDK path:
   ```
   sdk.dir=/path/to/your/Android/sdk
   ```

4. Add your Supabase credentials in the appropriate config file (see `core/di/AppModule.kt`).

5. Build and run on a physical device (BLE does not work on emulator).

### Permissions Required

```xml
BLUETOOTH_SCAN
BLUETOOTH_CONNECT
ACCESS_FINE_LOCATION
INTERNET
```

---

## BLE UUIDs

| Device | Service UUID |
|---|---|
| Left insole | `4fa2c732-ca9a-4c20-9492-c167df3c942a` |
| Right insole | `4fa2c732-ca9a-4c20-9492-c167df3c942c` |

Characteristic UUID (both): `beb5483e-36e1-4688-b7f5-ea07361b26c9`

---

## Project Status

This is a capstone project currently in active development.

- [x] BLE dual-device connection & streaming
- [x] Live pressure and temperature display
- [x] Step counting with walking FSM
- [x] Authentication (login / register)
- [ ] Supabase data persistence (in progress)
- [ ] Historical trends charts (in progress)
- [ ] Diagnostic report generation

---

## License

This project is for academic/capstone purposes.
