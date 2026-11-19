package com.project.insole.features.sensor.domain.usecase

import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.model.WalkState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnalyzePressureThresholdUseCaseTest {

    private lateinit var useCase: AnalyzePressureThresholdUseCase

    @Before
    fun setUp() {
        useCase = AnalyzePressureThresholdUseCase()
    }

    private fun createData(
        leftPressure: Int = 1400,
        rightPressure: Int = 1400,
        leftTemp: Float = 32.0f,
        rightTemp: Float = 32.0f,
    ): InsoleSensorData {
        return InsoleSensorData(
            pressureValues = listOf(leftPressure, rightPressure),
            temperature = (leftTemp + rightTemp) / 2f,
            leftTemperature = leftTemp,
            rightTemperature = rightTemp,
            leftPressure = leftPressure,
            rightPressure = rightPressure,
            stepCount = 100,
            walkState = WalkState.WALKING,
            combinedAccelMag = 1.0f,
            batteryLevel = 90,
            timestamp = 1000L
        )
    }

    @Test
    fun `normal walking stance at 1500 kPa does not trigger alert`() {
        val data = createData(leftPressure = 1500, rightPressure = 1450)
        for (i in 0 until 10) {
            val alerts = useCase(data, currentTimeMs = 1000L + i * 200)
            assertTrue(alerts.isEmpty())
        }
    }

    @Test
    fun `overload stance impact of 2050 kPa triggers alert and latches`() {
        val spikeData = createData(leftPressure = 2050)
        val alerts = useCase(spikeData, currentTimeMs = 1000L)
        assertEquals(1, alerts.size)
        assertEquals(AlertSeverity.WARNING, alerts[0].severity)
        assertTrue(alerts[0].message.contains("Left Foot"))
    }

    @Test
    fun `latches alert for 10 seconds even when foot lifts to zero during swing phase`() {
        val spikeData = createData(leftPressure = 2050)
        useCase(spikeData, currentTimeMs = 1000L)
        useCase(spikeData, currentTimeMs = 1200L)
        useCase(spikeData, currentTimeMs = 1400L) // triggers, latched until 11400L

        // Foot in air (0 kPa swing) 2 seconds later
        val swingData = createData(leftPressure = 0)
        val alerts = useCase(swingData, currentTimeMs = 3400L)
        assertEquals("Alert must stay latched during swing phase", 1, alerts.size)

        // Still latched at 9 seconds later (10400L < 11400L)
        val alertsStillLatched = useCase(swingData, currentTimeMs = 10400L)
        assertEquals(1, alertsStillLatched.size)

        // After 10-second latch expires (11500L > 11400L) and pressure < 1800 kPa, alert clears
        val alertsExpired = useCase(swingData, currentTimeMs = 11500L)
        assertTrue("Alert should clear after 10s latch and low pressure", alertsExpired.isEmpty())
    }

    @Test
    fun `boundary hover between 1900 and 1950 kPa after latch expires does NOT re-trigger`() {
        val spikeData = createData(leftPressure = 2050)
        useCase(spikeData, currentTimeMs = 1000L)
        useCase(spikeData, currentTimeMs = 1200L)
        useCase(spikeData, currentTimeMs = 1400L) // triggered

        // 12 seconds later, pressure is hovering at 1900 kPa (< 2000 kPa warning threshold)
        val hoverData = createData(leftPressure = 1900)
        val alerts = useCase(hoverData, currentTimeMs = 13000L)
        assertTrue("Hovering at 1900 kPa must not re-trigger alert", alerts.isEmpty())
    }

    @Test
    fun `sustained temperature asymmetry of 2_4 C triggers thermal alert`() {
        val tempAlertData = createData(leftTemp = 35.0f, rightTemp = 32.6f) // diff = 2.4 C
        useCase(tempAlertData, currentTimeMs = 1000L)
        useCase(tempAlertData, currentTimeMs = 1200L)
        val alerts = useCase(tempAlertData, currentTimeMs = 1400L)

        assertEquals(1, alerts.size)
        assertTrue(alerts[0].message.contains("temperature asymmetry"))
    }

    @Test
    fun `reset clears all latches and counters`() {
        val spikeData = createData(leftPressure = 2050)
        useCase(spikeData, currentTimeMs = 1000L)
        useCase(spikeData, currentTimeMs = 1200L)
        useCase(spikeData, currentTimeMs = 1400L) // triggered

        useCase.reset()

        val swingData = createData(leftPressure = 0)
        val alerts = useCase(swingData, currentTimeMs = 2000L)
        assertTrue("Reset must clear active latched alerts", alerts.isEmpty())
    }
}
