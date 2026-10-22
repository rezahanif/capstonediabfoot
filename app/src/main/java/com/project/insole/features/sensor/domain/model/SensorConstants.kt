package com.project.insole.features.sensor.domain.model

object SensorConstants {
    // Pressure thresholds — DEVICE-SCALE PLACEHOLDER pending FSR calibration
    // (TD-11: firmware kPa values are uncalibrated conductance estimates).
    // NOT a clinical value; firmware maps RFP (0-4095) to (0-255).
    // CRITICAL_THRESHOLD must be <= 255.
    const val PRESSURE_THRESHOLD_WARNING = 200
    const val PRESSURE_THRESHOLD_CRITICAL = 240

    // Temperature thresholds
    // Literature-sourced (JMIR mHealth 2025 once-per-minute plantar cohort;
    // IWGDF 2023): a 2.2 °C inter-foot difference is clinically significant.
    const val TEMPERATURE_DIFF_WARNING = 2.2f

    // Step counting thresholds — POC-tuned (owner validation), in g units.
    // (The firmware sends m/s² + rad/s; SensorUnitDetector converts before
    // these are applied.)
    const val STEP_THRESHOLD = 0.15f        // Higher to avoid noise
    const val MOTION_START_THRESHOLD = 0.12f // Higher to avoid noise
    const val MOTION_CONFIRM_THRESHOLD = 0.15f
    const val MOTION_STOP_THRESHOLD = 0.05f  // Higher to allow reset
}
