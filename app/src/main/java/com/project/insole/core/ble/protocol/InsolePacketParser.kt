package com.project.insole.core.ble.protocol

/**
 * Defines packet structure and byte-offset constants for parsing ESP32 sensor data.
 * All sensor readings arrive in a fixed binary protocol from the ESP32 insole.
 */
object InsolePacketParser {

    /**
     * Packet structure constants (byte offsets from start of packet)
     */
    object PacketOffsets {
        const val PACKET_HEADER_OFFSET = 0x00           // 1 byte: packet identifier (0xAA)
        const val PACKET_LENGTH_OFFSET = 0x01           // 1 byte: total payload length
        const val PAYLOAD_TYPE_OFFSET = 0x02            // 1 byte: message type
        const val PRESSURE_DATA_OFFSET = 0x03           // Start of pressure array
        const val TEMPERATURE_OFFSET = 0x23             // 4 bytes: temperature (float)
        const val STEP_COUNT_OFFSET = 0x27              // 2 bytes: step count (uint16)
        const val BATTERY_OFFSET = 0x29                 // 1 byte: battery percentage
        const val RSSI_OFFSET = 0x2A                    // 1 byte: RSSI signal strength
        const val TIMESTAMP_OFFSET = 0x2B               // 4 bytes: device timestamp
        const val CHECKSUM_OFFSET = 0x2F                // 1 byte: CRC8 checksum
    }

    /**
     * Payload type identifiers
     */
    object PayloadTypes {
        const val SENSOR_DATA: Byte = 0x01              // Real-time sensor reading
        const val CALIBRATION_DATA: Byte = 0x02         // Calibration command response
        const val DEVICE_INFO: Byte = 0x03              // Firmware version, device info
        const val ALERT_DATA: Byte = 0x04               // Alert/error from device
    }

    /**
     * Pressure sensor configuration
     */
    object PressureSensor {
        const val NUM_PRESSURE_SENSORS = 32             // Total pressure zones
        const val PRESSURE_BYTES_PER_SENSOR = 1         // 1 byte per sensor (0-255)
        const val PRESSURE_ARRAY_LENGTH = 32            // 32 bytes total for pressure
        const val PRESSURE_MIN_VALUE = 0
        const val PRESSURE_MAX_VALUE = 255
    }

    /**
     * Validates packet structure and checksum.
     */
    fun isValidPacket(data: ByteArray): Boolean {
        if (data.isEmpty()) return false
        
        // Check packet header
        if (data[PacketOffsets.PACKET_HEADER_OFFSET].toInt() != 0xAA) return false
        
        // Check minimum packet size
        if (data.size < (PacketOffsets.CHECKSUM_OFFSET + 1)) return false
        
        // Verify CRC8 checksum
        return verifyCrc8(data)
    }

    /**
     * Extracts pressure values from packet.
     */
    fun extractPressureValues(data: ByteArray): List<Int> {
        val pressureValues = mutableListOf<Int>()
        
        for (i in 0 until PressureSensor.NUM_PRESSURE_SENSORS) {
            val offset = PacketOffsets.PRESSURE_DATA_OFFSET + i
            if (offset < data.size) {
                pressureValues.add(data[offset].toInt() and 0xFF)
            }
        }
        
        return pressureValues
    }

    /**
     * Extracts temperature from packet (IEEE 754 float).
     */
    fun extractTemperature(data: ByteArray): Float {
        if (data.size < PacketOffsets.TEMPERATURE_OFFSET + 4) return 0f
        
        val bytes = data.copyOfRange(
            PacketOffsets.TEMPERATURE_OFFSET,
            PacketOffsets.TEMPERATURE_OFFSET + 4
        )
        
        return bytesToFloat(bytes)
    }

    /**
     * Extracts step count from packet (uint16 little-endian).
     */
    fun extractStepCount(data: ByteArray): Int {
        if (data.size < PacketOffsets.STEP_COUNT_OFFSET + 2) return 0
        
        val low = data[PacketOffsets.STEP_COUNT_OFFSET].toInt() and 0xFF
        val high = data[PacketOffsets.STEP_COUNT_OFFSET + 1].toInt() and 0xFF
        
        return (high shl 8) or low
    }

    /**
     * Extracts battery level from packet.
     */
    fun extractBatteryLevel(data: ByteArray): Int {
        if (data.size <= PacketOffsets.BATTERY_OFFSET) return 0
        return data[PacketOffsets.BATTERY_OFFSET].toInt() and 0xFF
    }

    /**
     * Extracts RSSI signal strength from packet.
     */
    fun extractRssi(data: ByteArray): Int {
        if (data.size <= PacketOffsets.RSSI_OFFSET) return 0
        
        val value = data[PacketOffsets.RSSI_OFFSET].toInt()
        // RSSI is stored as signed byte (-127 to 0 dBm)
        return if (value > 127) value - 256 else value
    }

    /**
     * Extracts device timestamp from packet (uint32 little-endian).
     */
    fun extractTimestamp(data: ByteArray): Long {
        if (data.size < PacketOffsets.TIMESTAMP_OFFSET + 4) return 0L
        
        var timestamp = 0L
        for (i in 0..3) {
            val byte = data[PacketOffsets.TIMESTAMP_OFFSET + i].toInt() and 0xFF
            timestamp = timestamp or (byte.toLong() shl (i * 8))
        }
        
        return timestamp
    }

    /**
     * Verifies CRC8 checksum of packet.
     */
    private fun verifyCrc8(data: ByteArray): Boolean {
        if (data.isEmpty()) return false
        
        val calculatedCrc = calculateCrc8(data, 0, data.size - 1)
        val packetCrc = data[data.size - 1].toInt() and 0xFF
        
        return calculatedCrc == packetCrc
    }

    /**
     * Calculates CRC8 checksum using polynomial 0x07.
     */
    private fun calculateCrc8(data: ByteArray, start: Int, end: Int): Int {
        var crc = 0
        
        for (i in start..end) {
            crc = crc xor (data[i].toInt() and 0xFF)
            for (j in 0 until 8) {
                if ((crc and 0x80) != 0) {
                    crc = ((crc shl 1) xor 0x07) and 0xFF
                } else {
                    crc = (crc shl 1) and 0xFF
                }
            }
        }
        
        return crc and 0xFF
    }

    /**
     * Converts 4 bytes to IEEE 754 float (little-endian).
     */
    private fun bytesToFloat(bytes: ByteArray): Float {
        if (bytes.size < 4) return 0f
        
        val intBits = (bytes[0].toInt() and 0xFF) or
                     ((bytes[1].toInt() and 0xFF) shl 8) or
                     ((bytes[2].toInt() and 0xFF) shl 16) or
                     ((bytes[3].toInt() and 0xFF) shl 24)
        
        return Float.fromBits(intBits)
    }
}
