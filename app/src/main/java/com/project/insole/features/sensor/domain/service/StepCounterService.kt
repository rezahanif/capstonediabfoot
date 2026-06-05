package com.project.insole.features.sensor.domain.service

import com.project.insole.features.sensor.domain.model.DualFootStepCounter
import com.project.insole.features.sensor.domain.model.SensorPacket
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain-level service that maintains the state of the step counter.
 */
@Singleton
class StepCounterService @Inject constructor() {
    private val counter = DualFootStepCounter()

    fun processPacket(packet: SensorPacket, isLeft: Boolean): Int {
        return if (isLeft) {
            counter.processLeft(packet)
        } else {
            counter.processRight(packet)
        }
    }

    val totalSteps: Int get() = counter.totalSteps
    val walkState get() = counter.dominantState
    val combinedAccelMag get() = counter.combinedAccelMag

    fun reset() {
        counter.reset()
    }
}
