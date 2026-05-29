package com.project.insole.core.ble;

/**
 * ViewModel for BLE scanning and connection management.
 * Used in setup/pairing screens to discover and connect to insole devices.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\f\u001a\u00020\rJ\u0016\u0010\u000e\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010J\u0006\u0010\u0012\u001a\u00020\rJ\b\u0010\u0013\u001a\u00020\rH\u0002J\u0006\u0010\u0014\u001a\u00020\rJ\u0006\u0010\u0015\u001a\u00020\rR\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0016"}, d2 = {"Lcom/project/insole/core/ble/BleViewModel;", "Landroidx/lifecycle/ViewModel;", "bleManager", "Lcom/project/insole/core/ble/InsoleBleManager;", "(Lcom/project/insole/core/ble/InsoleBleManager;)V", "_bleState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/core/ble/BleUiState;", "bleState", "Lkotlinx/coroutines/flow/StateFlow;", "getBleState", "()Lkotlinx/coroutines/flow/StateFlow;", "checkBluetoothEnabled", "", "connectToDevice", "deviceAddress", "", "deviceName", "disconnect", "observeBleState", "startScanning", "stopScanning", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class BleViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.core.ble.InsoleBleManager bleManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.core.ble.BleUiState> _bleState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.project.insole.core.ble.BleUiState> bleState = null;
    
    @javax.inject.Inject()
    public BleViewModel(@org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.InsoleBleManager bleManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.project.insole.core.ble.BleUiState> getBleState() {
        return null;
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
    java.lang.String deviceAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String deviceName) {
    }
    
    public final void disconnect() {
    }
}