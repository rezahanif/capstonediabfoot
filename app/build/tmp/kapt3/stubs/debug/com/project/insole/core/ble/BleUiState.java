package com.project.insole.core.ble;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b2\b\u0087\b\u0018\u00002\u00020\u0001B\u00b1\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\t\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\f\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0017\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u0019\u0012\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\f\u00a2\u0006\u0002\u0010\u001bJ\t\u00106\u001a\u00020\u0003H\u00c6\u0003J\t\u00107\u001a\u00020\u0012H\u00c6\u0003J\t\u00108\u001a\u00020\u0012H\u00c6\u0003J\t\u00109\u001a\u00020\u0012H\u00c6\u0003J\t\u0010:\u001a\u00020\u0012H\u00c6\u0003J\t\u0010;\u001a\u00020\u0017H\u00c6\u0003J\t\u0010<\u001a\u00020\u0019H\u00c6\u0003J\u000b\u0010=\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\t\u0010>\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010?\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u00c6\u0003J\t\u0010@\u001a\u00020\tH\u00c6\u0003J\t\u0010A\u001a\u00020\tH\u00c6\u0003J\u000b\u0010B\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\u000b\u0010C\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\t\u0010D\u001a\u00020\u000fH\u00c6\u0003J\t\u0010E\u001a\u00020\u000fH\u00c6\u0003J\u00b5\u0001\u0010F\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\f2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u000f2\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00122\b\b\u0002\u0010\u0014\u001a\u00020\u00122\b\b\u0002\u0010\u0015\u001a\u00020\u00122\b\b\u0002\u0010\u0016\u001a\u00020\u00172\b\b\u0002\u0010\u0018\u001a\u00020\u00192\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\fH\u00c6\u0001J\u0013\u0010G\u001a\u00020\u00032\b\u0010H\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010I\u001a\u00020\u0017H\u00d6\u0001J\t\u0010J\u001a\u00020\fH\u00d6\u0001R\u0013\u0010\u001a\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u001eR\u0011\u0010\u001f\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\u001f\u0010\u001eR\u0011\u0010 \u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b \u0010\u001eR\u0011\u0010!\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b!\u0010\u001eR\u0011\u0010\"\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\"\u0010\u001eR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010\u001eR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010&R\u0011\u0010\u0014\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010(R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001dR\u0011\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010(R\u0011\u0010\n\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010$R\u0011\u0010\u0010\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010&R\u0011\u0010\u0015\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010(R\u0013\u0010\r\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010\u001dR\u0011\u0010\u0013\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010(R\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u00101R\u0011\u0010\u0016\u001a\u00020\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u00103R\u0011\u0010\u0018\u001a\u00020\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u00105\u00a8\u0006K"}, d2 = {"Lcom/project/insole/core/ble/BleUiState;", "", "isBluetoothEnabled", "", "isScanning", "scannedDevices", "", "Lcom/project/insole/core/ble/ScannedDevice;", "leftDeviceState", "Lcom/project/insole/core/ble/model/BleDeviceState;", "rightDeviceState", "leftRawData", "", "rightRawData", "leftPacketSeq", "", "rightPacketSeq", "leftTempC", "", "rightTempC", "leftPressure", "rightPressure", "totalSteps", "", "walkState", "Lcom/project/insole/features/sensor/presentation/components/WalkState;", "errorMessage", "(ZZLjava/util/List;Lcom/project/insole/core/ble/model/BleDeviceState;Lcom/project/insole/core/ble/model/BleDeviceState;Ljava/lang/String;Ljava/lang/String;JJFFFFILcom/project/insole/features/sensor/presentation/components/WalkState;Ljava/lang/String;)V", "getErrorMessage", "()Ljava/lang/String;", "()Z", "isBothConnected", "isEitherConnecting", "isLeftConnected", "isRightConnected", "getLeftDeviceState", "()Lcom/project/insole/core/ble/model/BleDeviceState;", "getLeftPacketSeq", "()J", "getLeftPressure", "()F", "getLeftRawData", "getLeftTempC", "getRightDeviceState", "getRightPacketSeq", "getRightPressure", "getRightRawData", "getRightTempC", "getScannedDevices", "()Ljava/util/List;", "getTotalSteps", "()I", "getWalkState", "()Lcom/project/insole/features/sensor/presentation/components/WalkState;", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class BleUiState {
    private final boolean isBluetoothEnabled = false;
    private final boolean isScanning = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.project.insole.core.ble.ScannedDevice> scannedDevices = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.core.ble.model.BleDeviceState leftDeviceState = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.core.ble.model.BleDeviceState rightDeviceState = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String leftRawData = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String rightRawData = null;
    private final long leftPacketSeq = 0L;
    private final long rightPacketSeq = 0L;
    private final float leftTempC = 0.0F;
    private final float rightTempC = 0.0F;
    private final float leftPressure = 0.0F;
    private final float rightPressure = 0.0F;
    private final int totalSteps = 0;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.presentation.components.WalkState walkState = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    
    public BleUiState(boolean isBluetoothEnabled, boolean isScanning, @org.jetbrains.annotations.NotNull()
    java.util.List<com.project.insole.core.ble.ScannedDevice> scannedDevices, @org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.model.BleDeviceState leftDeviceState, @org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.model.BleDeviceState rightDeviceState, @org.jetbrains.annotations.Nullable()
    java.lang.String leftRawData, @org.jetbrains.annotations.Nullable()
    java.lang.String rightRawData, long leftPacketSeq, long rightPacketSeq, float leftTempC, float rightTempC, float leftPressure, float rightPressure, int totalSteps, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.presentation.components.WalkState walkState, @org.jetbrains.annotations.Nullable()
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
    public final com.project.insole.core.ble.model.BleDeviceState getLeftDeviceState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.core.ble.model.BleDeviceState getRightDeviceState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLeftRawData() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getRightRawData() {
        return null;
    }
    
    public final long getLeftPacketSeq() {
        return 0L;
    }
    
    public final long getRightPacketSeq() {
        return 0L;
    }
    
    public final float getLeftTempC() {
        return 0.0F;
    }
    
    public final float getRightTempC() {
        return 0.0F;
    }
    
    public final float getLeftPressure() {
        return 0.0F;
    }
    
    public final float getRightPressure() {
        return 0.0F;
    }
    
    public final int getTotalSteps() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.features.sensor.presentation.components.WalkState getWalkState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    public final boolean isLeftConnected() {
        return false;
    }
    
    public final boolean isRightConnected() {
        return false;
    }
    
    public final boolean isBothConnected() {
        return false;
    }
    
    public final boolean isEitherConnecting() {
        return false;
    }
    
    public BleUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    public final float component10() {
        return 0.0F;
    }
    
    public final float component11() {
        return 0.0F;
    }
    
    public final float component12() {
        return 0.0F;
    }
    
    public final float component13() {
        return 0.0F;
    }
    
    public final int component14() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.features.sensor.presentation.components.WalkState component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component16() {
        return null;
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
    public final com.project.insole.core.ble.model.BleDeviceState component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    public final long component8() {
        return 0L;
    }
    
    public final long component9() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.core.ble.BleUiState copy(boolean isBluetoothEnabled, boolean isScanning, @org.jetbrains.annotations.NotNull()
    java.util.List<com.project.insole.core.ble.ScannedDevice> scannedDevices, @org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.model.BleDeviceState leftDeviceState, @org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.model.BleDeviceState rightDeviceState, @org.jetbrains.annotations.Nullable()
    java.lang.String leftRawData, @org.jetbrains.annotations.Nullable()
    java.lang.String rightRawData, long leftPacketSeq, long rightPacketSeq, float leftTempC, float rightTempC, float leftPressure, float rightPressure, int totalSteps, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.presentation.components.WalkState walkState, @org.jetbrains.annotations.Nullable()
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