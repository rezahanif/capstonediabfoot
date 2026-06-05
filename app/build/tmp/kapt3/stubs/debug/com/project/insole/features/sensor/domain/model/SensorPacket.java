package com.project.insole.features.sensor.domain.model;

/**
 * Low-level sensor packet from a single insole.
 * Format: "ax,ay,az,gx,gy,gz,pressure,temperature"
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u001b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0087\b\u0018\u0000 %2\u00020\u0001:\u0001%BU\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003JY\u0010\u001d\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u001e\u001a\u00020\u001f2\b\u0010 \u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010!\u001a\u00020\"H\u00d6\u0001J\t\u0010#\u001a\u00020$H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\rR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\rR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\rR\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\rR\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\rR\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\r\u00a8\u0006&"}, d2 = {"Lcom/project/insole/features/sensor/domain/model/SensorPacket;", "", "accelX", "", "accelY", "accelZ", "gyroX", "gyroY", "gyroZ", "pressure", "temperature", "(FFFFFFFF)V", "getAccelX", "()F", "getAccelY", "getAccelZ", "getGyroX", "getGyroY", "getGyroZ", "getPressure", "getTemperature", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "", "other", "hashCode", "", "toString", "", "Companion", "app_debug"})
public final class SensorPacket {
    private final float accelX = 0.0F;
    private final float accelY = 0.0F;
    private final float accelZ = 0.0F;
    private final float gyroX = 0.0F;
    private final float gyroY = 0.0F;
    private final float gyroZ = 0.0F;
    private final float pressure = 0.0F;
    private final float temperature = 0.0F;
    @org.jetbrains.annotations.NotNull()
    public static final com.project.insole.features.sensor.domain.model.SensorPacket.Companion Companion = null;
    
    public SensorPacket(float accelX, float accelY, float accelZ, float gyroX, float gyroY, float gyroZ, float pressure, float temperature) {
        super();
    }
    
    public final float getAccelX() {
        return 0.0F;
    }
    
    public final float getAccelY() {
        return 0.0F;
    }
    
    public final float getAccelZ() {
        return 0.0F;
    }
    
    public final float getGyroX() {
        return 0.0F;
    }
    
    public final float getGyroY() {
        return 0.0F;
    }
    
    public final float getGyroZ() {
        return 0.0F;
    }
    
    public final float getPressure() {
        return 0.0F;
    }
    
    public final float getTemperature() {
        return 0.0F;
    }
    
    public SensorPacket() {
        super();
    }
    
    public final float component1() {
        return 0.0F;
    }
    
    public final float component2() {
        return 0.0F;
    }
    
    public final float component3() {
        return 0.0F;
    }
    
    public final float component4() {
        return 0.0F;
    }
    
    public final float component5() {
        return 0.0F;
    }
    
    public final float component6() {
        return 0.0F;
    }
    
    public final float component7() {
        return 0.0F;
    }
    
    public final float component8() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.features.sensor.domain.model.SensorPacket copy(float accelX, float accelY, float accelZ, float gyroX, float gyroY, float gyroZ, float pressure, float temperature) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/project/insole/features/sensor/domain/model/SensorPacket$Companion;", "", "()V", "fromBleString", "Lcom/project/insole/features/sensor/domain/model/SensorPacket;", "raw", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Parses: "ax,ay,az,gx,gy,gz,pressureKPa,tempCelsius"
         * Returns null if the string is malformed or too short.
         */
        @org.jetbrains.annotations.Nullable()
        public final com.project.insole.features.sensor.domain.model.SensorPacket fromBleString(@org.jetbrains.annotations.NotNull()
        java.lang.String raw) {
            return null;
        }
    }
}