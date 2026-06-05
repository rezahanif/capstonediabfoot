package com.project.insole.core.ble;

/**
 * Manages BLE connection lifecycle and initialization.
 * Handles automatic connection to previously paired device or prompts for pairing.
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\b\u0007\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\u0019\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\t\u001a\u00020\nJ\u0006\u0010\u000b\u001a\u00020\nJ\b\u0010\f\u001a\u0004\u0018\u00010\rJ\b\u0010\u000e\u001a\u0004\u0018\u00010\rJ\u0006\u0010\u000f\u001a\u00020\nJ\u0016\u0010\u0010\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\rR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/project/insole/core/ble/BleConnectionManager;", "", "context", "Landroid/content/Context;", "bleManager", "Lcom/project/insole/core/ble/InsoleBleManager;", "(Landroid/content/Context;Lcom/project/insole/core/ble/InsoleBleManager;)V", "scope", "Lkotlinx/coroutines/CoroutineScope;", "clearSavedDevice", "", "disconnectDevice", "getSavedDeviceAddress", "", "getSavedDeviceName", "initializeBleConnection", "saveDeviceConnection", "deviceAddress", "deviceName", "Companion", "app_debug"})
@android.annotation.SuppressLint(value = {"MissingPermission"})
public final class BleConnectionManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.core.ble.InsoleBleManager bleManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "BleConnectionManager";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SAVED_DEVICE_ADDRESS = "saved_insole_device_address";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SAVED_DEVICE_NAME = "saved_insole_device_name";
    @org.jetbrains.annotations.NotNull()
    public static final com.project.insole.core.ble.BleConnectionManager.Companion Companion = null;
    
    @javax.inject.Inject()
    public BleConnectionManager(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.InsoleBleManager bleManager) {
        super();
    }
    
    /**
     * Initialize BLE connection on app startup.
     * Attempts to reconnect to previously paired device.
     */
    public final void initializeBleConnection() {
    }
    
    /**
     * Save device connection for automatic reconnection.
     */
    public final void saveDeviceConnection(@org.jetbrains.annotations.NotNull()
    java.lang.String deviceAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String deviceName) {
    }
    
    /**
     * Get previously saved device address.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSavedDeviceAddress() {
        return null;
    }
    
    /**
     * Get previously saved device name.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSavedDeviceName() {
        return null;
    }
    
    /**
     * Clear saved device connection.
     */
    public final void clearSavedDevice() {
    }
    
    /**
     * Disconnect from device.
     */
    public final void disconnectDevice() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/project/insole/core/ble/BleConnectionManager$Companion;", "", "()V", "SAVED_DEVICE_ADDRESS", "", "SAVED_DEVICE_NAME", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}