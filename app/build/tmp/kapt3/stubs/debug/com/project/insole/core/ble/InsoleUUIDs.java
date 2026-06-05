package com.project.insole.core.ble;

/**
 * BLE UUIDs for Smart Insole Left and Right devices.
 * Matches the ESP32 firmware exactly.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\b\u001a\u00020\u00042\b\u0010\t\u001a\u0004\u0018\u00010\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/project/insole/core/ble/InsoleUUIDs;", "", "()V", "LEFT_CHARACTERISTIC", "", "LEFT_SERVICE", "RIGHT_CHARACTERISTIC", "RIGHT_SERVICE", "identifySide", "uuid", "app_debug"})
public final class InsoleUUIDs {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String LEFT_SERVICE = "4fa2c732-ca9a-4c20-9492-c167df3c942b";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String LEFT_CHARACTERISTIC = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String RIGHT_SERVICE = "4fa2c732-ca9a-4c20-9492-c167df3c942c";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String RIGHT_CHARACTERISTIC = "beb5483e-36e1-4688-b7f5-ea07361b26c9";
    @org.jetbrains.annotations.NotNull()
    public static final com.project.insole.core.ble.InsoleUUIDs INSTANCE = null;
    
    private InsoleUUIDs() {
        super();
    }
    
    /**
     * Identifies the side (LEFT/RIGHT) based on the Service UUID string.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String identifySide(@org.jetbrains.annotations.Nullable()
    java.lang.String uuid) {
        return null;
    }
}