package com.project.insole;

/**
 * Single activity hosting Jetpack Compose Navigation for the entire app.
 * All screens are defined in the NavHost with their respective routes.
 * Handles Bluetooth permissions at runtime (Android 6.0+).
 * Initializes BLE connection on app startup.
 */
@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u0004\b\u0007\u0018\u0000 \u001b2\u00020\u0001:\u0001\u001bB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\t\u001a\u00020\nH\u0002J\b\u0010\u000b\u001a\u00020\nH\u0002J\b\u0010\f\u001a\u00020\nH\u0002J\u0012\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0014J+\u0010\u0011\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\u00132\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00160\u00152\u0006\u0010\u0017\u001a\u00020\u0018H\u0016\u00a2\u0006\u0002\u0010\u0019J\b\u0010\u001a\u001a\u00020\u000eH\u0002R\u001e\u0010\u0003\u001a\u00020\u00048\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\b\u00a8\u0006\u001c"}, d2 = {"Lcom/project/insole/MainActivity;", "Landroidx/activity/ComponentActivity;", "()V", "bleConnectionManager", "Lcom/project/insole/core/ble/BleConnectionManager;", "getBleConnectionManager", "()Lcom/project/insole/core/ble/BleConnectionManager;", "setBleConnectionManager", "(Lcom/project/insole/core/ble/BleConnectionManager;)V", "hasBluetoothConnectPermission", "", "hasBluetoothScanPermission", "hasLocationPermission", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onRequestPermissionsResult", "requestCode", "", "permissions", "", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "requestBluetoothPermissions", "Companion", "app_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    private static final int REQUEST_CODE_BLE_PERMISSIONS = 100;
    @javax.inject.Inject()
    public com.project.insole.core.ble.BleConnectionManager bleConnectionManager;
    @org.jetbrains.annotations.NotNull()
    public static final com.project.insole.MainActivity.Companion Companion = null;
    
    public MainActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.core.ble.BleConnectionManager getBleConnectionManager() {
        return null;
    }
    
    public final void setBleConnectionManager(@org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.BleConnectionManager p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    /**
     * Request Bluetooth permissions at runtime (Android 6.0+).
     * Checks if permissions are already granted before requesting.
     */
    private final void requestBluetoothPermissions() {
    }
    
    @java.lang.Override()
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull()
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull()
    int[] grantResults) {
    }
    
    private final boolean hasBluetoothScanPermission() {
        return false;
    }
    
    private final boolean hasBluetoothConnectPermission() {
        return false;
    }
    
    private final boolean hasLocationPermission() {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/project/insole/MainActivity$Companion;", "", "()V", "REQUEST_CODE_BLE_PERMISSIONS", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}