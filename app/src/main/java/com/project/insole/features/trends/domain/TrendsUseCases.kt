package com.project.insole.features.trends.domain

/**
 * Pure Kotlin domain use cases for trends and analytics.
 * No Android or BLE dependencies - only business logic.
 */

class CalculateHealthScoreUseCase {
    operator fun invoke(
        avgPressure: Int,
        avgTemperature: Float,
        totalSteps: Int,
        alerts: Int
    ): Float {
        // Score calculation: 0-100
        var score = 100f

        // Deduct for high pressure zones
        if (avgPressure > 150) score -= (avgPressure - 150) * 0.1f

        // Deduct for abnormal temperature
        if (avgTemperature > 37 || avgTemperature < 20) score -= 10f

        // Bonus for good step count
        if (totalSteps > 7000) score += 5f

        // Deduct heavily for alerts
        score -= (alerts * 5f)

        return score.coerceIn(0f, 100f)
    }
}

class DetectTrendUseCase {
    operator fun invoke(previousValue: Float, currentValue: Float): Trend {
        return when {
            currentValue > previousValue * 1.1f -> Trend.INCREASING
            currentValue < previousValue * 0.9f -> Trend.DECREASING
            else -> Trend.STABLE
        }
    }
}

enum class Trend {
    INCREASING,
    STABLE,
    DECREASING
}
