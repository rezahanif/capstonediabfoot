package com.project.insole.features.sensor.domain.model

object SensorConstants {
    // Pressure thresholds — Calibrated physical units (kPa) for the 5-zone FSR insole.
    // Baseline reference: 60.0 kg subject walking trial produces up to 1,741 kPa total load.
    // 2,000 kPa warning threshold prevents false alarms during normal stance push-off (~25% safety margin).
    const val DEFAULT_REFERENCE_WEIGHT_KG = 60.0f
    const val PRESSURE_THRESHOLD_WARNING = 2000
    const val PRESSURE_THRESHOLD_CRITICAL = 2500

    // Temperature thresholds
    // Literature-sourced (JMIR mHealth 2025 once-per-minute plantar cohort;
    // IWGDF 2023): a 2.2 °C inter-foot difference is clinically significant.
    const val TEMPERATURE_DIFF_WARNING = 2.2f

    // Physiological skin temperature bounds (Armstrong et al., Lavery et al.)
    // Ambient room/shoe temp (< 27.0°C) and artifact/error sentinels are excluded.
    const val MIN_PHYSIOLOGICAL_TEMP_C = 27.0f
    const val MAX_PHYSIOLOGICAL_TEMP_C = 38.5f

    // Wear gating & stance pressure thresholds (kPa)
    const val MIN_WEAR_PRESSURE_KPA = 5.0f
    const val MIN_STANCE_PRESSURE_KPA = 20.0f
    const val MIN_CLEAN_SENSORS_ACTIVE = 3

    // Step counting thresholds — POC-tuned (owner validation), in g units.
    // (The firmware sends m/s² + rad/s; SensorUnitDetector converts before
    // these are applied.)
    const val STEP_THRESHOLD = 0.15f        // Higher to avoid noise
    const val MOTION_START_THRESHOLD = 0.12f // Higher to avoid noise
    const val MOTION_CONFIRM_THRESHOLD = 0.15f
    const val MOTION_STOP_THRESHOLD = 0.05f  // Higher to allow reset
}
