package com.project.insole.features.diagnostics.domain

import com.project.insole.features.diagnostics.data.DiagnosticsDataSource.DeviceStatus
import com.project.insole.features.diagnostics.data.DiagnosticsDataSource.InsoleHardwareDiagnostic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateDiagnosticReportUseCaseTest {

    private val useCase = GenerateDiagnosticReportUseCase()

    private fun sampleDiagnostic(
        side: String,
        connected: Boolean = true,
        rssi: Int? = -60,
        temp: Float = 31.5f,
        peakTemp: Float = 32.0f,
        pressure: Float = 50f,
        peakPressure: Float = 120f
    ) = InsoleHardwareDiagnostic(
        side = side,
        macAddress = if (connected) "AA:BB:CC:DD:EE:FF" else null,
        connectionState = if (connected) "Connected" else "Disconnected",
        rssiDbm = rssi,
        liveTemperatureC = temp,
        peakTemperatureC = peakTemp,
        livePressureKpa = pressure,
        peakPressureKpa = peakPressure,
        stepCount = 100
    )

    @Test
    fun `both insoles connected and balanced yields EXCELLENT health and balanced summary`() {
        val status = DeviceStatus(
            leftInsole = sampleDiagnostic("Left", temp = 31.0f),
            rightInsole = sampleDiagnostic("Right", temp = 31.4f),
            thermalAsymmetryC = 0.4f,
            isBilateralConnected = true,
            totalSteps = 200,
            walkState = "STANDING",
            batteryNotice = "Not available on hardware",
            firmwareProtocol = "v2 CSV",
            lastSyncTime = System.currentTimeMillis()
        )

        val report = useCase(status)

        assertEquals(HealthStatus.EXCELLENT, report.overallHealth)
        assertTrue(report.thermalAsymmetrySummary.contains("Balanced asymmetry"))
        assertTrue(report.thermalAsymmetrySummary.contains("0.40 °C"))
        assertTrue(report.signalQuality.contains("Strong Signal"))
    }

    @Test
    fun `elevated thermal asymmetry triggers FAIR health and elevated summary for doctor review`() {
        val status = DeviceStatus(
            leftInsole = sampleDiagnostic("Left", temp = 34.5f),
            rightInsole = sampleDiagnostic("Right", temp = 31.5f),
            thermalAsymmetryC = 3.0f, // >= 2.2°C clinical threshold
            isBilateralConnected = true,
            totalSteps = 500,
            walkState = "WALKING",
            batteryNotice = "Not available on hardware",
            firmwareProtocol = "v2 CSV",
            lastSyncTime = System.currentTimeMillis()
        )

        val report = useCase(status)

        assertEquals(HealthStatus.FAIR, report.overallHealth)
        assertTrue(report.thermalAsymmetrySummary.contains("Elevated asymmetry"))
        assertTrue(report.thermalAsymmetrySummary.contains("3.00 °C"))
        assertTrue(report.thermalAsymmetrySummary.contains("≥ 2.2 °C clinical threshold"))
    }

    @Test
    fun `disconnected insole yields POOR health`() {
        val status = DeviceStatus(
            leftInsole = sampleDiagnostic("Left", connected = true),
            rightInsole = sampleDiagnostic("Right", connected = false),
            thermalAsymmetryC = 0f,
            isBilateralConnected = false,
            totalSteps = 0,
            walkState = "UNKNOWN",
            batteryNotice = "Not available on hardware",
            firmwareProtocol = "v2 CSV",
            lastSyncTime = System.currentTimeMillis()
        )

        val report = useCase(status)

        assertEquals(HealthStatus.POOR, report.overallHealth)
    }
}
