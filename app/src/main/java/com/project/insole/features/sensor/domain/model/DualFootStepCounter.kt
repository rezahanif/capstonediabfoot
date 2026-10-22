package com.project.insole.features.sensor.domain.model

class DualFootStepCounter(
    leftClock: Clock = SystemClock,
    rightClock: Clock = SystemClock
) {

    val leftFSM  = WalkingFSM(leftClock)
    val rightFSM = WalkingFSM(rightClock)

    /** Combined total = left steps + right steps. */
    val totalSteps: Int
        get() = leftFSM.stepCount + rightFSM.stepCount

    val leftSteps: Int get() = leftFSM.stepCount
    val rightSteps: Int get() = rightFSM.stepCount

    /** Feed a pre-parsed packet from the LEFT insole. Returns updated total. */
    fun processLeft(packet: SensorPacket): Int {
        leftFSM.process(packet)
        return totalSteps
    }

    /** Feed a pre-parsed packet from the RIGHT insole. Returns updated total. */
    fun processRight(packet: SensorPacket): Int {
        rightFSM.process(packet)
        return totalSteps
    }

    /** Convenience helper to process raw string. */
    fun processBleString(raw: String, isLeft: Boolean): Int? {
        val packet = SensorPacket.fromBleString(raw) ?: return null
        return if (isLeft) processLeft(packet) else processRight(packet)
    }

    /** WALKING > TRANSITION > STANDING — whichever foot is more active wins. */
    val dominantState: WalkState
        get() {
            val l = leftFSM.state
            val r = rightFSM.state
            return when {
                l == WalkState.WALKING    || r == WalkState.WALKING    -> WalkState.WALKING
                l == WalkState.TRANSITION || r == WalkState.TRANSITION -> WalkState.TRANSITION
                else                                                    -> WalkState.STANDING
            }
        }

    /**
     * Average accelMag across the feet actually streaming. Dividing by 2
     * when only one insole is connected would halve the reported motion
     * (the idle FSM reports 0), so the divisor is the number of feet with
     * live data — mirrors the temperature-average guard in aggregation.
     */
    fun combinedAccelMag(leftPresent: Boolean, rightPresent: Boolean): Float {
        val presentCount = (if (leftPresent) 1 else 0) + (if (rightPresent) 1 else 0)
        if (presentCount == 0) return 0f
        val sum = (if (leftPresent) leftFSM.accelMag else 0f) +
            (if (rightPresent) rightFSM.accelMag else 0f)
        return sum / presentCount
    }

    fun reset() {
        leftFSM.reset()
        rightFSM.reset()
    }

    fun setInitialSteps(total: Int, left: Int, right: Int) {
        reset()
        leftFSM.stepCount = left
        rightFSM.stepCount = right
        // Guard against persisted splits that predate per-foot storage
        // (leftCount/rightCount were 0 while count > 0): attribute the
        // remainder to LEFT to keep total consistent with the DB row.
        val persisted = left + right
        if (persisted < total) leftFSM.stepCount = left + (total - persisted)
    }
}
