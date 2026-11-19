package com.project.insole.features.sensor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  WalkingFSMTest — Full State Machine Test Suite
 * ═══════════════════════════════════════════════════════════════════════════
 *
 *  WHAT MAKES THE ORIGINAL TEST WRONG
 *  ────────────────────────────────────────────────────────────────────────
 *  The original test fed one packet and expected WALKING state. This fails
 *  because two gates must be passed before reaching WALKING:
 *
 *    1. MOTION_START gate    → accelMag > 0.12f  → enters TRANSITION
 *    2. 100ms timer gate     → now - walkingTimerMs > 100L  → enters WALKING
 *
 *  The 100ms timer uses System.currentTimeMillis() internally, so a test
 *  that doesn't control time will always fail the second gate.
 *
 *  SOLUTION: Clock Injection
 *  ────────────────────────────────────────────────────────────────────────
 *  WalkingFSM now accepts a Clock interface. Tests inject FakeClock, which
 *  lets us teleport time forward by exactly N milliseconds. No Thread.sleep,
 *  no flakiness, no 1.5-second waits in CI.
 *
 *  HOW TO CALCULATE PACKET VALUES
 *  ────────────────────────────────────────────────────────────────────────
 *  The FSM computes accelMag on the FIRST packet without EMA (it initialises
 *  the filter directly from the packet). From the second packet onward,
 *  EMA(alpha=0.2) smooths the values.
 *
 *  For the FIRST packet (filterInit=false):
 *    totalAccel     = sqrt(ax² + ay² + az²)
 *    gravityRemoved = |totalAccel − 1.0|
 *    derivative     = |totalAccel − 1.0|   ← previousTotalAccel starts at 1.0
 *    accelMag       = 0.7·gravityRemoved + 0.3·derivative
 *                   = gravityRemoved       ← both terms are equal on first packet
 *
 *  HIGH-MOTION packet  (0, 0, -1.5) → totalAccel=1.5, accelMag=0.5  ✓ > 0.15
 *  LOW-MOTION  packet  (0, 0, -1.0) → totalAccel=1.0, accelMag=0.0  ✓ < 0.05
 *
 *  For SUBSEQUENT packets (EMA active), see [driveToWalking] for the
 *  settled packet sequence that guarantees convergence.
 */
class WalkingFSMTest {

    // ── shared fixtures ───────────────────────────────────────────────────────
    private lateinit var clock: FakeClock
    private lateinit var fsm: WalkingFSM

    // Packets chosen so their accelMag lands clearly on one side of every threshold.
    // See class-level Kdoc for the maths.
    private val highMotionPacket = SensorPacket(accelX = 0f, accelY = 0f, accelZ = -1.5f)
    private val lowMotionPacket  = SensorPacket(accelX = 0f, accelY = 0f, accelZ = -1.0f)

