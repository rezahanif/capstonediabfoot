package com.project.insole.core.ble;

/**
 * Native BLE Manager handling GATT operations, scanning, and MTU configuration.
 * Enhanced to support two concurrent connections (Left & Right insoles).
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0096\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001f\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020\u00142\n\b\u0002\u0010.\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\u0002\u0010/J\u0010\u00100\u001a\u00020\u00072\u0006\u00101\u001a\u00020\u0015H\u0002J\u0012\u00102\u001a\u00020,2\n\b\u0002\u0010-\u001a\u0004\u0018\u00010\u0014J\u0018\u00103\u001a\u00020,2\u0006\u00101\u001a\u00020\u00152\u0006\u00104\u001a\u000205H\u0002J \u00106\u001a\u00020,2\u0006\u00101\u001a\u00020\u00152\u0006\u00104\u001a\u0002052\u0006\u00107\u001a\u000208H\u0002J\u0006\u00109\u001a\u00020\u0007J \u0010:\u001a\u00020,2\u0006\u00101\u001a\u00020\u00152\u0006\u00104\u001a\u0002052\u0006\u00107\u001a\u000208H\u0016J\u001c\u0010:\u001a\u00020,2\b\u00101\u001a\u0004\u0018\u00010\u00152\b\u00104\u001a\u0004\u0018\u000105H\u0017J\"\u0010;\u001a\u00020,2\b\u00101\u001a\u0004\u0018\u00010\u00152\u0006\u0010<\u001a\u00020=2\u0006\u0010>\u001a\u00020=H\u0016J\u001a\u0010?\u001a\u00020,2\b\u00101\u001a\u0004\u0018\u00010\u00152\u0006\u0010<\u001a\u00020=H\u0016J&\u0010@\u001a\u00020,2\u001e\u0010A\u001a\u001a\u0012\u0004\u0012\u000208\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020,0BJ\u0006\u0010C\u001a\u00020,J\u0006\u0010D\u001a\u00020,J\u0018\u0010E\u001a\u00020,2\u0006\u0010.\u001a\u00020\u00072\u0006\u0010F\u001a\u00020\nH\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00150\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0016\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00070\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00070\u001c\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001dR\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00070\u001c\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001dR\u0017\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\n0\u001c\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001dR\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\n0\u001c\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001dR\u000e\u0010#\u001a\u00020$X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010%\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u001c\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001dR\u0017\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00110(\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010*\u00a8\u0006G"}, d2 = {"Lcom/project/insole/core/ble/InsoleBleManager;", "Landroid/bluetooth/BluetoothGattCallback;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "_isBluetoothEnabled", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_isScanning", "_leftDeviceState", "Lcom/project/insole/core/ble/model/BleDeviceState;", "_rightDeviceState", "_scannedDevices", "", "Lcom/project/insole/core/ble/ScannedDevice;", "_telemetryFlow", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/project/insole/core/ble/BleDataPacket;", "activeGatts", "", "", "Landroid/bluetooth/BluetoothGatt;", "addressToSide", "bluetoothAdapter", "Landroid/bluetooth/BluetoothAdapter;", "bluetoothManager", "Landroid/bluetooth/BluetoothManager;", "isBluetoothEnabledFlow", "Lkotlinx/coroutines/flow/StateFlow;", "()Lkotlinx/coroutines/flow/StateFlow;", "isScanning", "leftDeviceState", "getLeftDeviceState", "rightDeviceState", "getRightDeviceState", "scanCallback", "Landroid/bluetooth/le/ScanCallback;", "scannedDevices", "getScannedDevices", "telemetryFlow", "Lkotlinx/coroutines/flow/SharedFlow;", "getTelemetryFlow", "()Lkotlinx/coroutines/flow/SharedFlow;", "connect", "", "deviceAddress", "isLeft", "(Ljava/lang/String;Ljava/lang/Boolean;)V", "determineSideFromGatt", "gatt", "disconnect", "enableNotifications", "characteristic", "Landroid/bluetooth/BluetoothGattCharacteristic;", "handleCharacteristicChange", "value", "", "isBluetoothEnabled", "onCharacteristicChanged", "onConnectionStateChange", "status", "", "newState", "onServicesDiscovered", "setOnCharacteristicChangedListener", "listener", "Lkotlin/Function3;", "startScanning", "stopScanning", "updateDeviceState", "state", "app_debug"})
@android.annotation.SuppressLint(value = {"MissingPermission"})
public final class InsoleBleManager extends android.bluetooth.BluetoothGattCallback {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final android.bluetooth.BluetoothManager bluetoothManager = null;
    @org.jetbrains.annotations.Nullable()
    private final android.bluetooth.BluetoothAdapter bluetoothAdapter = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.core.ble.model.BleDeviceState> _leftDeviceState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.project.insole.core.ble.model.BleDeviceState> leftDeviceState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.core.ble.model.BleDeviceState> _rightDeviceState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.project.insole.core.ble.model.BleDeviceState> rightDeviceState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.project.insole.core.ble.ScannedDevice>> _scannedDevices = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.project.insole.core.ble.ScannedDevice>> scannedDevices = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isScanning = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isScanning = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.project.insole.core.ble.BleDataPacket> _telemetryFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.project.insole.core.ble.BleDataPacket> telemetryFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, android.bluetooth.BluetoothGatt> activeGatts = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.Boolean> addressToSide = null;
    @org.jetbrains.annotations.NotNull()
    private final android.bluetooth.le.ScanCallback scanCallback = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isBluetoothEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isBluetoothEnabledFlow = null;
    
    public InsoleBleManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.project.insole.core.ble.model.BleDeviceState> getLeftDeviceState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.project.insole.core.ble.model.BleDeviceState> getRightDeviceState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.project.insole.core.ble.ScannedDevice>> getScannedDevices() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isScanning() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.project.insole.core.ble.BleDataPacket> getTelemetryFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isBluetoothEnabledFlow() {
        return null;
    }
    
    public final boolean isBluetoothEnabled() {
        return false;
    }
    
    public final void startScanning() {
    }
    
    public final void stopScanning() {
    }
    
    /**
     * Connects to a device and attempts to identify if it is Left or Right.
     */
    public final void connect(@org.jetbrains.annotations.NotNull()
    java.lang.String deviceAddress, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean isLeft) {
    }
    
    public final void disconnect(@org.jetbrains.annotations.Nullable()
    java.lang.String deviceAddress) {
    }
    
    private final boolean determineSideFromGatt(android.bluetooth.BluetoothGatt gatt) {
        return false;
    }
    
    @java.lang.Override()
    public void onConnectionStateChange(@org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGatt gatt, int status, int newState) {
    }
    
    @java.lang.Override()
    public void onServicesDiscovered(@org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGatt gatt, int status) {
    }
    
    private final void updateDeviceState(boolean isLeft, com.project.insole.core.ble.model.BleDeviceState state) {
    }
    
    private final void enableNotifications(android.bluetooth.BluetoothGatt gatt, android.bluetooth.BluetoothGattCharacteristic characteristic) {
    }
    
    @java.lang.Override()
    public void onCharacteristicChanged(@org.jetbrains.annotations.NotNull()
    android.bluetooth.BluetoothGatt gatt, @org.jetbrains.annotations.NotNull()
    android.bluetooth.BluetoothGattCharacteristic characteristic, @org.jetbrains.annotations.NotNull()
    byte[] value) {
    }
    
    @java.lang.Override()
    @kotlin.Suppress(names = {"DEPRECATION"})
    @java.lang.Deprecated()
    public void onCharacteristicChanged(@org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGatt gatt, @org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGattCharacteristic characteristic) {
    }
    
    private final void handleCharacteristicChange(android.bluetooth.BluetoothGatt gatt, android.bluetooth.BluetoothGattCharacteristic characteristic, byte[] value) {
    }
    
    public final void setOnCharacteristicChangedListener(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function3<? super byte[], ? super java.lang.Boolean, ? super java.lang.String, kotlin.Unit> listener) {
    }
}