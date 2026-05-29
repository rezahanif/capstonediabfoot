package com.project.insole.features.tracking.data.datasource;

/**
 * Parses raw bytes from ESP32 into Kotlin data classes.
 * This is the ONLY place where BLE byte parsing happens.
 * All other layers receive clean domain models.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010 \n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rJ\u0016\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f2\u0006\u0010\f\u001a\u00020\rH\u0002J\u000e\u0010\u0011\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\rJ\u0010\u0010\u0012\u001a\u00020\u00102\u0006\u0010\f\u001a\u00020\rH\u0002J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\f\u001a\u00020\rH\u0002R\u0016\u0010\u0003\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0006\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u00a8\u0006\u0015"}, d2 = {"Lcom/project/insole/features/tracking/data/datasource/BleSensorDataSource;", "", "()V", "_sensorDataFlow", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/features/tracking/domain/model/InsoleSensorData;", "sensorDataFlow", "Lkotlinx/coroutines/flow/Flow;", "getSensorDataFlow", "()Lkotlinx/coroutines/flow/Flow;", "onBleCharacteristicChanged", "", "rawData", "", "parsePressure", "", "", "parseRawBleData", "parseStepCount", "parseTemperature", "", "app_debug"})
public final class BleSensorDataSource {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.features.tracking.domain.model.InsoleSensorData> _sensorDataFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<com.project.insole.features.tracking.domain.model.InsoleSensorData> sensorDataFlow = null;
    
    @javax.inject.Inject()
    public BleSensorDataSource() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.project.insole.features.tracking.domain.model.InsoleSensorData> getSensorDataFlow() {
        return null;
    }
    
    /**
     * Parses raw BLE characteristic data into sensor readings.
     */
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.features.tracking.domain.model.InsoleSensorData parseRawBleData(@org.jetbrains.annotations.NotNull()
    byte[] rawData) {
        return null;
    }
    
    private final java.util.List<java.lang.Integer> parsePressure(byte[] rawData) {
        return null;
    }
    
    private final float parseTemperature(byte[] rawData) {
        return 0.0F;
    }
    
    private final int parseStepCount(byte[] rawData) {
        return 0;
    }
    
    public final void onBleCharacteristicChanged(@org.jetbrains.annotations.NotNull()
    byte[] rawData) {
    }
}