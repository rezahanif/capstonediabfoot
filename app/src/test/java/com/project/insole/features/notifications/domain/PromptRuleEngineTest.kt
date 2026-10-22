package com.project.insole.features.notifications.domain

import com.project.insole.features.sensor.domain.model.FakeClock
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Domain rule engine tests (TD-8). Pure JVM with [FakeClock] — no Android
 * time, no coroutines: every gate (persistence, cooldown, config version)
 * is deterministic.
 */
class PromptRuleEngineTest {

    private lateinit var clock: FakeClock
    private lateinit var engine: PromptRuleEngine

    @Before
    fun setUp() {
        clock = FakeClock(startTime = 1_000_000L)
        engine = PromptRuleEngine(clock = clock)
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private fun input(
        leftConnected: Boolean = true,
        rightConnected: Boolean = true,
        leftTemp: Float = 31.0f,
        rightTemp: Float = 31.0f,
        leftPressure: Int = 0,
        rightPressure: Int = 0
    ) = RuleInput(
        leftConnected = leftConnected,
        rightConnected = rightConnected,
        sensorData = InsoleSensorData(
            pressureValues = listOf(leftPressure, rightPressure),
            temperature = if (leftTemp > 0f && rightTemp > 0f) (leftTemp + rightTemp) / 2f
            else maxOf(leftTemp, rightTemp),
            leftTemperature = leftTemp,
            rightTemperature = rightTemp,
            leftPressure = leftPressure,
            rightPressure = rightPressure,
            stepCount = 0,
            walkState = com.project.insole.features.sensor.domain.model.WalkState.STANDING,
            combinedAccelMag = 0f,
            batteryLevel = 100,
            timestamp = 0L
        )
    )

    private fun warm(foot: FootSide, delta: Float = 2.5f) = if (foot == FootSide.LEFT) {
        input(leftTemp = 31.0f + delta, rightTemp = 31.0f)
    } else {
        input(leftTemp = 31.0f, rightTemp = 31.0f + delta)
    }

    // ── no-data ─────────────────────────────────────────────────────────────

    @Test
    fun `a disconnected foot yields NO_DATA, never ALL_CLEAR`() {
        val result = engine.evaluate(input(leftConnected = false))

        assertEquals(OverallStatus.NO_DATA, result.overallStatus)
        assertEquals(1, result.prompts.size)
        assertEquals("NO_DATA", result.prompts[0].id)
    }

    @Test
    fun `missing sensor data yields NO_DATA even when both feet are connected`() {
        val result = engine.evaluate(
            RuleInput(leftConnected = true, rightConnected = true, sensorData = null)
        )

        assertEquals(OverallStatus.NO_DATA, result.overallStatus)
    }

    // ── thermal rule: persistence gate ──────────────────────────────────────

    @Test
    fun `a single abnormal thermal reading never prompts`() {
        val result = engine.evaluate(warm(FootSide.LEFT))

        assertTrue(result.prompts.isEmpty())
        assertEquals(OverallStatus.ALL_CLEAR, result.overallStatus)
    }

    @Test
    fun `thermal prompt fires after the configured persistence count`() {
        var result = engine.evaluate(warm(FootSide.LEFT))
        result = engine.evaluate(warm(FootSide.LEFT))
        assertTrue("2 consecutive must not prompt", result.prompts.isEmpty())

        result = engine.evaluate(warm(FootSide.LEFT)) // 3rd consecutive

        assertEquals(1, result.prompts.size)
        val prompt = result.prompts[0]
        assertEquals("THERMAL_LEFT", prompt.id)
        assertEquals(FootSide.LEFT, prompt.foot)
        assertEquals(PromptSeverity.ATTENTION, prompt.severity)
        assertEquals("v1", prompt.thresholdVersion)
        assertEquals(OverallStatus.ATTENTION, result.overallStatus)
    }

    @Test
    fun `thermal persistence is consecutive - a quiet reading resets the count`() {
        engine.evaluate(warm(FootSide.LEFT))
        engine.evaluate(warm(FootSide.LEFT))
        engine.evaluate(input()) // quiet — count resets

        var result = engine.evaluate(warm(FootSide.LEFT))
        result = engine.evaluate(warm(FootSide.LEFT))
        assertTrue("only 2 consecutive since reset", result.prompts.isEmpty())

        result = engine.evaluate(warm(FootSide.LEFT))
        assertEquals(1, result.prompts.size)
    }

    @Test
    fun `flipping the warmer foot restarts persistence`() {
        engine.evaluate(warm(FootSide.LEFT))
        engine.evaluate(warm(FootSide.LEFT))

        // Right becomes the warmer foot — LEFT's counter must not carry over.
        var result = engine.evaluate(warm(FootSide.RIGHT))
        result = engine.evaluate(warm(FootSide.RIGHT))
        assertTrue(result.prompts.isEmpty())

        result = engine.evaluate(warm(FootSide.RIGHT))
        assertEquals("THERMAL_RIGHT", result.prompts[0].id)
    }

    @Test
    fun `invalid temperature on either foot skips the thermal rule`() {
        var result = engine.evaluate(input(leftTemp = 0f, rightTemp = 40f))
        assertTrue(result.prompts.isEmpty())

        result = engine.evaluate(warm(FootSide.LEFT)) // still only 1 valid reading
        result = engine.evaluate(warm(FootSide.LEFT))
        assertTrue(result.prompts.isEmpty())
    }

    // ── thermal cooldown ────────────────────────────────────────────────────

    @Test
    fun `thermal prompt does not repeat within the cooldown window`() {
        repeat(3) { engine.evaluate(warm(FootSide.LEFT)) }
        // Prompt fired on the 3rd. Still warm afterwards → no duplicate.
        var result = engine.evaluate(warm(FootSide.LEFT))
        assertTrue(result.prompts.isEmpty())

        clock.advanceBy(59L * 60_000L) // 59 of 60 minutes
        result = engine.evaluate(warm(FootSide.LEFT))
        assertTrue("cooldown not yet elapsed", result.prompts.isEmpty())

        clock.advanceBy(60L * 1000L) // past the 60-minute cooldown
        result = engine.evaluate(warm(FootSide.LEFT))
        assertEquals(1, result.prompts.size)
    }

    // ── pressure rule ───────────────────────────────────────────────────────

    @Test
    fun `pressure prompt fires immediately at threshold then respects cooldown`() {
        var result = engine.evaluate(input(leftPressure = 200))
        assertEquals(1, result.prompts.size)
        assertEquals("PRESSURE_LEFT", result.prompts[0].id)
        assertEquals(FootSide.LEFT, result.prompts[0].foot)

        result = engine.evaluate(input(leftPressure = 200))
        assertTrue("within cooldown", result.prompts.isEmpty())

        clock.advanceBy(30L * 60_000L)
        result = engine.evaluate(input(leftPressure = 200))
        assertEquals(1, result.prompts.size)
    }

    @Test
    fun `pressure prompts are independent per foot`() {
        engine.evaluate(input(leftPressure = 200))

        val result = engine.evaluate(input(leftPressure = 200, rightPressure = 210))
        assertEquals(1, result.prompts.size)
        assertEquals("PRESSURE_RIGHT", result.prompts[0].id)
    }

    // ── config-driven behaviour (TD-8: thresholds are configuration) ───────

    @Test
    fun `a new config changes behaviour and resets the bookkeeping`() {
        engine.evaluate(warm(FootSide.LEFT))
        engine.evaluate(warm(FootSide.LEFT))

        // New regime: persistence drops to 1 and the version changes.
        val v2 = ThresholdsConfig(
            version = "v2-clinical-review",
            thermalDeltaWarningC = 2.2f,
            thermalPersistenceCount = 1,
            thermalCooldownMinutes = 60,
            pressureWarning = 200,
            pressureCooldownMinutes = 30
        )
        engine.updateConfig(v2)

        val result = engine.evaluate(warm(FootSide.LEFT))

        assertEquals(1, result.prompts.size)
        assertEquals("v2-clinical-review", result.prompts[0].thresholdVersion)
    }

    @Test
    fun `every prompt carries the active config version`() {
        val result = engine.evaluate(input(leftPressure = 250))
        assertEquals("v1", result.prompts[0].thresholdVersion)
    }
}
