package com.project.insole.core.ble;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u000e\u001a\u00020\u000fJ\u000e\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u0012J\u0012\u0010\u0013\u001a\u00020\u000f2\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0012J\b\u0010\u0014\u001a\u00020\u000fH\u0002J\b\u0010\u0015\u001a\u00020\u000fH\u0002J\u0006\u0010\u0016\u001a\u00020\u000fJ\u0006\u0010\u0017\u001a\u00020\u000fR\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/project/insole/core/ble/BleViewModel;", "Landroidx/lifecycle/ViewModel;", "bleManager", "Lcom/project/insole/core/ble/InsoleBleManager;", "stepCounterService", "Lcom/project/insole/features/sensor/domain/service/StepCounterService;", "(Lcom/project/insole/core/ble/InsoleBleManager;Lcom/project/insole/features/sensor/domain/service/StepCounterService;)V", "_bleState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/core/ble/BleUiState;", "bleState", "Lkotlinx/coroutines/flow/StateFlow;", "getBleState", "()Lkotlinx/coroutines/flow/StateFlow;", "checkBluetoothEnabled", "", "connectToDevice", "deviceAddress", "", "disconnect", "observeBleState", "observeTelemetry", "startScanning", "stopScanning", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class BleViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.core.ble.InsoleBleManager bleManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.domain.service.StepCounterService stepCounterService = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.core.ble.BleUiState> _bleState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.project.insole.core.ble.BleUiState> bleState = null;
    
    @javax.inject.Inject()
    public BleViewModel(@org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.InsoleBleManager bleManager, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.domain.service.StepCounterService stepCounterService) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.project.insole.core.ble.BleUiState> getBleState() {
        return null;
    }
    
    private final void observeTelemetry() {
    }
    
    private final void observeBleState() {
    }
    
    public final void checkBluetoothEnabled() {
    }
    
    public final void startScanning() {
    }
    
    public final void stopScanning() {
    }
    
    public final void connectToDevice(@org.jetbrains.annotations.NotNull()
    java.lang.String deviceAddress) {
    }
    
    public final void disconnect(@org.jetbrains.annotations.Nullable()
    java.lang.String deviceAddress) {
    }
}