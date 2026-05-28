package com.project.insole.core.ble.permission;

/**
 * Manages Bluetooth Low Energy permissions for Android devices.
 * Handles runtime permission requests for both pre and post Android 12 devices.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0011\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0006\u0010\n\u001a\u00020\u000bJ\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\b0\u0007J\u000e\u0010\r\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\bJ\u0011\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\b0\u0010\u00a2\u0006\u0002\u0010\u0011R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/project/insole/core/ble/permission/BlePermissionManager;", "", "context", "Landroid/content/Context;", "<init>", "(Landroid/content/Context;)V", "android12PlusPermissions", "", "", "preAndroid12Permissions", "hasAllPermissions", "", "getRequiredPermissions", "isPermissionGranted", "permission", "getPermissionsForVersion", "", "()[Ljava/lang/String;", "app_debug"})
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