package com.project.insole.features.sensor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DualFootStepCounterTest {

    private val highMotionPacket = SensorPacket(accelX = 0f, accelY = 0f, accelZ = -1.5f)

    // FakeClock keeps the JVM tests off android.os.SystemClock (not mocked
    // in unit tests); the FSM never reads a real time value in these paths.
    private fun newCounter() = DualFootStepCounter(
        leftClock = FakeClock(),
        rightClock = FakeClock()
    )

    @Test
    fun `one-foot streaming does not halve combined accel magnitude`() {
        val counter = newCounter()
        counter.processLeft(highMotionPacket)

        val combined = counter.combinedAccelMag(leftPresent = true, rightPresent = false)

        assertEquals(counter.leftFSM.accelMag, combined, 1e-6f)
    }

    @Test
    fun `two-foot streaming averages combined accel magnitude`() {
        val counter = newCounter()
        counter.processLeft(highMotionPacket)
        counter.processRight(highMotionPacket)

        val combined = counter.combinedAccelMag(leftPresent = true, rightPresent = true)

        assertEquals((counter.leftFSM.accelMag + counter.rightFSM.accelMag) / 2f, combined, 1e-6f)
    }

    @Test
    fun `no feet streaming yields zero combined magnitude`() {
        val counter = newCounter()
        assertEquals(0f, counter.combinedAccelMag(leftPresent = false, rightPresent = false), 1e-6f)
    }

    @Test
    fun `dominant state picks the more active foot`() {
        val counter = newCounter()
        counter.processLeft(highMotionPacket) // left → TRANSITION

        assertEquals(WalkState.TRANSITION, counter.dominantState)
    }

    @Test
    fun `setInitialSteps restores both feet`() {
        val counter = newCounter()

        counter.setInitialSteps(total = 30, left = 12, right = 18)

        assertEquals(12, counter.leftSteps)
        assertEquals(18, counter.rightSteps)
        assertEquals(30, counter.totalSteps)
    }

    @Test
    fun `setInitialSteps keeps total consistent for legacy rows without per-foot split`() {
        val counter = newCounter()

        // Rows persisted before v3 have count > 0 but leftCount/rightCount = 0
        counter.setInitialSteps(total = 30, left = 0, right = 0)

        assertEquals(30, counter.totalSteps)
        assertEquals(30, counter.leftSteps) // remainder attributed to LEFT by design
    }

    @Test
    fun `total steps is the sum of both feet`() {
        val counter = newCounter()
        // First packet from each foot enters TRANSITION and counts one step
        // (steps count in WALKING and TRANSITION — see docs/TECH_DEBT.md).
        counter.processLeft(highMotionPacket)
        counter.processRight(highMotionPacket)

        assertTrue(counter.leftSteps >= 1)
        assertTrue(counter.rightSteps >= 1)
        assertEquals(counter.leftSteps + counter.rightSteps, counter.totalSteps)
    }
}
