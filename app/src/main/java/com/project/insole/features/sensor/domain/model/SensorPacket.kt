package com.project.insole.features.sensor.domain.model

/**
 * Low-level sensor packet from a single insole.
 * Format: "ax,ay,az,gx,gy,gz,pressure,temperature"
 */
data class SensorPacket(
    val accelX: Float = 0f,   // ax (m/s²)
    val accelY: Float = 0f,   // ay
    val accelZ: Float = 0f,   // az
    val gyroX: Float = 0f,    // gx (rad/s)
    val gyroY: Float = 0f,    // gy
    val gyroZ: Float = 0f,    // gz
    val pressure: Float = 0f,     // index 6 (kPa / raw ADC)
    val temperature: Float = 0f,  // index 7 (°C)
) {
    companion object {
        /**
         * Parses: "ax,ay,az,gx,gy,gz,pressureKPa,tempCelsius"
         * Returns null if the string is malformed or too short.
         */
        fun fromBleString(raw: String): SensorPacket? {
            return try {
                val p = raw.trim().split(",").map { it.trim() }
                if (p.size < 8) return null
                SensorPacket(
                    accelX      = p[0].toFloatOrNull() ?: 0f,
                    accelY      = p[1].toFloatOrNull() ?: 0f,
                    accelZ      = p[2].toFloatOrNull() ?: 0f,
                    gyroX       = p[3].toFloatOrNull() ?: 0f,
                    gyroY       = p[4].toFloatOrNull() ?: 0f,
                    gyroZ       = p[5].toFloatOrNull() ?: 0f,
                    pressure    = p[6].toFloatOrNull() ?: 0f,
                    temperature = p[7].toFloatOrNull() ?: 0f,
                )
            } catch (_: Exception) { null }
        }
    }
}
