package com.project.insole.features.sensor.domain.model

/**
 * Wear gating & clean data discrimination for diabetic foot monitoring.
 *
 * Implements clinical literature criteria (Armstrong et al., IWGDF):
 * - Clean On-Body Wear: Differentiates legitimate in-shoe wear from ambient noise,
 *   resting on table/floor, or floating ADC pins.
 * - Active Stance: Identifies weight-bearing samples (standing/walking stance)
 *   so hourly/daily pressure means reflect genuine mechanical stress rather than
 *   being diluted by swing-phase zeros.
 */
object WearGating {

    /**
     * Evaluates whether a sensor packet qualifies as clean on-body wear.
     * Guards against floating ADC pins and ambient off-shoe temperature noise.
     */
    fun isCleanOnBody(
        packet: SensorPacket,
        walkState: WalkState? = null
    ): Boolean {
        // Temperature check: physiological skin temperature
        val tempValid = isPhysiologicalSkinTemp(packet.temperature)

        // Contact check: at least 3 active FSRs (pin-glitch safe) or sufficient weight-bearing pressure
        val activeSensorCount = packet.pressuresKpa.count { it > SensorConstants.MIN_WEAR_PRESSURE_KPA }
        val contactValid = activeSensorCount >= SensorConstants.MIN_CLEAN_SENSORS_ACTIVE ||
                packet.pressure >= SensorConstants.MIN_STANCE_PRESSURE_KPA ||
                walkState == WalkState.WALKING || walkState == WalkState.STANDING || walkState == WalkState.TRANSITION

        // If packet carries temperature (heartbeat or v1 inline): require physiological temp OR active contact
        // If temperature is 0 (v2 stream packet awaiting heartbeat), rely on mechanical contact
        return if (packet.temperature > 0f) {
            tempValid && (contactValid || packet.pressure > 0)
        } else {
            contactValid
        }
    }

    /**
     * Checks if a temperature reading falls into the plausible human plantar skin temperature range.
     * Off-shoe room ambient (< 27.0°C), fever/burn (> 38.5°C), and -999 sentinel are rejected.
     */
    fun isPhysiologicalSkinTemp(tempC: Float): Boolean {
        return tempC != -999f && tempC in SensorConstants.MIN_PHYSIOLOGICAL_TEMP_C..SensorConstants.MAX_PHYSIOLOGICAL_TEMP_C
    }

    /**
     * Evaluates whether the current packet represents active stance (weight-bearing).
     * Used for computing true hourly stance mean pressure.
     */
    fun isActiveStance(
        packet: SensorPacket,
        walkState: WalkState? = null
    ): Boolean {
        val totalPressure = packet.pressure
        val peakPressure = packet.pressuresKpa.maxOrNull() ?: 0f
        return totalPressure >= SensorConstants.MIN_STANCE_PRESSURE_KPA ||
                peakPressure >= SensorConstants.MIN_STANCE_PRESSURE_KPA ||
                walkState == WalkState.STANDING ||
                (walkState == WalkState.WALKING && totalPressure > 10)
    }
}
