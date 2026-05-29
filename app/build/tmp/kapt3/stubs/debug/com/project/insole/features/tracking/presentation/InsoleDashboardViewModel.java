package com.project.insole.features.tracking.presentation;

/**
 * ViewModel for the insole dashboard screen.
 * Exposes immutable StateFlow for UI consumption.
 * Only accesses domain use cases - no data layer access directly.
 * Repository is accessed to get sensor data, then use cases are applied.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\b\u0010\u0012\u001a\u00020\u0013H\u0002J\u0010\u0010\u0014\u001a\u00020\u00132\u0006\u0010\u0015\u001a\u00020\u0016H\u0002R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/project/insole/features/tracking/presentation/InsoleDashboardViewModel;", "Landroidx/lifecycle/ViewModel;", "sensorRepository", "Lcom/project/insole/features/tracking/data/repository/SensorRepository;", "mapPressureToGridUseCase", "Lcom/project/insole/features/tracking/domain/usecase/MapPressureToGridUseCase;", "processStepCountUseCase", "Lcom/project/insole/features/tracking/domain/usecase/ProcessStepCountUseCase;", "analyzePressureThresholdUseCase", "Lcom/project/insole/features/tracking/domain/usecase/AnalyzePressureThresholdUseCase;", "(Lcom/project/insole/features/tracking/data/repository/SensorRepository;Lcom/project/insole/features/tracking/domain/usecase/MapPressureToGridUseCase;Lcom/project/insole/features/tracking/domain/usecase/ProcessStepCountUseCase;Lcom/project/insole/features/tracking/domain/usecase/AnalyzePressureThresholdUseCase;)V", "_dashboardState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/features/tracking/presentation/DashboardUiState;", "dashboardState", "Lkotlinx/coroutines/flow/StateFlow;", "getDashboardState", "()Lkotlinx/coroutines/flow/StateFlow;", "observeSensorData", "", "updateDashboard", "sensorData", "Lcom/project/insole/features/tracking/domain/model/InsoleSensorData;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class InsoleDashboardViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.tracking.data.repository.SensorRepository sensorRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.tracking.domain.usecase.MapPressureToGridUseCase mapPressureToGridUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.tracking.domain.usecase.ProcessStepCountUseCase processStepCountUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.tracking.domain.usecase.AnalyzePressureThresholdUseCase analyzePressureThresholdUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.features.tracking.presentation.DashboardUiState> _dashboardState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.project.insole.features.tracking.presentation.DashboardUiState> dashboardState = null;
    
    @javax.inject.Inject()
    public InsoleDashboardViewModel(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.tracking.data.repository.SensorRepository sensorRepository, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.tracking.domain.usecase.MapPressureToGridUseCase mapPressureToGridUseCase, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.tracking.domain.usecase.ProcessStepCountUseCase processStepCountUseCase, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.tracking.domain.usecase.AnalyzePressureThresholdUseCase analyzePressureThresholdUseCase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.project.insole.features.tracking.presentation.DashboardUiState> getDashboardState() {
        return null;
    }
    
    private final void observeSensorData() {
    }
    
    private final void updateDashboard(com.project.insole.features.tracking.domain.model.InsoleSensorData sensorData) {
    }
}