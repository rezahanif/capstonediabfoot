package com.project.insole.features.sensor.domain.usecase

import com.project.insole.features.sensor.domain.model.InsoleSensorData

data class ThresholdAlert(
    val message: String,
    val severity: AlertSeverity,
    val affectedZones: List<String>
)

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

/**
 * Pure Kotlin use case for analyzing pressure thresholds.
 * Triggers alerts if pressure exceeds safety limits for diabetic patients.
 * No Android or BLE dependencies - just business logic.
 */
class AnalyzePressureThresholdUseCase {

    private val PRESSURE_THRESHOLD = 200  // Example threshold in arbitrary units
    private val CRITICAL_THRESHOLD = 300

    /**
     * Analyzes sensor data and returns alerts if thresholds are exceeded.
     */
    operator fun invoke(sensorData: InsoleSensorData): List<ThresholdAlert> {
        val alerts = mutableListOf<ThresholdAlert>()

        sensorData.pressureValues.forEachIndexed { index, pressure ->
            when {
                pressure >= CRITICAL_THRESHOLD -> {
                    alerts.add(
                        ThresholdAlert(
                            message = "Critical pressure detected at zone $index",
                            severity = AlertSeverity.CRITICAL,
                            affectedZones = listOf(index.toString())
                        )
                    )
                }
                pressure >= PRESSURE_THRESHOLD -> {
                    alerts.add(
                        ThresholdAlert(
                            message = "High pressure detected at zone $index",
                            severity = AlertSeverity.WARNING,
                            affectedZones = listOf(index.toString())
                        )
                    )
                }
            }
        }

        return alerts
    }
}
