package com.project.insole.features.notifications.domain

import com.project.insole.features.sensor.domain.model.SensorConstants

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
    /** Per-foot pressure (device scale) above which a pressure prompt may fire. */
    val pressureWarning: Int,
    /** Pressure prompt silence window per foot, in minutes. */
    val pressureCooldownMinutes: Int
) {
    companion object {
        /**
         * Bundled defaults — the POC-tuned values, versioned "v1" to match
         * the DATA_CONTRACT. Mirrors SensorConstants so the two cannot drift.
         */
        val DEFAULT = ThresholdsConfig(
            version = "v1",
            thermalDeltaWarningC = SensorConstants.TEMPERATURE_DIFF_WARNING,
            thermalPersistenceCount = 3,
            thermalCooldownMinutes = 60,
            pressureWarning = SensorConstants.PRESSURE_THRESHOLD_WARNING,
            pressureCooldownMinutes = 30
        )
    }
}
