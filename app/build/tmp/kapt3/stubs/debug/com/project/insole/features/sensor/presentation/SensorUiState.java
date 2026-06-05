package com.project.insole.features.sensor.presentation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\'\b\u0087\b\u0018\u00002\u00020\u0001B\u0099\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\u0006\u0012\b\b\u0002\u0010\n\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\u000e\b\u0002\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u0005\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\f\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\f\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\f\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0015J\t\u0010&\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\'\u001a\u00020\u0011H\u00c6\u0003J\u000b\u0010(\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\u000b\u0010)\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\t\u0010*\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\t\u0010,\u001a\u00020\bH\u00c6\u0003J\t\u0010-\u001a\u00020\u0006H\u00c6\u0003J\t\u0010.\u001a\u00020\u0006H\u00c6\u0003J\t\u0010/\u001a\u00020\fH\u00c6\u0003J\u000f\u00100\u001a\b\u0012\u0004\u0012\u00020\f0\u0005H\u00c6\u0003J\u000b\u00101\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\t\u00102\u001a\u00020\u0003H\u00c6\u0003J\u009d\u0001\u00103\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u00062\b\b\u0002\u0010\n\u001a\u00020\u00062\b\b\u0002\u0010\u000b\u001a\u00020\f2\u000e\b\u0002\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u00052\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\f2\b\b\u0002\u0010\u000f\u001a\u00020\u00032\b\b\u0002\u0010\u0010\u001a\u00020\u00112\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\f2\b\b\u0002\u0010\u0014\u001a\u00020\u0003H\u00c6\u0001J\u0013\u00104\u001a\u00020\u00032\b\u00105\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00106\u001a\u00020\u0006H\u00d6\u0001J\t\u00107\u001a\u00020\fH\u00d6\u0001R\u0011\u0010\n\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\u000f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u001dR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u001dR\u0011\u0010\u0014\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u001dR\u0013\u0010\u0012\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0019R\u0013\u0010\u0013\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0019R\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0011\u0010\t\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0017R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001c\u00a8\u00068"}, d2 = {"Lcom/project/insole/features/sensor/presentation/SensorUiState;", "", "isLoading", "", "fsrValues", "", "", "temperature", "", "stepCount", "batteryLevel", "connectionQuality", "", "thresholdAlerts", "errorMessage", "isConnected", "sessionDurationSeconds", "", "rawBleLeft", "rawBleRight", "isRefreshing", "(ZLjava/util/List;FIILjava/lang/String;Ljava/util/List;Ljava/lang/String;ZJLjava/lang/String;Ljava/lang/String;Z)V", "getBatteryLevel", "()I", "getConnectionQuality", "()Ljava/lang/String;", "getErrorMessage", "getFsrValues", "()Ljava/util/List;", "()Z", "getRawBleLeft", "getRawBleRight", "getSessionDurationSeconds", "()J", "getStepCount", "getTemperature", "()F", "getThresholdAlerts", "component1", "component10", "component11", "component12", "component13", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class SensorUiState {
    private final boolean isLoading = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.Integer> fsrValues = null;
    private final float temperature = 0.0F;
    private final int stepCount = 0;
    private final int batteryLevel = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String connectionQuality = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> thresholdAlerts = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    private final boolean isConnected = false;
    private final long sessionDurationSeconds = 0L;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String rawBleLeft = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String rawBleRight = null;
    private final boolean isRefreshing = false;
    
    public SensorUiState(boolean isLoading, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> fsrValues, float temperature, int stepCount, int batteryLevel, @org.jetbrains.annotations.NotNull()
    java.lang.String connectionQuality, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> thresholdAlerts, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage, boolean isConnected, long sessionDurationSeconds, @org.jetbrains.annotations.Nullable()
    java.lang.String rawBleLeft, @org.jetbrains.annotations.Nullable()
    java.lang.String rawBleRight, boolean isRefreshing) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.Integer> getFsrValues() {
        return null;
    }
    
    public final float getTemperature() {
        return 0.0F;
    }
    
    public final int getStepCount() {
        return 0;
    }
    
    public final int getBatteryLevel() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getConnectionQuality() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getThresholdAlerts() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    public final boolean isConnected() {
        return false;
    }
    
    public final long getSessionDurationSeconds() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getRawBleLeft() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getRawBleRight() {
        return null;
    }
    
    public final boolean isRefreshing() {
        return false;
    }
    
    public SensorUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    public final long component10() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component12() {
        return null;
    }
    
    public final boolean component13() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.Integer> component2() {
        return null;
    }
    
    public final float component3() {
        return 0.0F;
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int component5() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component8() {
        return null;
    }
    
    public final boolean component9() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.features.sensor.presentation.SensorUiState copy(boolean isLoading, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> fsrValues, float temperature, int stepCount, int batteryLevel, @org.jetbrains.annotations.NotNull()
    java.lang.String connectionQuality, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> thresholdAlerts, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage, boolean isConnected, long sessionDurationSeconds, @org.jetbrains.annotations.Nullable()
    java.lang.String rawBleLeft, @org.jetbrains.annotations.Nullable()
    java.lang.String rawBleRight, boolean isRefreshing) {
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