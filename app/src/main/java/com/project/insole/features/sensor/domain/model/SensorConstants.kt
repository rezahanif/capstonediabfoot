package com.project.insole.features.sensor.domain.model

object SensorConstants {
    // Pressure thresholds (medically significant)
    // Firmware maps RFP (0-4095) to (0-255). 
    // CRITICAL_THRESHOLD must be <= 255.
    const val PRESSURE_THRESHOLD_WARNING = 200
    const val PRESSURE_THRESHOLD_CRITICAL = 240 

    // Temperature thresholds
    const val TEMPERATURE_DIFF_WARNING = 2.2f // 2.2°C difference is clinically significant
    
    // Step counting thresholds (tuned in G units)
    const val STEP_THRESHOLD = 0.08f
    const val MOTION_START_THRESHOLD = 0.05f
    const val MOTION_CONFIRM_THRESHOLD = 0.07f
    const val MOTION_STOP_THRESHOLD = 0.02f
}
