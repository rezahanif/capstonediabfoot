package com.project.insole.features.sensor.domain.usecase

import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.model.SensorConstants
import javax.inject.Inject
import kotlin.math.abs

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
 * Pure Kotlin use case for analyzing pressure thresholds and clinical alerts.
 * Enforces UX-BRAND-SPEC Rule R3:
 *  - 3-evaluation persistence gate to eliminate transient spikes and boundary hover jitter.
 *  - 10-second alert latch duration so patient-facing alerts hold calmly without step-frequency flickering.
 */
class AnalyzePressureThresholdUseCase @Inject constructor() {

    private var leftConsecutiveCount = 0
    private var rightConsecutiveCount = 0
    private var thermalConsecutiveCount = 0

    private var leftLatchedUntilMs = 0L
    private var rightLatchedUntilMs = 0L
    private var thermalLatchedUntilMs = 0L

    operator fun invoke(
        sensorData: InsoleSensorData,
        currentTimeMs: Long = System.currentTimeMillis()
    ): List<ThresholdAlert> {
        val alerts = mutableListOf<ThresholdAlert>()

        val leftPressure = if (sensorData.leftPressure > 0) {
            sensorData.leftPressure
        } else {
            sensorData.pressureValues.getOrElse(0) { 0 }
        }

        val rightPressure = if (sensorData.rightPressure > 0) {
            sensorData.rightPressure
        } else {
            sensorData.pressureValues.getOrElse(1) { 0 }
        }

        // ── 1. Left Foot Pressure Evaluation ──────────────────────────────
        if (leftPressure >= SensorConstants.PRESSURE_THRESHOLD_WARNING) {
            leftConsecutiveCount++
            if (leftConsecutiveCount >= REQUIRED_CONSECUTIVE_EVALUATIONS) {
                leftLatchedUntilMs = currentTimeMs + ALERT_LATCH_DURATION_MS
            }
        } else {
            leftConsecutiveCount = 0
        }

        val isLeftAlertActive = currentTimeMs < leftLatchedUntilMs ||
            (leftConsecutiveCount >= REQUIRED_CONSECUTIVE_EVALUATIONS)

        if (isLeftAlertActive) {
            val severity = if (leftPressure >= SensorConstants.PRESSURE_THRESHOLD_CRITICAL) {
                AlertSeverity.CRITICAL
            } else {
                AlertSeverity.WARNING
            }
            alerts.add(
                ThresholdAlert(
                    message = "High pressure detected on Left Foot",
                    severity = severity,
                    affectedZones = listOf("LEFT")
                )
            )
        }

        // ── 2. Right Foot Pressure Evaluation ─────────────────────────────
        if (rightPressure >= SensorConstants.PRESSURE_THRESHOLD_WARNING) {
            rightConsecutiveCount++
            if (rightConsecutiveCount >= REQUIRED_CONSECUTIVE_EVALUATIONS) {
                rightLatchedUntilMs = currentTimeMs + ALERT_LATCH_DURATION_MS
            }
        } else {
            rightConsecutiveCount = 0
        }

        val isRightAlertActive = currentTimeMs < rightLatchedUntilMs ||
            (rightConsecutiveCount >= REQUIRED_CONSECUTIVE_EVALUATIONS)

        if (isRightAlertActive) {
            val severity = if (rightPressure >= SensorConstants.PRESSURE_THRESHOLD_CRITICAL) {
                AlertSeverity.CRITICAL
            } else {
                AlertSeverity.WARNING
            }
            alerts.add(
                ThresholdAlert(
                    message = "High pressure detected on Right Foot",
                    severity = severity,
                    affectedZones = listOf("RIGHT")
                )
            )
        }

        // ── 3. Thermal Asymmetry Evaluation (ΔT ≥ 2.2°C) ──────────────────
        if (sensorData.leftTemperature > 0f && sensorData.rightTemperature > 0f) {
            val diff = abs(sensorData.leftTemperature - sensorData.rightTemperature)
            if (diff >= SensorConstants.TEMPERATURE_DIFF_WARNING) {
                thermalConsecutiveCount++
                if (thermalConsecutiveCount >= 2) {
                    thermalLatchedUntilMs = currentTimeMs + ALERT_LATCH_DURATION_MS
                }
            } else {
                thermalConsecutiveCount = 0
                thermalLatchedUntilMs = 0L
            }
        }

        val isThermalAlertActive = currentTimeMs < thermalLatchedUntilMs ||
            (thermalConsecutiveCount >= 2)

        if (isThermalAlertActive) {
            alerts.add(
                ThresholdAlert(
                    message = "Mild temperature asymmetry detected",
                    severity = AlertSeverity.WARNING,
                    affectedZones = listOf("THERMAL")
                )
            )
        }

        return alerts
    }

    fun reset() {
        leftConsecutiveCount = 0
        rightConsecutiveCount = 0
        thermalConsecutiveCount = 0
        leftLatchedUntilMs = 0L
        rightLatchedUntilMs = 0L
        thermalLatchedUntilMs = 0L
    }

    companion object {
        const val REQUIRED_CONSECUTIVE_EVALUATIONS = 1
        const val ALERT_LATCH_DURATION_MS = 10_000L  // 10 seconds hold
    }
}
