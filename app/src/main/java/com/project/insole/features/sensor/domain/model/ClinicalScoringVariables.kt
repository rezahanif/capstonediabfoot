package com.project.insole.features.sensor.domain.model

import com.project.insole.features.notifications.domain.FootSide

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Standardized variables foundation for clinical scoring, 2-day thermal hotspot tracking,
 * and clean data export to the doctor platform.
 *
 * All values are computed on clean on-body data (wear-gated) to ensure noise immunity.
 */
data class ClinicalScoringVariables(
    val date: String,
    val cleanWearMinutes: Int,
    val isCurrentlyWornClean: Boolean,
    val isCurrentlyActiveStance: Boolean,

    // ── Daily Clean Thermal Metrics ──────────────────────────────────────
    val cleanLeftAvgTemp: Float,
    val cleanRightAvgTemp: Float,
    val cleanThermalAsymmetryC: Float,
    val warmerFoot: FootSide?,
    val cleanLeftTempSamples: Int,
    val cleanRightTempSamples: Int,

    // ── 2-Day Consecutive Hotspot State (Armstrong / IWGDF) ──────────────
    val consecutiveThermalDays: Int,
    val sustainedThermalSide: FootSide?,
    val isSustainedHotspot2Day: Boolean,
    val yesterdayCleanThermalAsymmetryC: Float,
    val yesterdayWarmerFoot: FootSide?,

    // ── Clean Stance Plantar Pressure (Hourly & Daily) ───────────────────
    val currentHourCleanStanceMeanPressureKpa: Float,
    val currentHourStanceSamples: Int,
    val dailyCleanStanceMeanPressureKpa: Float,
    val dailyCleanStanceSamples: Int,
    val cleanLeftPeakPressureKpa: Float,
    val cleanRightPeakPressureKpa: Float,
    val pressureAsymmetryRatio: Float,

    // ── Clinical Data Quality & Sufficiency Gating ──────────────────────
    val isThermalDataSufficient: Boolean,
    val isPressureDataSufficient: Boolean,

    // ── Clinical Risk Scoring Function (Doctor-facing & Alert Guidance) ──
    val thermalExceed: Int = 0,         // 1 if thermal threshold exceeded, 0 otherwise
    val pressureExceed: Int = 0,        // 1 if pressure threshold exceeded, 0 otherwise
    val isValidSwell: Boolean = false,  // true if thermalExceed == 1 && pressureExceed == 1
    val riskLevel: RiskLevel = RiskLevel.LOW // HIGH, MEDIUM, LOW
) {
    companion object {
        /**
         * Clinical risk scoring truth table:
         * - thermalExceed == 1 && pressureExceed == 1 -> HIGH (Valid Swell)
         * - thermalExceed == 1 && pressureExceed == 0 -> HIGH (Sustained Hotspot)
         * - thermalExceed == 0 && pressureExceed == 1 -> MEDIUM (Mechanical Overload)
         * - thermalExceed == 0 && pressureExceed == 0 -> LOW (Normal Baseline)
         */
        fun calculateRisk(thermalExceed: Int, pressureExceed: Int): Pair<RiskLevel, Boolean> {
            val isValidSwell = thermalExceed == 1 && pressureExceed == 1
            val risk = when {
                thermalExceed == 1 -> RiskLevel.HIGH
                pressureExceed == 1 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            }
            return Pair(risk, isValidSwell)
        }

        val EMPTY = ClinicalScoringVariables(
            date = "",
            cleanWearMinutes = 0,
            isCurrentlyWornClean = false,
            isCurrentlyActiveStance = false,
            cleanLeftAvgTemp = 0f,
            cleanRightAvgTemp = 0f,
            cleanThermalAsymmetryC = 0f,
            warmerFoot = null,
            cleanLeftTempSamples = 0,
            cleanRightTempSamples = 0,
            consecutiveThermalDays = 0,
            sustainedThermalSide = null,
            isSustainedHotspot2Day = false,
            yesterdayCleanThermalAsymmetryC = 0f,
            yesterdayWarmerFoot = null,
            currentHourCleanStanceMeanPressureKpa = 0f,
            currentHourStanceSamples = 0,
            dailyCleanStanceMeanPressureKpa = 0f,
            dailyCleanStanceSamples = 0,
            cleanLeftPeakPressureKpa = 0f,
            cleanRightPeakPressureKpa = 0f,
            pressureAsymmetryRatio = 0f,
            isThermalDataSufficient = false,
            isPressureDataSufficient = false,
            thermalExceed = 0,
            pressureExceed = 0,
            isValidSwell = false,
            riskLevel = RiskLevel.LOW
        )
    }
}