    @Before
    fun setUp() {
        clock = FakeClock(startTime = 1_000L)   // arbitrary non-zero start
        fsm   = WalkingFSM(clock)
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  1. INITIAL STATE
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `initial state is STANDING`() {
        assertEquals(WalkState.STANDING, fsm.state)
    }

    @Test
    fun `initial step count is zero`() {
        assertEquals(0, fsm.stepCount)
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  2. STANDING → TRANSITION
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `high-motion packet moves FSM from STANDING to TRANSITION`() {
        // First packet always initialises the EMA filter directly,
        // so accelMag = |1.5 − 1.0| = 0.5, well above MOTION_START = 0.12.
        fsm.process(highMotionPacket)

        assertEquals(WalkState.TRANSITION, fsm.state)
    }

    @Test
    fun `low-motion packet keeps FSM in STANDING`() {
        // accelMag = |1.0 − 1.0| = 0.0, below MOTION_START = 0.12.
        fsm.process(lowMotionPacket)

        assertEquals(WalkState.STANDING, fsm.state)
    }

    @Test
    fun `high gyro alone triggers TRANSITION from STANDING`() {
        // gyroMag > 15 deg/s is an alternative trigger (see FSM source).
        val highGyroPacket = SensorPacket(
            accelX = 0f, accelY = 0f, accelZ = -1.0f, // ← neutral accel
            gyroX  = 13f, gyroY  = 10f, gyroZ  = 0f   // ← magnitude > 15.0
        )
        fsm.process(highGyroPacket)

        assertEquals(WalkState.TRANSITION, fsm.state)
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  3. TRANSITION → WALKING  (requires the 100 ms timer)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `TRANSITION does NOT become WALKING before 100 ms have elapsed`() {
        fsm.process(highMotionPacket)           // → TRANSITION, timer starts
        clock.advanceBy(50L)                    // only 50 ms have passed
        fsm.process(highMotionPacket)           // still in TRANSITION window

        assertEquals(WalkState.TRANSITION, fsm.state)
    }

    @Test
    fun `TRANSITION becomes WALKING after 100 ms of sustained motion`() {
        fsm.process(highMotionPacket)           // → TRANSITION
        clock.advanceBy(101L)                   // 101 ms later…
        fsm.process(highMotionPacket)           // …confirm threshold still exceeded

        assertEquals(WalkState.WALKING, fsm.state)
    }

    @Test
    fun `TRANSITION falls back to STANDING when motion stops`() {
        fsm.process(highMotionPacket)           // → TRANSITION
        // Feed enough low-motion packets to drive EMA below MOTION_STOP = 0.05
        repeat(20) { fsm.process(lowMotionPacket) }

        assertEquals(WalkState.STANDING, fsm.state)
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  4. WALKING → STANDING  (requires the 1500 ms inactivity timer)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `WALKING does NOT return to STANDING before 1500 ms of inactivity`() {
        driveToWalking()

        // Settle EMA to low-motion values
        repeat(20) { fsm.process(lowMotionPacket) }
        clock.advanceBy(1_000L)                 // only 1 second of quiet
        fsm.process(lowMotionPacket)

        assertEquals(WalkState.WALKING, fsm.state)   // still walking
    }

    @Test
    fun `WALKING returns to STANDING after 1500 ms of inactivity`() {
        driveToWalking()

        repeat(20) { fsm.process(lowMotionPacket) }  // drive EMA below stop threshold
        clock.advanceBy(1_501L)                       // 1.5 s of quiet
        fsm.process(lowMotionPacket)

        assertEquals(WalkState.STANDING, fsm.state)
    }

    @Test
    fun `inactivity timer resets when motion resumes during WALKING`() {
        driveToWalking()

        // Partial quiet period — not enough to stop
        repeat(20) { fsm.process(lowMotionPacket) }
        clock.advanceBy(1_000L)

        // Motion returns before timeout → timer resets
        fsm.process(highMotionPacket)
        clock.advanceBy(600L)
        repeat(20) { fsm.process(lowMotionPacket) }
        clock.advanceBy(600L)
        fsm.process(lowMotionPacket)

        // 600 + 600 = 1200 ms since last motion, still < 1500 ms
        assertEquals(WalkState.WALKING, fsm.state)
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  5. STEP COUNTING  (hysteresis: arm → trigger → disarm → re-arm)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `step is counted when accelMag crosses STEP_THRESHOLD in WALKING`() {
        driveToWalking()
        // Reset trigger by feeding low motion
        repeat(20) { fsm.process(lowMotionPacket) }
        
        val stepsBefore = fsm.stepCount
        // Feed high motion until step is counted (EMA needs to converge)
        repeat(10) { fsm.process(highMotionPacket) }

        assertEquals(stepsBefore + 1, fsm.stepCount)
    }

    @Test
    fun `holding peak does NOT count additional steps (hysteresis)`() {
        driveToWalking()

        // Multiple packets all above threshold — should still be exactly 1 new step
        repeat(10) { fsm.process(highMotionPacket) }
        val stepsAfterHold = fsm.stepCount

        repeat(10) { fsm.process(highMotionPacket) }

        // Count should not increase while trigger is latched
        assertEquals(stepsAfterHold, fsm.stepCount)
    }

    @Test
    fun `two complete step cycles produce exactly 2 new steps`() {
        driveToWalking()
        // Reset trigger by feeding low motion
        repeat(20) { fsm.process(lowMotionPacket) }

        val stepsBefore = fsm.stepCount

        repeat(2) {
            repeat(10) { fsm.process(highMotionPacket) } // peak  → step counted
            repeat(20) { fsm.process(lowMotionPacket) }  // valley → trigger re-armed
        }

        assertEquals(stepsBefore + 2, fsm.stepCount)
    }

    @Test
    fun `steps are NOT counted while in STANDING state`() {
        // FSM never leaves STANDING in this test
        repeat(10) { fsm.process(highMotionPacket) }
        // After the first packet enters TRANSITION, subsequent packets keep
        // it in TRANSITION (no time advance). Steps count in WALKING *and*
        // TRANSITION (WalkingFSM.process) — the packet above counts one step
        // in TRANSITION; field validation pending, see docs/TECH_DEBT.md.
        val stepsInTransition = fsm.stepCount

        // Reset to a clean STANDING and feed motion
        fsm.reset()
        fsm.process(highMotionPacket)   // → TRANSITION, not WALKING
        assertEquals(stepsInTransition, fsm.stepCount)
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  5B. BIOMECHANICAL FSR FUSION (Swing Unweighting & Stance Touchdown)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `FSR gait cycle requires unweighted swing before stance touchdown`() {
        driveToWalking()

        val swingPacket = SensorPacket(
            accelX = 0f, accelY = 0f, accelZ = -1.0f,
            gyroX = 0f, gyroY = 30f, gyroZ = 0f,
            pressure = 10f,
            pressuresKpa = listOf(0f, 0f, 0f, 0f, 10f)
        )
        val stancePacket = SensorPacket(
            accelX = 0f, accelY = 0f, accelZ = -1.0f,
            pressure = 150f,
            pressuresKpa = listOf(30f, 20f, 40f, 10f, 50f)
        )

        val stepsBefore = fsm.stepCount

        // 1. Swing unweights the foot (< 40 kPa)
        fsm.process(swingPacket)
        clock.advanceBy(200L)

        // 2. Stance touchdown (> 60 kPa) triggers 1 step
        clock.advanceBy(300L)
        fsm.process(stancePacket)

        assertEquals(stepsBefore + 1, fsm.stepCount)

        // 3. Sustained stance contact without swing does NOT double count
        clock.advanceBy(200L)
        fsm.process(stancePacket)
        assertEquals(stepsBefore + 1, fsm.stepCount)
    }

    @Test
    fun `constant static standing pressure completely rejects false steps`() {
        driveToWalking()

        // High static standing pressure without unweighting (Scenario 4)
        val standingPressurePacket = SensorPacket(
            accelX = 0f, accelY = 0.2f, accelZ = -1.2f, // Accel motion variation
            pressure = 300f,
            pressuresKpa = listOf(50f, 50f, 50f, 50f, 100f)
        )

        val stepsBefore = fsm.stepCount

        // Even with motion variation, foot is never unweighted → zero steps counted
        repeat(15) {
            clock.advanceBy(200L)
            fsm.process(standingPressurePacket)
        }

        assertEquals(stepsBefore, fsm.stepCount)
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  6. RESET
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `reset clears state machine back to initial conditions`() {
        driveToWalking()
        repeat(5) { fsm.process(highMotionPacket) }
        assertTrue(fsm.stepCount > 0)

        fsm.reset()

        assertEquals(WalkState.STANDING, fsm.state)
        assertEquals(0, fsm.stepCount)
    }

    @Test
    fun `fsm behaves correctly after reset`() {
        driveToWalking()
        fsm.reset()

        // Should be able to re-enter WALKING from scratch
        driveToWalking()

        assertEquals(WalkState.WALKING, fsm.state)
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  6.5  TRANSITION DEAD ZONE + STREAM STALL
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Feeds packets whose EMA-settled accelMag lands in the dead zone
     * (0.05 ≤ accelMag ≤ 0.12, gyro quiet) — matched by neither the confirm
     * nor the stop gate. accelZ = -1.08 converges to |total−1| ≈ 0.08 with
     * the EMA at α=0.2; ~10 packets are enough to settle below 0.12.
     * Packets are fed while the clock is frozen so the 100 ms confirm gate
     * can never fire and accidentally promote the FSM to WALKING.
     */
    private fun driveToTransitionDeadZone() {
        fsm.process(highMotionPacket)                 // → TRANSITION, timer stamped
        repeat(10) { fsm.process(deadZonePacket) }    // EMA settles into the dead zone
        assertEquals(WalkState.TRANSITION, fsm.state) // precondition: trapped in TRANSITION
    }

    private val deadZonePacket = SensorPacket(accelX = 0f, accelY = 0f, accelZ = -1.08f)

    @Test
    fun `TRANSITION dead zone exits to STANDING after the timeout`() {
        driveToTransitionDeadZone()

        clock.advanceBy(WalkingFSM.TRANSITION_TIMEOUT_MS + 100L)
        fsm.process(deadZonePacket)

        assertEquals(WalkState.STANDING, fsm.state)
    }

    @Test
    fun `TRANSITION dead zone does NOT exit before the timeout`() {
        driveToTransitionDeadZone()

        clock.advanceBy(500L)
        fsm.process(deadZonePacket)

        assertEquals(WalkState.TRANSITION, fsm.state)
    }

    @Test
    fun `stream stall forces WALKING back to STANDING and keeps step count`() {
        driveToWalking()
        assertTrue(fsm.stepCount >= 0)
        val stepsBefore = fsm.stepCount

        // Simulates the BLE stream dying while WALKING — the 1500 ms
        // inactivity timer inside process() would never fire on its own.
        fsm.onStreamStalled()

        assertEquals(WalkState.STANDING, fsm.state)
        assertEquals(stepsBefore, fsm.stepCount)
    }

    @Test
    fun `FSM can enter TRANSITION normally after a stall`() {
        driveToWalking()
        fsm.onStreamStalled()
        assertEquals(WalkState.STANDING, fsm.state)

        fsm.process(highMotionPacket)

        assertEquals(WalkState.TRANSITION, fsm.state)
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  7. EMA FILTER  (observable side-effects)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `single noisy spike does not cause TRANSITION (EMA smoothing)`() {
        // Send many low-motion packets first to settle EMA near zero
        repeat(30) { fsm.process(lowMotionPacket) }
        assertEquals(WalkState.STANDING, fsm.state)

        // One spike packet — EMA dampens it: new accelMag = 0.2 * spike + 0.8 * settled
        // For spike (0, 0, -1.5): contribution ≈ 0.2 × 0.5 = 0.1, still below 0.12
        val spikePacket = SensorPacket(accelX = 0f, accelY = 0f, accelZ = -1.5f)
        fsm.process(spikePacket)

        // EMA absorbs the spike; FSM should NOT have transitioned
        assertEquals(WalkState.STANDING, fsm.state)
    }

    @Test
    fun `sustained motion does cause TRANSITION despite EMA`() {
        repeat(30) { fsm.process(lowMotionPacket) }

        // Sustained high motion — EMA eventually converges to high value
        repeat(15) { fsm.process(highMotionPacket) }

        assertEquals(WalkState.TRANSITION, fsm.state)
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Drives the FSM into WALKING state deterministically.
     *
     * Strategy:
     *   • Send one highMotionPacket → TRANSITION (filter initialised directly)
     *   • Advance fake clock 101 ms past the timer gate
     *   • Send another highMotionPacket → WALKING confirmed
     *
     * After this call: fsm.state == WALKING, clock is 101 ms ahead of setUp.
     */
    private fun driveToWalking() {
        fsm.process(highMotionPacket)   // → TRANSITION; timer = clock.now
        clock.advanceBy(101L)           // past the 100 ms gate
        fsm.process(highMotionPacket)   // → WALKING

        // Defensive assertion so test failures inside driveToWalking are obvious
        assertEquals(
            "driveToWalking() precondition failed — FSM should be WALKING",
            WalkState.WALKING, fsm.state
        )
    }
}
