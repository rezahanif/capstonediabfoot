package com.project.insole.features.notifications.domain

import com.project.insole.features.sensor.domain.model.SensorConstants
import kotlin.math.pow

/**
 * Versioned thresholds for the patient prompt rules (TD-8).
 *
 * "Configuration, not hard-coded": the ACTIVE config is loaded from the
 * cloud (`GET /v1/config/thresholds`, served by cloud/server), cached
 * locally, and falls back to [DEFAULT] when the network or the endpoint is
 * unavailable. Every prompt/episode produced under a config carries its
 * `version` — the same `threshold_version` field the DATA_CONTRACT stamps
 * on episodes and prompt-log entries — so the doctor app always knows which
 * rule regime generated the data.
 *
 * Changing clinical values requires a new version and clinical sign-off
 * (see docs/design/UX-BRAND-SPEC.md, Threshold provenance).
 */
data class ThresholdsConfig(
    val version: String,
    /** Inter-foot temperature difference (°C) above which a thermal prompt may fire. */
    val thermalDeltaWarningC: Float,
    /** Consecutive abnormal evaluations required before the thermal prompt fires. */
    val thermalPersistenceCount: Int,
    /** Thermal prompt silence window, in minutes. */
    val thermalCooldownMinutes: Int,
    /** Per-foot pressure (kPa) above which a pressure prompt may fire. */
    val pressureWarning: Int,
    /** Pressure prompt silence window per foot, in minutes. */
    val pressureCooldownMinutes: Int,
    /** Consecutive abnormal evaluations required before the pressure prompt fires (Rule R2). Default 3. */
    val pressurePersistenceCount: Int = 3,
    /** Patient body weight in kg. Null falls back to DEFAULT_REFERENCE_WEIGHT_KG (60.0 kg). */
    val userWeightKg: Float? = null
) {
    companion object {
        const val DEFAULT_REFERENCE_WEIGHT_KG = SensorConstants.DEFAULT_REFERENCE_WEIGHT_KG
        const val MAX_SCALED_PRESSURE_WARNING_KPA = 2800

        /**
         * Bundled defaults — physical kPa calibrated values, versioned "v1" to match
         * the DATA_CONTRACT. Mirrors SensorConstants so the two cannot drift.
         */
        val DEFAULT = ThresholdsConfig(
            version = "v1",
            thermalDeltaWarningC = SensorConstants.TEMPERATURE_DIFF_WARNING,
            thermalPersistenceCount = 3,
            thermalCooldownMinutes = 60,
            pressureWarning = SensorConstants.PRESSURE_THRESHOLD_WARNING,
            pressureCooldownMinutes = 30,
            pressurePersistenceCount = 3,
            userWeightKg = null
        )
    }

    /**
     * Calculates the effective pressure warning threshold (kPa) scaled by body weight.
     * Uses allometric scaling (mass^0.67) relative to the 60.0 kg reference subject.
     * Capped at MAX_SCALED_PRESSURE_WARNING_KPA for patient tissue safety.
     */
    fun effectivePressureWarning(): Int {
        val weight = userWeightKg ?: DEFAULT_REFERENCE_WEIGHT_KG
        if (weight <= 0f) return pressureWarning
        val scaleRatio = (weight / DEFAULT_REFERENCE_WEIGHT_KG).toDouble()
        val factor = scaleRatio.pow(0.67)
        val scaled = (pressureWarning * factor).toInt()
        return scaled.coerceAtMost(MAX_SCALED_PRESSURE_WARNING_KPA)
    }
}
