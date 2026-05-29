package com.project.insole.core.ble.permission;

/**
 * Manages Bluetooth Low Energy permissions for Android devices.
 * Handles runtime permission requests for both pre and post Android 12 devices.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0011\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00070\n\u00a2\u0006\u0002\u0010\u000bJ\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006J\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u0007R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/project/insole/core/ble/permission/BlePermissionManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "android12PlusPermissions", "", "", "preAndroid12Permissions", "getPermissionsForVersion", "", "()[Ljava/lang/String;", "getRequiredPermissions", "hasAllPermissions", "", "isPermissionGranted", "permission", "app_debug"})
public final class BlePermissionManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    
    /**
     * List of required BLE permissions for Android 12+
     */
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> android12PlusPermissions = null;
    
    /**
     * List of required permissions for pre-Android 12 devices
     */
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> preAndroid12Permissions = null;
    
    public BlePermissionManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Checks if all required BLE permissions are granted.
     */
    public final boolean hasAllPermissions() {
        return false;
    }
    
    /**
     * Returns list of permissions that need to be requested.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getRequiredPermissions() {
        return null;
    }
    
    /**
     * Checks specific permission status.
     */
    public final boolean isPermissionGranted(@org.jetbrains.annotations.NotNull()
    java.lang.String permission) {
        return false;
    }
    
    /**
     * Returns the appropriate permissions based on Android version.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String[] getPermissionsForVersion() {
        return null;
    }
}