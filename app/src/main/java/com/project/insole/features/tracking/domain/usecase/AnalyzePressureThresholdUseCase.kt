package com.project.insole.features.tracking.domain.usecase

import android.util.Log
import com.project.insole.core.notifications.InsoleNotificationManager
import com.project.insole.features.tracking.domain.model.InsoleSensorData
import javax.inject.Inject

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
 * Sends notifications to user when thresholds are breached.
 */
class AnalyzePressureThresholdUseCase @Inject constructor(
    private val notificationManager: InsoleNotificationManager
) {

    companion object {
        private const val TAG = "PressureThresholdUseCase"
    }

    private val PRESSURE_THRESHOLD = 200  // Warning threshold
    private val CRITICAL_THRESHOLD = 300  // Critical threshold

    /**
     * Analyzes sensor data and returns alerts if thresholds are exceeded.
     * Sends notifications for any alerts triggered.
     */
    operator fun invoke(sensorData: InsoleSensorData): List<ThresholdAlert> {
        val alerts = mutableListOf<ThresholdAlert>()

        // Check left and right foot pressure
        checkFootPressure("Left", sensorData.leftPressure, alerts)
        checkFootPressure("Right", sensorData.rightPressure, alerts)

        // Check temperature difference
        val tempDifference = kotlin.math.abs(sensorData.leftTemperature - sensorData.rightTemperature)
        if (tempDifference > 2.5f) {
            val alert = ThresholdAlert(
                message = "Significant temperature difference detected between feet",
                severity = AlertSeverity.WARNING,
                affectedZones = listOf("Left", "Right")
            )
            alerts.add(alert)
            notificationManager.sendPressureAlert(
                title = "Temperature Difference Alert",
                message = "Temperature difference: ${String.format("%.1f", tempDifference)}°C",
                affectedZone = "Both_Feet",
                severity = com.project.insole.core.notifications.AlertSeverity.WARNING
            )
            Log.w(TAG, alert.message)
        }

        // Check individual pressure zones if needed
        sensorData.pressureValues.forEachIndexed { index, pressure ->
            when {
                pressure >= CRITICAL_THRESHOLD -> {
                    val alert = ThresholdAlert(
                        message = "Critical pressure detected at zone $index",
                        severity = AlertSeverity.CRITICAL,
                        affectedZones = listOf(index.toString())
                    )
                    alerts.add(alert)
                    notificationManager.sendPressureAlert(
                        title = "Critical Pressure Alert",
                        message = alert.message,
                        affectedZone = "Zone_$index",
                        severity = com.project.insole.core.notifications.AlertSeverity.CRITICAL
                    )
                    Log.e(TAG, alert.message)
                }
                pressure >= PRESSURE_THRESHOLD -> {
                    val alert = ThresholdAlert(
                        message = "High pressure detected at zone $index",
                        severity = AlertSeverity.WARNING,
                        affectedZones = listOf(index.toString())
                    )
                    alerts.add(alert)
                    notificationManager.sendPressureAlert(
                        title = "High Pressure Alert",
                        message = alert.message,
                        affectedZone = "Zone_$index",
                        severity = com.project.insole.core.notifications.AlertSeverity.WARNING
                    )
                    Log.w(TAG, alert.message)
                }
            }
        }

        return alerts
    }

    /**
     * Check pressure for a specific foot (left or right).
     */
    private fun checkFootPressure(
        footName: String,
        pressure: Int,
        alerts: MutableList<ThresholdAlert>
    ) {
        when {
            pressure >= CRITICAL_THRESHOLD -> {
                val alert = ThresholdAlert(
                    message = "Critical pressure on $footName foot: $pressure",
                    severity = AlertSeverity.CRITICAL,
                    affectedZones = listOf(footName)
                )
                alerts.add(alert)
                notificationManager.sendPressureAlert(
                    title = "Critical Pressure - $footName Foot",
                    message = "Pressure level: $pressure",
                    affectedZone = footName,
                    severity = com.project.insole.core.notifications.AlertSeverity.CRITICAL
                )
                Log.e(TAG, alert.message)
            }
            pressure >= PRESSURE_THRESHOLD -> {
                val alert = ThresholdAlert(
                    message = "High pressure on $footName foot: $pressure",
                    severity = AlertSeverity.WARNING,
                    affectedZones = listOf(footName)
                )
                alerts.add(alert)
                notificationManager.sendPressureAlert(
                    title = "High Pressure - $footName Foot",
                    message = "Pressure level: $pressure",
                    affectedZone = footName,
                    severity = com.project.insole.core.notifications.AlertSeverity.WARNING
                )
                Log.w(TAG, alert.message)
            }
        }
    }
}
