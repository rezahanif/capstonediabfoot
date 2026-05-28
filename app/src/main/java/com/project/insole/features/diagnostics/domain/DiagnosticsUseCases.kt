package com.project.insole.features.diagnostics.domain

/**
 * Pure Kotlin domain use cases for device diagnostics.
 * No Android or BLE dependencies - only business logic.
 */

data class DiagnosticReport(
    val batteryStatus: String,
    val signalQuality: String,
    val overallHealth: HealthStatus
)

enum class HealthStatus {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}

class GenerateDiagnosticReportUseCase {
    operator fun invoke(
        batteryLevel: Int,
        rssiStrength: Int,
        connectionState: String
    ): DiagnosticReport {
        val batteryStatus = when (batteryLevel) {
            in 80..100 -> "Excellent"
            in 50..79 -> "Good"
            in 20..49 -> "Low"
            else -> "Critical"
        }

        val signalQuality = when (rssiStrength) {
            in -50..-30 -> "Excellent"
            in -70..-51 -> "Good"
            in -80..-71 -> "Fair"
            else -> "Poor"
        }

        val overallHealth = when {
            batteryLevel > 50 && rssiStrength > -70 && connectionState == "Connected" -> HealthStatus.EXCELLENT
            batteryLevel > 20 && rssiStrength > -80 && connectionState == "Connected" -> HealthStatus.GOOD
            else -> HealthStatus.POOR
        }

        return DiagnosticReport(batteryStatus, signalQuality, overallHealth)
    }
}
