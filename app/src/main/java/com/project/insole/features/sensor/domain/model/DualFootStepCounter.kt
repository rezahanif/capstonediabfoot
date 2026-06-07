package com.project.insole.features.sensor.domain.model

class DualFootStepCounter {

    val leftFSM  = WalkingFSM()
    val rightFSM = WalkingFSM()

    /** Combined total = left steps + right steps. */
    val totalSteps: Int
        get() = leftFSM.stepCount + rightFSM.stepCount

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

    /** Average accelMag of both feet — used for the bar chart. */
    val combinedAccelMag: Float
        get() = (leftFSM.accelMag + rightFSM.accelMag) / 2f

    fun reset() {
        leftFSM.reset()
        rightFSM.reset()
    }

    fun setInitialSteps(steps: Int) {
        reset()
        leftFSM.stepCount = steps
    }
}
