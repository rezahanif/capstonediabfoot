package com.project.insole.features.sensor.presentation;

/**
 * ViewModel for all sensor-related screens (Dashboard & Monitoring).
 * Exposes immutable StateFlow with all ESP32 sensor data.
 * Both DashboardScreen and MonitoringScreen use this same ViewModel.
 * Only accesses domain use cases - no data layer access directly.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B)\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0004\b\n\u0010\u000bJ\b\u0010\u0013\u001a\u00020\u0014H\u0002J\u0010\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\u0017H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006\u0018"}, d2 = {"Lcom/project/insole/features/sensor/presentation/SensorViewModel;", "Landroidx/lifecycle/ViewModel;", "sensorRepository", "Lcom/project/insole/features/sensor/data/repository/SensorRepository;", "mapPressureToGridUseCase", "Lcom/project/insole/features/sensor/domain/usecase/MapPressureToGridUseCase;", "processStepCountUseCase", "Lcom/project/insole/features/sensor/domain/usecase/ProcessStepCountUseCase;", "analyzePressureThresholdUseCase", "Lcom/project/insole/features/sensor/domain/usecase/AnalyzePressureThresholdUseCase;", "<init>", "(Lcom/project/insole/features/sensor/data/repository/SensorRepository;Lcom/project/insole/features/sensor/domain/usecase/MapPressureToGridUseCase;Lcom/project/insole/features/sensor/domain/usecase/ProcessStepCountUseCase;Lcom/project/insole/features/sensor/domain/usecase/AnalyzePressureThresholdUseCase;)V", "_sensorState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/features/sensor/presentation/SensorUiState;", "sensorState", "Lkotlinx/coroutines/flow/StateFlow;", "getSensorState", "()Lkotlinx/coroutines/flow/StateFlow;", "observeSensorData", "", "updateSensorState", "sensorData", "Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SensorViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.data.repository.SensorRepository sensorRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.domain.usecase.MapPressureToGridUseCase mapPressureToGridUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.domain.usecase.ProcessStepCountUseCase processStepCountUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.domain.usecase.AnalyzePressureThresholdUseCase analyzePressureThresholdUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.features.sensor.presentation.SensorUiState> _sensorState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.project.insole.features.sensor.presentation.SensorUiState> sensorState = null;
    
    @javax.inject.Inject()
    public SensorViewModel(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.data.repository.SensorRepository sensorRepository, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.domain.usecase.MapPressureToGridUseCase mapPressureToGridUseCase, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.domain.usecase.ProcessStepCountUseCase processStepCountUseCase, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.domain.usecase.AnalyzePressureThresholdUseCase analyzePressureThresholdUseCase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.project.insole.features.sensor.presentation.SensorUiState> getSensorState() {
        return null;
    }
    
    private final void observeSensorData() {
    }
    
    private final void updateSensorState(com.project.insole.features.sensor.domain.model.InsoleSensorData sensorData) {
    }
}