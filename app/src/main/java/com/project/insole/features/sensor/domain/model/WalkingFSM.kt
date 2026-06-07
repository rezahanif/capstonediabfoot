package com.project.insole.features.sensor.domain.model

import kotlin.math.abs
import kotlin.math.sqrt

class WalkingFSM {

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

        // ── Convert m/s² → g (thresholds were tuned in g-units) ──────────────
        val ax = packet.accelX * 9.81f
        val ay = packet.accelY * 9.81f
        val az = packet.accelZ * 9.81f
        val gx = packet.gyroX
        val gy = packet.gyroY
        val gz = packet.gyroZ

        // ── EMA filter ────────────────────────────────────────────────────────
        if (!filterInit) {
            axF = ax; ayF = ay; azF = az
            gxF = gx; gyF = gy; gzF = gz
            previousTotalAccel = sqrt(ax * ax + ay * ay + az * az) // Initialize to real gravity
            filterInit = true
        } else {
            axF = ema(ax, axF); ayF = ema(ay, ayF); azF = ema(az, azF)
            gxF = ema(gx, gxF); gyF = ema(gy, gyF); gzF = ema(gz, gzF)
        }

        // ── Combined motion energy (identical to firmware) ─────────────────────
        val totalAccel     = sqrt(axF * axF + ayF * ayF + azF * azF)
        val gravityRemoved = abs(totalAccel - 1.0f)
        val derivative     = abs(totalAccel - previousTotalAccel)
        previousTotalAccel = totalAccel

        accelMag = 0.7f * gravityRemoved + 0.3f * derivative
        gyroMag  = sqrt(gxF * gxF + gyF * gyF + gzF * gzF)

        // LOG FOR DEBUGGING
        // android.util.Log.d("FSM", "State: $state, AccelMag: $accelMag, stepTriggered: $stepTriggered")

        // ── FSM ───────────────────────────────────────────────────────────────
        val now = System.currentTimeMillis()
        when (state) {
            WalkState.STANDING -> {
                if (accelMag > SensorConstants.MOTION_START_THRESHOLD) {
                    state = WalkState.TRANSITION
                    walkingTimerMs = now
                }
            }
            WalkState.TRANSITION -> when {
                accelMag > SensorConstants.MOTION_CONFIRM_THRESHOLD && (now - walkingTimerMs > 100L) ->
                    state = WalkState.WALKING
                accelMag < 0.02f ->
                    state = WalkState.STANDING
            }
            WalkState.WALKING -> {
                if (accelMag < SensorConstants.MOTION_STOP_THRESHOLD) {
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
        // Allow step counting in TRANSITION too, to be more responsive
        if (state == WalkState.WALKING || state == WalkState.TRANSITION) {
            if (accelMag > SensorConstants.STEP_THRESHOLD && !stepTriggered) {
                stepTriggered = true
                stepCount++
            }
            // Hysteresis: Must drop below a lower value to re-arm the trigger
            if (accelMag < (SensorConstants.STEP_THRESHOLD * 0.5f)) {
                stepTriggered = false
            }
        } else {
            stepTriggered = false
        }

        return stepCount
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
