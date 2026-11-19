package com.project.insole.features.sensor.domain.model

import com.project.insole.features.notifications.domain.FootSide
import com.project.insole.features.notifications.domain.PromptRuleEngine
import com.project.insole.features.notifications.domain.PromptSeverity
import com.project.insole.features.notifications.domain.RuleInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalRiskScoringTest {

    @Test
    fun riskScoringFunction_truthTableEvaluation() {
        // 1. thermalexceed = 1, pressureexceed = 1 -> alert = high (valid swell)
        val (risk1, swell1) = ClinicalScoringVariables.calculateRisk(thermalExceed = 1, pressureExceed = 1)
        assertEquals(RiskLevel.HIGH, risk1)
        assertTrue("Valid swell must be flagged when both thermal and pressure exceed", swell1)

        // 2. thermalexceed = 1, pressureexceed = 0 -> alert = high
        val (risk2, swell2) = ClinicalScoringVariables.calculateRisk(thermalExceed = 1, pressureExceed = 0)
        assertEquals(RiskLevel.HIGH, risk2)
        assertFalse(swell2)

        // 3. thermalexceed = 0, pressureexceed = 1 -> alert = medium
        val (risk3, swell3) = ClinicalScoringVariables.calculateRisk(thermalExceed = 0, pressureExceed = 1)
        assertEquals(RiskLevel.MEDIUM, risk3)
        assertFalse(swell3)

        // 4. thermalexceed = 0, pressureexceed = 0 -> alert = low
        val (risk4, swell4) = ClinicalScoringVariables.calculateRisk(thermalExceed = 0, pressureExceed = 0)
        assertEquals(RiskLevel.LOW, risk4)
        assertFalse(swell4)
    }

    @Test
    fun promptRuleEngine_attachesRiskAndValidSwellToPrompts() {
        val clock = FakeClock(startTime = 1_000_000L)
        val engine = PromptRuleEngine(clock = clock)

        // High risk valid swell (thermal exceed + pressure exceed)
        val validSwellClinical = ClinicalScoringVariables.EMPTY.copy(
            consecutiveThermalDays = 2,
            sustainedThermalSide = FootSide.LEFT,
            isSustainedHotspot2Day = true,
            cleanThermalAsymmetryC = 2.6f,
            thermalExceed = 1,
            pressureExceed = 1,
            isValidSwell = true,
            riskLevel = RiskLevel.HIGH
        )

        val dummyData = InsoleSensorData(
            pressureValues = listOf(2200, 500),
            temperature = 33.0f,
            leftTemperature = 34.0f,
            rightTemperature = 31.4f,
            leftPressure = 2200,
            rightPressure = 500,
            stepCount = 0,
            walkState = WalkState.STANDING,
            combinedAccelMag = 0f,
            batteryLevel = 100,
            timestamp = 0L
        )

        val input = RuleInput(
            leftConnected = true,
            rightConnected = true,
            sensorData = dummyData,
            clinicalVariables = validSwellClinical
        )

        val evaluation = engine.evaluate(input)
        val thermalPrompt = evaluation.prompts.find { it.id == "THERMAL_LEFT" }

        assertTrue(thermalPrompt != null)
        assertEquals(PromptSeverity.ATTENTION, thermalPrompt?.severity)
        assertEquals(RiskLevel.HIGH, thermalPrompt?.riskLevel)
        assertTrue("Valid swell must be attached to prompt", thermalPrompt?.isValidSwell == true)
    }
}
