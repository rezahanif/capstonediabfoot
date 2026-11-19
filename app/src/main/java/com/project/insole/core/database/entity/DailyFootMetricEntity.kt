package com.project.insole.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists daily clinical foot metrics for diabetic foot monitoring (TD-7).
 * Stores raw and clean thermal summaries (min/max/sum/samples), peak plantar pressures,
 * active wear minutes, stance pressure sums, and 2-day consecutive hotspot tracking.
 * Enables honest DATA_CONTRACT batch generation and multi-day Trends display.
 */
@Entity(tableName = "daily_foot_metrics")
data class DailyFootMetricEntity(
    @PrimaryKey val date: String,             // Format: YYYY-MM-DD
    val leftMinTemp: Float = 0f,
    val leftMaxTemp: Float = 0f,
    val leftSumTemp: Double = 0.0,
    val leftSamples: Int = 0,
    val rightMinTemp: Float = 0f,
    val rightMaxTemp: Float = 0f,
    val rightSumTemp: Double = 0.0,
    val rightSamples: Int = 0,
    val leftPeakPressureKpa: Float = 0f,
    val rightPeakPressureKpa: Float = 0f,
    val wearMinutes: Int = 0,

    // Clean data metrics (wear-gated, noise-filtered)
    val cleanLeftMinTemp: Float = 0f,
    val cleanLeftMaxTemp: Float = 0f,
    val cleanLeftSumTemp: Double = 0.0,
    val cleanLeftSamples: Int = 0,
    val cleanRightMinTemp: Float = 0f,
    val cleanRightMaxTemp: Float = 0f,
    val cleanRightSumTemp: Double = 0.0,
    val cleanRightSamples: Int = 0,
    val cleanWearMinutes: Int = 0,
    val cleanStancePressureSumKpa: Double = 0.0,
    val cleanStanceSamples: Int = 0,
    val consecutiveThermalDays: Int = 0,
    val sustainedThermalSide: String? = null
) {
    val leftAvgTemp: Float
        get() = if (leftSamples > 0) (leftSumTemp / leftSamples).toFloat() else 0f

    val rightAvgTemp: Float
        get() = if (rightSamples > 0) (rightSumTemp / rightSamples).toFloat() else 0f

    val thermalAsymmetryC: Float
        get() = if (leftSamples > 0 && rightSamples > 0) {
            kotlin.math.abs(leftAvgTemp - rightAvgTemp)
        } else 0f

    // ── Clean Data Computed Properties ─────────────────────────────────────
    val cleanLeftAvgTemp: Float
        get() = if (cleanLeftSamples > 0) (cleanLeftSumTemp / cleanLeftSamples).toFloat() else 0f

    val cleanRightAvgTemp: Float
        get() = if (cleanRightSamples > 0) (cleanRightSumTemp / cleanRightSamples).toFloat() else 0f

    val cleanThermalAsymmetryC: Float
        get() = if (cleanLeftSamples > 0 && cleanRightSamples > 0) {
            kotlin.math.abs(cleanLeftAvgTemp - cleanRightAvgTemp)
        } else 0f

    val cleanStanceMeanPressureKpa: Float
        get() = if (cleanStanceSamples > 0) (cleanStancePressureSumKpa / cleanStanceSamples).toFloat() else 0f

    val isSustainedHotspot2Day: Boolean
        get() = consecutiveThermalDays >= 2 && !sustainedThermalSide.isNullOrBlank()

    // ── Clinical Risk Scoring Function (Doctor-facing & Alert Guidance) ──
    val thermalExceed: Int
        get() {
            val cleanValid = cleanLeftSamples >= 10 && cleanRightSamples >= 10 && cleanThermalAsymmetryC >= 2.2f
            val legacyValid = leftSamples >= 10 && rightSamples >= 10 && thermalAsymmetryC >= 2.2f
            return if (isSustainedHotspot2Day || cleanValid || legacyValid) 1 else 0
        }

    val pressureExceed: Int
        get() = if (leftPeakPressureKpa >= 200f || rightPeakPressureKpa >= 200f || cleanStanceMeanPressureKpa >= 100f) 1 else 0

    val isValidSwell: Boolean
        get() = thermalExceed == 1 && pressureExceed == 1

    val riskLevel: String
        get() = when {
            thermalExceed == 1 -> "HIGH"
            pressureExceed == 1 -> "MEDIUM"
            else -> "LOW"
        }
}
