package com.project.insole.core.ble;

/**
 * ViewModel for BLE scanning and connection management.
 * Used in setup/pairing screens to discover and connect to insole devices.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\b\u0010\r\u001a\u00020\u000eH\u0002J\u0006\u0010\u000f\u001a\u00020\u000eJ\u0006\u0010\u0010\u001a\u00020\u000eJ\u0006\u0010\u0011\u001a\u00020\u000eJ\u0016\u0010\u0012\u001a\u00020\u000e2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0014J\u0006\u0010\u0016\u001a\u00020\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u0017"}, d2 = {"Lcom/project/insole/core/ble/BleViewModel;", "Landroidx/lifecycle/ViewModel;", "bleManager", "Lcom/project/insole/core/ble/InsoleBleManager;", "<init>", "(Lcom/project/insole/core/ble/InsoleBleManager;)V", "_bleState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/core/ble/BleUiState;", "bleState", "Lkotlinx/coroutines/flow/StateFlow;", "getBleState", "()Lkotlinx/coroutines/flow/StateFlow;", "observeBleState", "", "checkBluetoothEnabled", "startScanning", "stopScanning", "connectToDevice", "deviceAddress", "", "deviceName", "disconnect", "app_debug"})
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