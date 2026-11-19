package com.project.insole.features.sensor.domain.model

import kotlin.math.roundToInt

/**
 * Stateless parser for the firmware's CSV telemetry lines. Three line types
 * are accepted (anything else is rejected as corrupt — substituting values
 * would inject fake "at rest" samples into the FSM and fake values into the
 * medical peak tracking):
 *
 *  1. **v2 stream** — "ax,ay,az,gx,gy,gz,p1,p2,p3,p4,p5" (11 numeric fields):
 *     six IMU values followed by the five plantar FSR readings (kPa, in
 *     [FSR_ZONE_ORDER] order).
 *  2. **legacy v1 stream** — "ax,ay,az,gx,gy,gz,pressure,tempC" (8 numeric
 *     fields): one FSR under the whole foot plus the NTC temperature inline.
 *  3. **temperature-only heartbeat** — "T,tempC[,batteryPercent]": sent by
 *     v2 firmware at ~1/min when the NTC has no pressure line to ride on.
 *
 * UNITS: accel/gyro values are exactly what the firmware sent — normalized
 * into the canonical internal units (g, deg/s) by [SensorUnitsProfile] in
 * BleSensorDataSource immediately after parsing. Pressure/temperature are
 * untouched (raw firmware units).
 */
object SensorPacketParser {

    /**
     * Plausible on-body NTC window (°C). Outside it the reading is a sensor
     * fault — in particular this rejects the firmware's −999 NTC-failure
     * sentinel instead of poisoning peak tracking and the UI with it.
     */
    private const val TEMP_MIN_C = -5f
    private const val TEMP_MAX_C = 55f

    private const val BATTERY_MIN_PERCENT = 0
    private const val BATTERY_MAX_PERCENT = 100

    /** Field counts of the two stream formats (trailing fields are ignored). */
    private const val V2_FIELD_COUNT = 11
    private const val LEGACY_FIELD_COUNT = 8

    fun parse(raw: String): SensorPacket? {
        val fields = raw.trim().split(",").map { it.trim() }

        // Temperature-only heartbeat: "T,<tempC>[,<batteryPercent>]".
        if (fields.firstOrNull() == "T") {
            return parseTemperatureLine(fields)
        }

        return when {
            fields.size >= V2_FIELD_COUNT -> parseStreamV2(fields)
            fields.size >= LEGACY_FIELD_COUNT -> parseLegacyV1(fields)
            else -> null
        }
    }

    /**
     * Parses a batched multi-line payload containing one or more newline-separated
     * stream lines and/or heartbeat lines. Returns an ordered list of all successfully
     * parsed packets. Malformed individual lines are skipped without dropping valid frames.
     */
    fun parseBatch(raw: String): List<SensorPacket> {
        val lines = raw.trim().lines()
        if (lines.isEmpty()) return emptyList()
        val result = ArrayList<SensorPacket>(lines.size)
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isNotEmpty()) {
                parse(trimmed)?.let { result.add(it) }
            }
        }
        return result
    }

    /** "T,<tempC>[,<batteryPercent>]" — no IMU, no pressure. */
    private fun parseTemperatureLine(fields: List<String>): SensorPacket? {
        if (fields.size < 2) return null

        val tempC = floatField(fields[1])?.let { temp ->
            temp.takeIf { it in TEMP_MIN_C..TEMP_MAX_C }
        } ?: return null

        // Optional battery percentage; a malformed/out-of-range value rejects
        // the whole packet (consistent with the wholesale-reject policy).
        val battery = if (fields.size >= 3) {
            floatField(fields[2])
                ?.takeIf { it >= BATTERY_MIN_PERCENT && it <= BATTERY_MAX_PERCENT }
                ?.roundToInt()
                ?: return null
        } else {
            null
        }

        return SensorPacket(
            temperature = tempC,
            batteryPercent = battery,
            pressuresKpa = emptyList(),
            imuValid = false,
            isTemperatureOnly = true,
        )
    }

    /** "ax,ay,az,gx,gy,gz,p1,p2,p3,p4,p5" — five-zone pressure stream. */
    private fun parseStreamV2(fields: List<String>): SensorPacket? {
        val accelX = floatField(fields[0]) ?: return null
        val accelY = floatField(fields[1]) ?: return null
        val accelZ = floatField(fields[2]) ?: return null
        val gyroX = floatField(fields[3]) ?: return null
        val gyroY = floatField(fields[4]) ?: return null
        val gyroZ = floatField(fields[5]) ?: return null

        // p1..p5 in FSR_ZONE_ORDER (Hallux, MTH1, MTH2, MTH4, Heel).
        val pressures = ArrayList<Float>(FSR_ZONE_ORDER.size)
        for (i in 0 until FSR_ZONE_ORDER.size) {
            val p = floatField(fields[6 + i]) ?: return null
            if (p < 0f) return null   // negative pressure is a corrupt reading
            pressures.add(p)
        }

        return SensorPacket(
            accelX = accelX,
            accelY = accelY,
            accelZ = accelZ,
            gyroX = gyroX,
            gyroY = gyroY,
            gyroZ = gyroZ,
            pressure = pressures.sum(),   // total foot load across the 5 FSRs
            temperature = 0f,             // v2 carries temperature only on the "T," heartbeat
            pressuresKpa = pressures,
            imuValid = !isImuAllZero(accelX, accelY, accelZ, gyroX, gyroY, gyroZ),
            isTemperatureOnly = false,
        )
    }

    /** "ax,ay,az,gx,gy,gz,pressureKPa,tempC" — legacy single-FSR stream. */
    private fun parseLegacyV1(fields: List<String>): SensorPacket? {
        val accelX = floatField(fields[0]) ?: return null
        val accelY = floatField(fields[1]) ?: return null
        val accelZ = floatField(fields[2]) ?: return null
        val gyroX = floatField(fields[3]) ?: return null
        val gyroY = floatField(fields[4]) ?: return null
        val gyroZ = floatField(fields[5]) ?: return null

        val pressure = floatField(fields[6]) ?: return null
        if (pressure < 0f) return null

        val tempC = floatField(fields[7])?.takeIf { it in TEMP_MIN_C..TEMP_MAX_C }
            ?: return null

        return SensorPacket(
            accelX = accelX,
            accelY = accelY,
            accelZ = accelZ,
            gyroX = gyroX,
            gyroY = gyroY,
            gyroZ = gyroZ,
            pressure = pressure,
            temperature = tempC,
            pressuresKpa = listOf(pressure),
            imuValid = !isImuAllZero(accelX, accelY, accelZ, gyroX, gyroY, gyroZ),
            isTemperatureOnly = false,
        )
    }

    /**
     * Zero-IMU guard: a resting foot still reports ≈ ±1 g on some axis, so
     * six exact zeros means the MPU is absent/unpowered, not "at rest".
     * Such packets must not pollute unit detection or drive the FSM.
     */
    private fun isImuAllZero(
        accelX: Float, accelY: Float, accelZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float,
    ): Boolean = accelX == 0f && accelY == 0f && accelZ == 0f &&
        gyroX == 0f && gyroY == 0f && gyroZ == 0f

    private fun floatField(s: String): Float? =
        s.toFloatOrNull()?.takeUnless { it.isNaN() || it.isInfinite() }
}
