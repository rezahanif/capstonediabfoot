package com.project.insole.features.sensor.domain.model

/**
 * Low-level sensor packet from a single insole. One of three line types:
 *
 *  1. **v2 stream** — "ax,ay,az,gx,gy,gz,p1,p2,p3,p4,p5": six IMU values +
 *     five plantar FSRs (kPa). [pressuresKpa] holds p1..p5 in
 *     [FSR_ZONE_ORDER] order and [pressure] is their sum; [temperature] is
 *     0 (v2 reports temperature only on the "T," heartbeat line).
 *  2. **legacy v1 stream** — "ax,ay,az,gx,gy,gz,pressure,tempC": single FSR
 *     ([pressuresKpa] has that one value) + NTC temperature inline.
 *  3. **temperature-only heartbeat** — "T,tempC[,batteryPercent]":
 *     [isTemperatureOnly] = true, [pressuresKpa] empty, [imuValid] false.
 *
 * [imuValid] is false when all six IMU values are exactly 0 — a resting foot
 * always reports ≈ ±1 g on some axis, so all-zero means the MPU is absent,
 * not "at rest". Consumers must not feed such packets into unit detection
 * or the step FSM.
 *
 * UNITS: the accel/gyro values below are exactly what the firmware sent —
 * this model does NOT assume a unit system. They are converted into the
 * canonical internal units (g, deg/s) by [SensorUnitsProfile] in
 * BleSensorDataSource immediately after parsing, so downstream consumers
 * (WalkingFSM thresholds, UI) never deal with firmware units. Pressure and
 * temperature stay in raw firmware units (device pressure scale / °C).
 */
data class SensorPacket(
    val accelX: Float = 0f,   // ax — firmware units, normalized downstream
    val accelY: Float = 0f,   // ay
    val accelZ: Float = 0f,   // az
    val gyroX: Float = 0f,    // gx — firmware units, normalized downstream
    val gyroY: Float = 0f,    // gy
    val gyroZ: Float = 0f,    // gz
    val pressure: Float = 0f,     // total foot load (sum of pressuresKpa) — firmware units
    val temperature: Float = 0f,  // °C (legacy v1 inline, or the "T," heartbeat)
    /** Per-zone pressures in [FSR_ZONE_ORDER] order. 5 entries = v2, 1 = legacy, 0 = heartbeat. */
    val pressuresKpa: List<Float> = emptyList(),
    /** false when the IMU line is all-zero (MPU absent) or the packet carries no IMU data. */
    val imuValid: Boolean = true,
    /** true for the 1/min "T,..." temperature-only heartbeat line. */
    val isTemperatureOnly: Boolean = false,
    /** Battery percent (0..100) when the firmware reports it on the "T," line; null otherwise. */
    val batteryPercent: Int? = null,
) {
    companion object {
        /**
         * Thin delegate to [SensorPacketParser.parse] — kept so existing
         * callers/tests keep working. See the parser for the accepted line
         * formats and the strict rejection rules (malformed, corrupt, or
         * out-of-range packets return null rather than being coerced).
         */
        fun fromBleString(raw: String): SensorPacket? {
            return SensorPacketParser.parse(raw)
        }
    }
}
