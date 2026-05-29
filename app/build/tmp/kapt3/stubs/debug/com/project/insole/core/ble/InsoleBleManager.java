package com.project.insole.core.ble;

/**
 * Native BLE Manager handling GATT operations, scanning, and MTU configuration.
 * This is the single entry point for all BLE communication with the ESP32 insole.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0080\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0012\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\r\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\"\u001a\u00020\u00192\u0006\u0010#\u001a\u00020$J\u0006\u0010%\u001a\u00020\u0019J\u0018\u0010&\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\'\u001a\u00020(H\u0002J\u0006\u0010)\u001a\u00020\tJ\u001c\u0010*\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0016J$\u0010+\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\b\u0010\'\u001a\u0004\u0018\u00010(2\u0006\u0010,\u001a\u00020-H\u0016J$\u0010.\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\b\u0010\'\u001a\u0004\u0018\u00010(2\u0006\u0010,\u001a\u00020-H\u0016J\"\u0010/\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\u0006\u0010,\u001a\u00020-2\u0006\u00100\u001a\u00020-H\u0016J$\u00101\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\b\u00102\u001a\u0004\u0018\u0001032\u0006\u0010,\u001a\u00020-H\u0016J\"\u00104\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\u0006\u00105\u001a\u00020-2\u0006\u0010,\u001a\u00020-H\u0016J\u001a\u00106\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\u0006\u0010,\u001a\u00020-H\u0016J\u000e\u00107\u001a\u00020\u00192\u0006\u0010\'\u001a\u00020(J\u000e\u00108\u001a\u00020\u00192\u0006\u00109\u001a\u00020-J\u001a\u0010:\u001a\u00020\u00192\u0012\u0010;\u001a\u000e\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u00190\u0017J\u0006\u0010<\u001a\u00020\u0019J\u0006\u0010=\u001a\u00020\u0019J\u0016\u0010>\u001a\u00020\u00192\u0006\u0010\'\u001a\u00020(2\u0006\u0010?\u001a\u00020\u0018R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0016\u001a\u0010\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u0019\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u001bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\t0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0011R\u0017\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\t0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0011R\u000e\u0010\u001e\u001a\u00020\u001fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0011\u00a8\u0006@"}, d2 = {"Lcom/project/insole/core/ble/InsoleBleManager;", "Landroid/bluetooth/BluetoothGattCallback;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "_bleDeviceState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/core/ble/model/BleDeviceState;", "_isBluetoothEnabled", "", "_isScanning", "_scannedDevices", "", "Lcom/project/insole/core/ble/ScannedDevice;", "bleDeviceState", "Lkotlinx/coroutines/flow/StateFlow;", "getBleDeviceState", "()Lkotlinx/coroutines/flow/StateFlow;", "bluetoothAdapter", "Landroid/bluetooth/BluetoothAdapter;", "bluetoothManager", "Landroid/bluetooth/BluetoothManager;", "characteristicChangedListener", "Lkotlin/Function1;", "", "", "gatt", "Landroid/bluetooth/BluetoothGatt;", "isBluetoothEnabledFlow", "isScanning", "scanCallback", "Landroid/bluetooth/le/ScanCallback;", "scannedDevices", "getScannedDevices", "connect", "deviceAddress", "", "disconnect", "enableNotifications", "characteristic", "Landroid/bluetooth/BluetoothGattCharacteristic;", "isBluetoothEnabled", "onCharacteristicChanged", "onCharacteristicRead", "status", "", "onCharacteristicWrite", "onConnectionStateChange", "newState", "onDescriptorWrite", "descriptor", "Landroid/bluetooth/BluetoothGattDescriptor;", "onMtuChanged", "mtu", "onServicesDiscovered", "readCharacteristic", "requestMtu", "mtuSize", "setOnCharacteristicChangedListener", "listener", "startScanning", "stopScanning", "writeCharacteristic", "data", "app_debug"})
@android.annotation.SuppressLint(value = {"MissingPermission"})
public final class InsoleBleManager extends android.bluetooth.BluetoothGattCallback {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final android.bluetooth.BluetoothManager bluetoothManager = null;
    @org.jetbrains.annotations.Nullable()
    private final android.bluetooth.BluetoothAdapter bluetoothAdapter = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.core.ble.model.BleDeviceState> _bleDeviceState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.project.insole.core.ble.model.BleDeviceState> bleDeviceState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.project.insole.core.ble.ScannedDevice>> _scannedDevices = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.project.insole.core.ble.ScannedDevice>> scannedDevices = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isScanning = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isScanning = null;
    @org.jetbrains.annotations.Nullable()
    private android.bluetooth.BluetoothGatt gatt;
    @org.jetbrains.annotations.NotNull()
    private final android.bluetooth.le.ScanCallback scanCallback = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isBluetoothEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isBluetoothEnabledFlow = null;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super byte[], kotlin.Unit> characteristicChangedListener;
    
    public InsoleBleManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.project.insole.core.ble.model.BleDeviceState> getBleDeviceState() {
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
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isBluetoothEnabledFlow() {
        return null;
    }
    
    /**
     * Check if Bluetooth is enabled.
     */
    public final boolean isBluetoothEnabled() {
        return false;
    }
    
    /**
     * Start scanning for BLE devices with dual insole filters.
     */
    public final void startScanning() {
    }
    
    /**
     * Stop scanning for BLE devices.
     */
    public final void stopScanning() {
    }
    
    /**
     * Connects to a BLE device by address.
     */
    public final void connect(@org.jetbrains.annotations.NotNull()
    java.lang.String deviceAddress) {
    }
    
    /**
     * Disconnects from the current device.
     */
    public final void disconnect() {
    }
    
    /**
     * Requests MTU size update for faster data transfer.
     * minSdk = 24, so LOLLIPOP (API 21) check is always true and omitted.
     */
    public final void requestMtu(int mtuSize) {
    }
    
    /**
     * Reads characteristic value from the device.
     */
    public final void readCharacteristic(@org.jetbrains.annotations.NotNull()
    android.bluetooth.BluetoothGattCharacteristic characteristic) {
    }
    
    /**
     * Writes data to a characteristic on the device.
     */
    public final void writeCharacteristic(@org.jetbrains.annotations.NotNull()
    android.bluetooth.BluetoothGattCharacteristic characteristic, @org.jetbrains.annotations.NotNull()
    byte[] data) {
    }
    
    @java.lang.Override()
    public void onConnectionStateChange(@org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGatt gatt, int status, int newState) {
    }
    
    @java.lang.Override()
    public void onServicesDiscovered(@org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGatt gatt, int status) {
    }
    
    private final void enableNotifications(android.bluetooth.BluetoothGatt gatt, android.bluetooth.BluetoothGattCharacteristic characteristic) {
    }
    
    @java.lang.Override()
    public void onCharacteristicRead(@org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGatt gatt, @org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGattCharacteristic characteristic, int status) {
    }
    
    @java.lang.Override()
    public void onCharacteristicWrite(@org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGatt gatt, @org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGattCharacteristic characteristic, int status) {
    }
    
    @java.lang.Override()
    public void onCharacteristicChanged(@org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGatt gatt, @org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGattCharacteristic characteristic) {
    }
    
    public final void setOnCharacteristicChangedListener(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super byte[], kotlin.Unit> listener) {
    }
    
    @java.lang.Override()
    public void onDescriptorWrite(@org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGatt gatt, @org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGattDescriptor descriptor, int status) {
    }
    
    @java.lang.Override()
    public void onMtuChanged(@org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothGatt gatt, int mtu, int status) {
    }
}