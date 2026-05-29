package com.project.insole.features.diagnostics.data;

/**
 * Data source for device diagnostics information.
 * Handles battery level, RSSI signal strength, and device status.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001:\u0001\u0011B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\r\u0010\u000eJ\u001c\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00050\u000bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0010\u0010\u000eR\u0016\u0010\u0003\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0006\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0012"}, d2 = {"Lcom/project/insole/features/diagnostics/data/DiagnosticsDataSource;", "", "()V", "_deviceStatusFlow", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/features/diagnostics/data/DiagnosticsDataSource$DeviceStatus;", "deviceStatusFlow", "Lkotlinx/coroutines/flow/Flow;", "getDeviceStatusFlow", "()Lkotlinx/coroutines/flow/Flow;", "calibrateDevice", "Lkotlin/Result;", "", "calibrateDevice-IoAF18A", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDeviceStatus", "getDeviceStatus-IoAF18A", "DeviceStatus", "app_debug"})
public final class DiagnosticsDataSource {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.features.diagnostics.data.DiagnosticsDataSource.DeviceStatus> _deviceStatusFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<com.project.insole.features.diagnostics.data.DiagnosticsDataSource.DeviceStatus> deviceStatusFlow = null;
    
    @javax.inject.Inject()
    public DiagnosticsDataSource() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.project.insole.features.diagnostics.data.DiagnosticsDataSource.DeviceStatus> getDeviceStatusFlow() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\tH\u00c6\u0003J;\u0010\u0018\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\tH\u00c6\u0001J\u0013\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001c\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u001d\u001a\u00020\u0006H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\f\u00a8\u0006\u001e"}, d2 = {"Lcom/project/insole/features/diagnostics/data/DiagnosticsDataSource$DeviceStatus;", "", "batteryLevel", "", "rssiStrength", "connectionState", "", "firmwareVersion", "lastSyncTime", "", "(IILjava/lang/String;Ljava/lang/String;J)V", "getBatteryLevel", "()I", "getConnectionState", "()Ljava/lang/String;", "getFirmwareVersion", "getLastSyncTime", "()J", "getRssiStrength", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
    public static final class DeviceStatus {
        private final int batteryLevel = 0;
        private final int rssiStrength = 0;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String connectionState = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String firmwareVersion = null;
        private final long lastSyncTime = 0L;
        
        public DeviceStatus(int batteryLevel, int rssiStrength, @org.jetbrains.annotations.NotNull()
        java.lang.String connectionState, @org.jetbrains.annotations.NotNull()
        java.lang.String firmwareVersion, long lastSyncTime) {
            super();
        }
        
        public final int getBatteryLevel() {
            return 0;
        }
        
        public final int getRssiStrength() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getConnectionState() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getFirmwareVersion() {
            return null;
        }
        
        public final long getLastSyncTime() {
            return 0L;
        }
        
        public final int component1() {
            return 0;
        }
        
        public final int component2() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component4() {
            return null;
        }
        
        public final long component5() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.project.insole.features.diagnostics.data.DiagnosticsDataSource.DeviceStatus copy(int batteryLevel, int rssiStrength, @org.jetbrains.annotations.NotNull()
        java.lang.String connectionState, @org.jetbrains.annotations.NotNull()
        java.lang.String firmwareVersion, long lastSyncTime) {
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
}