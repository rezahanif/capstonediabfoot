package com.project.insole.features.sensor.domain.usecase

import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.service.StepCounterService
import javax.inject.Inject

/**
 * Pure Kotlin use case for calculating step count if done app-side.
 */
class ProcessStepCountUseCase @Inject constructor(
    private val stepCounterService: StepCounterService
) {

    /**
     * Returns the current total steps from the StepCounterService.
     */
    operator fun invoke(sensorData: InsoleSensorData): Int {
        // Return current session total
        return stepCounterService.totalSteps
    }
}
