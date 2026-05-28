package com.project.insole.core.sensor

/**
 * Central repository for all sensor-related constants.
 * Includes thresholds, sampling rates, and calibration parameters.
 * Update these values based on device specifications and clinical requirements.
 */
object SensorConstants {

    /**
     * Pressure sensor thresholds (in arbitrary pressure units, 0-255 range)
     */
    object PressureThresholds {
        // Normal activity threshold - alert if exceeded
        const val PRESSURE_WARNING_THRESHOLD = 150

        // High pressure threshold - critical alert
        const val PRESSURE_CRITICAL_THRESHOLD = 200

        // Minimum detectable pressure (noise floor)
        const val PRESSURE_MIN_DETECTABLE = 10

        // Maximum valid pressure reading
        const val PRESSURE_MAX_VALUE = 255

        // Hysteresis to prevent alert flipping (units)
        const val PRESSURE_HYSTERESIS = 5
    }

    /**
     * Temperature sensor thresholds (in Celsius)
     */
    object TemperatureThresholds {
        // Normal skin temperature lower bound
        const val TEMP_MIN_NORMAL = 20f

        // Normal skin temperature upper bound
        const val TEMP_MAX_NORMAL = 37f

        // Warning threshold - elevated temperature may indicate inflammation
        const val TEMP_WARNING_THRESHOLD = 39f

        // Critical threshold - excessive heat
        const val TEMP_CRITICAL_THRESHOLD = 41f

        // Sensor malfunction threshold
        const val TEMP_SENSOR_ERROR_THRESHOLD = 50f
    }

    /**
     * Sampling rates and timing
     */
    object SamplingRates {
        // BLE notification rate from device (Hz)
        const val BLE_SENSOR_RATE_HZ = 10

        // BLE sensor interval in milliseconds
        const val BLE_SENSOR_INTERVAL_MS = 100L

        // MTU size for BLE packets
        const val BLE_MTU_SIZE = 512

        // Connection timeout duration
        const val CONNECTION_TIMEOUT_MS = 10000L

        // Reconnection attempt interval
        const val RECONNECTION_INTERVAL_MS = 5000L

        // Maximum reconnection attempts
        const val MAX_RECONNECTION_ATTEMPTS = 5

        // Data buffer size (number of readings to store)
        const val DATA_BUFFER_SIZE = 1000

        // Aggregation interval for analytics (seconds)
        const val AGGREGATION_INTERVAL_SECONDS = 60
    }

    /**
     * Step detection thresholds and parameters
     */
    object StepDetection {
        // Minimum pressure change to detect foot placement
        const val PRESSURE_CHANGE_THRESHOLD = 20

        // Minimum duration of pressure to register as step (ms)
        const val MIN_STEP_DURATION_MS = 300

        // Maximum duration to avoid counting as double-step (ms)
        const val MAX_STEP_DURATION_MS = 2000

        // Pressure zone that must be activated for step detection
        const val STEP_DETECTION_ZONES = 5  // e.g., heel, arch zones
    }

    /**
     * Battery and device health parameters
     */
    object DeviceHealth {
        // Battery critical level percentage
        const val BATTERY_CRITICAL_LEVEL = 10

        // Battery low warning level percentage
        const val BATTERY_LOW_LEVEL = 25

        // RSSI (signal strength) critical threshold (dBm)
        const val RSSI_CRITICAL_THRESHOLD = -100

        // RSSI warning threshold (dBm)
        const val RSSI_WARNING_THRESHOLD = -85

        // Connection quality threshold for seamless operation
        const val RSSI_GOOD_CONNECTION = -70

        // Maximum acceptable packet loss percentage
        const val MAX_PACKET_LOSS_PERCENT = 5
    }

    /**
     * Calibration parameters
     */
    object Calibration {
        // Number of readings per zone for calibration
        const val CALIBRATION_SAMPLES_PER_ZONE = 100

        // Maximum acceptable deviation during calibration
        const val CALIBRATION_MAX_DEVIATION = 15f

        // Calibration timeout (seconds)
        const val CALIBRATION_TIMEOUT_SECONDS = 60

        // Zero-load baseline offset for each sensor
        const val ZERO_OFFSET = 5

        // Sensor gain (pressure units per Newton)
        const val SENSOR_GAIN = 2.5f
    }

    /**
     * Alert cooldown periods (to avoid alert spam)
     */
    object AlertCooldown {
        // Cooldown for pressure alerts (seconds)
        const val PRESSURE_ALERT_COOLDOWN = 30

        // Cooldown for temperature alerts (seconds)
        const val TEMPERATURE_ALERT_COOLDOWN = 60

        // Cooldown for battery alerts (seconds)
        const val BATTERY_ALERT_COOLDOWN = 300

        // Cooldown for connection alerts (seconds)
        const val CONNECTION_ALERT_COOLDOWN = 60
    }

    /**
     * Data retention and storage
     */
    object DataRetention {
        // Hours of sensor data to keep in local database
        const val LOCAL_DATA_RETENTION_HOURS = 24

        // Maximum entries per day for historical data
        const val MAX_DAILY_ENTRIES = 1440  // 24 * 60 (one per minute)

        // Sync interval with cloud backend (seconds)
        const val CLOUD_SYNC_INTERVAL_SECONDS = 300  // 5 minutes

        // Batch size for cloud upload
        const val CLOUD_UPLOAD_BATCH_SIZE = 100
    }

    /**
     * Unit conversions
     */
    object UnitConversions {
        // Pressure: arbitrary units to millimeters of mercury (mmHg)
        const val PRESSURE_TO_MMHG = 0.01f

        // Pressure: arbitrary units to kilopascals (kPa)
        const val PRESSURE_TO_KPA = 0.001f

        // Step distance estimation (cm per step, varies by person)
        const val AVERAGE_STEP_LENGTH_CM = 75f
    }
}
