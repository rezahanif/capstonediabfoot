package com.project.insole.features.sensor.domain.usecase

import com.project.insole.features.sensor.domain.model.SensorPacket
import com.project.insole.features.sensor.domain.service.StepCounterService
import javax.inject.Inject

/**
 * Use case for processing raw sensor packets and updating the step counter.
 */
class ProcessStepCountUseCase @Inject constructor(
    private val stepCounterService: StepCounterService
) {

    /**
     * Processes a single packet from either the left or right insole.
     * Returns the updated total step count.
     */
    operator fun invoke(packet: SensorPacket, isLeft: Boolean): Int {
        return stepCounterService.processPacket(packet, isLeft)
    }

    /**
     * Overload for getting the current total without processing.
     */
    fun getTotalSteps(): Int = stepCounterService.totalSteps
}
