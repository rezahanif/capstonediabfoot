package com.project.insole.core.ble

/**
 * BLE UUIDs for Smart Insole Left and Right devices.
 * Matches the ESP32 firmware exactly.
 */
object InsoleUUIDs {
    // Left insole (Smart_Insole_Left)
    const val LEFT_SERVICE        = "4fa2c732-ca9a-4c20-9492-c167df3c942b"
    const val LEFT_CHARACTERISTIC = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

    // Right insole (Smart_Insole_Right)
    const val RIGHT_SERVICE        = "4fa2c732-ca9a-4c20-9492-c167df3c942c"
    const val RIGHT_CHARACTERISTIC = "beb5483e-36e1-4688-b7f5-ea07361b26c9"

    /**
     * Identifies the side (LEFT/RIGHT) based on the Service UUID string and/or device name.
     */
    fun identifySide(uuid: String?, name: String? = null): String {
        if (uuid != null) {
            if (uuid.contains(LEFT_SERVICE, ignoreCase = true)) return "LEFT"
            if (uuid.contains(RIGHT_SERVICE, ignoreCase = true)) return "RIGHT"
        }
        if (name != null) {
            if (name.contains("Right", ignoreCase = true)) return "RIGHT"
            if (name.contains("Left", ignoreCase = true)) return "LEFT"
        }
        return "UNKNOWN"
    }
}
