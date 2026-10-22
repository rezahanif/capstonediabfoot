package com.project.insole.features.diagnostics.domain

import javax.inject.Inject

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

class GenerateDiagnosticReportUseCase @Inject constructor() {
    operator fun invoke(
        batteryLevel: Int?,
        rssiStrength: Int?,
        connectionState: String
    ): DiagnosticReport {
        // Null = the firmware cannot report the value (TD-13: no battery
        // monitoring hardware). Report it honestly as Unavailable — never a
        // placeholder number.
        val batteryStatus = when {
            batteryLevel == null -> "Unavailable"
            batteryLevel in 80..100 -> "Excellent"
            batteryLevel in 50..79 -> "Good"
            batteryLevel in 20..49 -> "Low"
            else -> "Critical"
        }

        val signalQuality = when {
            rssiStrength == null -> "Unknown"
            rssiStrength in -50..-30 -> "Excellent"
            rssiStrength in -70..-51 -> "Good"
            rssiStrength in -80..-71 -> "Fair"
            else -> "Poor"
        }

        val overallHealth = when {
            connectionState != "Connected" -> HealthStatus.POOR
            batteryLevel != null && batteryLevel > 50 && (rssiStrength == null || rssiStrength > -70) -> HealthStatus.EXCELLENT
            batteryLevel != null && batteryLevel > 20 && (rssiStrength == null || rssiStrength > -80) -> HealthStatus.GOOD
            batteryLevel == null && (rssiStrength == null || rssiStrength > -80) -> HealthStatus.FAIR
            else -> HealthStatus.POOR
        }

        return DiagnosticReport(batteryStatus, signalQuality, overallHealth)
    }
}
