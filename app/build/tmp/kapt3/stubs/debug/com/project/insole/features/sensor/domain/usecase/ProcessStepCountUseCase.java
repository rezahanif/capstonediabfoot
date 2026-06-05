package com.project.insole.features.sensor.domain.usecase;

/**
 * Pure Kotlin use case for calculating step count if done app-side.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0011\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/project/insole/features/sensor/domain/usecase/ProcessStepCountUseCase;", "", "stepCounterService", "Lcom/project/insole/features/sensor/domain/service/StepCounterService;", "(Lcom/project/insole/features/sensor/domain/service/StepCounterService;)V", "invoke", "", "sensorData", "Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;", "app_debug"})
public final class ProcessStepCountUseCase {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.domain.service.StepCounterService stepCounterService = null;
    
    @javax.inject.Inject()
    public ProcessStepCountUseCase(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.domain.service.StepCounterService stepCounterService) {
        super();
    }
    
    /**
     * Returns the current total steps from the StepCounterService.
     */
    public final int invoke(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.domain.model.InsoleSensorData sensorData) {
        return 0;
    }
}