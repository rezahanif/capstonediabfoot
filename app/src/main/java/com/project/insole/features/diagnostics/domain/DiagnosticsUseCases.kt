package com.project.insole.features.diagnostics.domain

import com.project.insole.features.diagnostics.data.DiagnosticsDataSource.DeviceStatus
import com.project.insole.features.sensor.domain.model.SensorConstants
import javax.inject.Inject

/**
 * Pure Kotlin domain report for technical insole diagnostics (TD-4).
 * Combines hardware signal qualities with clinical asymmetry evaluation.
 */

data class DiagnosticReport(
    val status: DeviceStatus,
    val signalQuality: String,
    val thermalAsymmetrySummary: String,
    val overallHealth: HealthStatus
)

enum class HealthStatus {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}

class GenerateDiagnosticReportUseCase @Inject constructor() {
    operator fun invoke(status: DeviceStatus): DiagnosticReport {
        val leftRssi = status.leftInsole.rssiDbm
        val rightRssi = status.rightInsole.rssiDbm

        val signalQuality = when {
            leftRssi == null && rightRssi == null -> "Unknown (Not Scanned)"
            (leftRssi ?: -100) > -65 && (rightRssi ?: -100) > -65 -> "Strong Signal (>-65 dBm)"
            (leftRssi ?: -100) > -80 || (rightRssi ?: -100) > -80 -> "Acceptable Signal"
            else -> "Weak Signal (<-80 dBm)"
        }

        val asymmetrySummary = when {
            status.leftInsole.liveTemperatureC <= 0f || status.rightInsole.liveTemperatureC <= 0f ->
                "Awaiting bilateral temperature reading"
            status.thermalAsymmetryC >= SensorConstants.TEMPERATURE_DIFF_WARNING ->
                "Elevated asymmetry: %.2f °C (≥ 2.2 °C clinical threshold)".format(status.thermalAsymmetryC)
            else ->
                "Balanced asymmetry: %.2f °C (< 2.2 °C clinical threshold)".format(status.thermalAsymmetryC)
        }

        val overallHealth = when {
            !status.isBilateralConnected -> HealthStatus.POOR
            status.thermalAsymmetryC >= SensorConstants.TEMPERATURE_DIFF_WARNING -> HealthStatus.FAIR
            (leftRssi == null || leftRssi > -75) && (rightRssi == null || rightRssi > -75) -> HealthStatus.EXCELLENT
            else -> HealthStatus.GOOD
        }

        return DiagnosticReport(
            status = status,
            signalQuality = signalQuality,
            thermalAsymmetrySummary = asymmetrySummary,
            overallHealth = overallHealth
        )
    }
}
