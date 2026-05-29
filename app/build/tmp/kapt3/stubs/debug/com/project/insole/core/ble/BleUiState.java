package com.project.insole.core.ble;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0014\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001BI\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\u0002\u0010\rJ\t\u0010\u0016\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\tH\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u000bH\u00c6\u0003J\u000b\u0010\u001b\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003JM\u0010\u001c\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u000bH\u00c6\u0001J\u0013\u0010\u001d\u001a\u00020\u00032\b\u0010\u001e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001f\u001a\u00020 H\u00d6\u0001J\t\u0010!\u001a\u00020\u000bH\u00d6\u0001R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0013\u0010\f\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0013R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010\u0013R\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006\""}, d2 = {"Lcom/project/insole/core/ble/BleUiState;", "", "isBluetoothEnabled", "", "isScanning", "scannedDevices", "", "Lcom/project/insole/core/ble/ScannedDevice;", "deviceState", "Lcom/project/insole/core/ble/model/BleDeviceState;", "connectedDeviceName", "", "errorMessage", "(ZZLjava/util/List;Lcom/project/insole/core/ble/model/BleDeviceState;Ljava/lang/String;Ljava/lang/String;)V", "getConnectedDeviceName", "()Ljava/lang/String;", "getDeviceState", "()Lcom/project/insole/core/ble/model/BleDeviceState;", "getErrorMessage", "()Z", "getScannedDevices", "()Ljava/util/List;", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class BleUiState {
    private final boolean isBluetoothEnabled = false;
    private final boolean isScanning = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.project.insole.core.ble.ScannedDevice> scannedDevices = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.core.ble.model.BleDeviceState deviceState = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String connectedDeviceName = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    
    public BleUiState(boolean isBluetoothEnabled, boolean isScanning, @org.jetbrains.annotations.NotNull()
    java.util.List<com.project.insole.core.ble.ScannedDevice> scannedDevices, @org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.model.BleDeviceState deviceState, @org.jetbrains.annotations.NotNull()
    java.lang.String connectedDeviceName, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage) {
        super();
    }
    
    public final boolean isBluetoothEnabled() {
        return false;
    }
    
    public final boolean isScanning() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.project.insole.core.ble.ScannedDevice> getScannedDevices() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.core.ble.model.BleDeviceState getDeviceState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getConnectedDeviceName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    public BleUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    public final boolean component2() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.project.insole.core.ble.ScannedDevice> component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.core.ble.model.BleDeviceState component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.core.ble.BleUiState copy(boolean isBluetoothEnabled, boolean isScanning, @org.jetbrains.annotations.NotNull()
    java.util.List<com.project.insole.core.ble.ScannedDevice> scannedDevices, @org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.model.BleDeviceState deviceState, @org.jetbrains.annotations.NotNull()
    java.lang.String connectedDeviceName, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}