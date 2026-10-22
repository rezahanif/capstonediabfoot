package com.project.insole.features.sensor.domain.model

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * CHANGE from original: accept a [Clock] parameter (default = [SystemClock]).
 * Every other line is identical to the original.
 *
 * This single constructor change is all that is needed to make the
 * entire FSM fully testable without Thread.sleep().
 */
class WalkingFSM(private val clock: Clock = SystemClock) {

    companion object {
        /**
         * Exit gate for the TRANSITION dead zone: mid-range motion
         * (MOTION_STOP ≤ accelMag ≤ MOTION_CONFIRM with low gyro) matched
         * neither the confirm nor the stop branch and would trap the FSM in
         * TRANSITION indefinitely. 2000 ms ≈ 10 packets at the 5 Hz rate.
         */
        const val TRANSITION_TIMEOUT_MS = 2000L
    }

    // ── EMA ──────────────────────────────────────────────────────────────────
    private val alpha = 0.2f
    private var filterInit = false

    private var axF = 0f; private var ayF = 0f; private var azF = 0f
    private var gxF = 0f; private var gyF = 0f; private var gzF = 0f

    // ── Motion features ───────────────────────────────────────────────────────
    var accelMag = 0f;  private set
    var gyroMag  = 0f;  private set

    private var previousTotalAccel = 1.0f

    // ── FSM ───────────────────────────────────────────────────────────────────
    var state: WalkState = WalkState.STANDING;  private set

    private var walkingTimerMs  = 0L
    private var standingTimerMs = 0L

    // ── Step counter ──────────────────────────────────────────────────────────
    var stepCount = 0;  internal set

    private var stepTriggered = false

    // ─────────────────────────────────────────────────────────────────────────
    private fun ema(input: Float, prev: Float) = alpha * input + (1f - alpha) * prev

    fun process(packet: SensorPacket): Int {

        val ax = packet.accelX
        val ay = packet.accelY
        val az = packet.accelZ
        val gx = packet.gyroX
        val gy = packet.gyroY
        val gz = packet.gyroZ

        // ── EMA filter ────────────────────────────────────────────────────────
        if (!filterInit) {
            axF = ax; ayF = ay; azF = az
            gxF = gx; gyF = gy; gzF = gz
            previousTotalAccel = sqrt(ax * ax + ay * ay + az * az)
            filterInit = true
        } else {
            axF = ema(ax, axF); ayF = ema(ay, ayF); azF = ema(az, azF)
            gxF = ema(gx, gxF); gyF = ema(gy, gyF); gzF = ema(gz, gzF)
        }

        // ── Combined motion energy ───────────────────────────────────────────
        val totalAccel     = sqrt(axF * axF + ayF * ayF + azF * azF)
        val gravityRemoved = abs(totalAccel - 1.0f)
        val derivative     = abs(totalAccel - previousTotalAccel)
        previousTotalAccel = totalAccel

        accelMag = 0.7f * gravityRemoved + 0.3f * derivative
        gyroMag  = sqrt(gxF * gxF + gyF * gyF + gzF * gzF)

        // ── FSM ───────────────────────────────────────────────────────────────
        val now = clock.currentTimeMillis()
        when (state) {
            WalkState.STANDING -> {
                if (accelMag > SensorConstants.MOTION_START_THRESHOLD || gyroMag > 15.0f) {
                    state = WalkState.TRANSITION
                    walkingTimerMs = now
                }
            }
            WalkState.TRANSITION -> when {
                accelMag > SensorConstants.MOTION_CONFIRM_THRESHOLD && (now - walkingTimerMs > 100L) ->
                    state = WalkState.WALKING
                accelMag < SensorConstants.MOTION_STOP_THRESHOLD && gyroMag < 10.0f ->
                    state = WalkState.STANDING
                now - walkingTimerMs > TRANSITION_TIMEOUT_MS ->
                    state = WalkState.STANDING
            }
            WalkState.WALKING -> {
                if (accelMag < SensorConstants.MOTION_STOP_THRESHOLD && gyroMag < 10.0f) {
                    if (standingTimerMs == 0L) standingTimerMs = now
                    if (now - standingTimerMs > 1500L) {
                        state           = WalkState.STANDING
                        standingTimerMs = 0L
                    }
                } else {
                    standingTimerMs = 0L
                }
            }
        }

        // ── Step detection ────────────────────────────────────────────────────
        if (state == WalkState.WALKING || state == WalkState.TRANSITION) {
            if (accelMag > SensorConstants.STEP_THRESHOLD && !stepTriggered) {
                stepTriggered = true
                stepCount++
            }
            if (accelMag < SensorConstants.MOTION_STOP_THRESHOLD) {
                stepTriggered = false
            }
        } else {
            stepTriggered = false
        }

        return stepCount
    }

    /**
     * Forces the FSM back to STANDING when the packet stream stalls (BLE
     * drop, device switched off). The 1500 ms inactivity timer only runs
     * inside [process], so without this the FSM would report WALKING forever
     * after the stream stops. Step count is preserved — only the motion
     * state is reset.
     */
    fun onStreamStalled() {
        state              = WalkState.STANDING
        stepTriggered      = false
        standingTimerMs    = 0L
        walkingTimerMs     = 0L
    }

    fun reset() {
        stepCount          = 0
        stepTriggered      = false
        state              = WalkState.STANDING
        filterInit         = false
        previousTotalAccel = 1.0f
        standingTimerMs    = 0L
        walkingTimerMs     = 0L
    }
}
