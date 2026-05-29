package com.project.insole.features.sensor.data.datasource;

/**
 * Parses raw bytes from ESP32 into Kotlin data classes.
 * Connected to InsoleBleManager to receive real BLE data.
 * This is the ONLY place where BLE byte parsing happens.
 * All other layers receive clean domain models.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fJ\u000e\u0010\u0010\u001a\u00020\u00072\u0006\u0010\u000e\u001a\u00020\u000fR\u0016\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0011"}, d2 = {"Lcom/project/insole/features/sensor/data/datasource/BleSensorDataSource;", "", "bleManager", "Lcom/project/insole/core/ble/InsoleBleManager;", "(Lcom/project/insole/core/ble/InsoleBleManager;)V", "_sensorDataFlow", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;", "sensorDataFlow", "Lkotlinx/coroutines/flow/Flow;", "getSensorDataFlow", "()Lkotlinx/coroutines/flow/Flow;", "onBleCharacteristicChanged", "", "rawData", "", "parseRawBleData", "app_debug"})
@android.annotation.SuppressLint(value = {"MissingPermission"})
public final class BleSensorDataSource {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.core.ble.InsoleBleManager bleManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.features.sensor.domain.model.InsoleSensorData> _sensorDataFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<com.project.insole.features.sensor.domain.model.InsoleSensorData> sensorDataFlow = null;
    
    @javax.inject.Inject()
    public BleSensorDataSource(@org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.InsoleBleManager bleManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.project.insole.features.sensor.domain.model.InsoleSensorData> getSensorDataFlow() {
        return null;
    }
    
    /**
     * Parses raw BLE characteristic data (Comma-separated String) into sensor readings.
     * Format: "accX,accY,accZ,gyroX,gyroY,gyroZ,pressure,temperature"
     */
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.features.sensor.domain.model.InsoleSensorData parseRawBleData(@org.jetbrains.annotations.NotNull()
    byte[] rawData) {
        return null;
    }
    
    public final void onBleCharacteristicChanged(@org.jetbrains.annotations.NotNull()
    byte[] rawData) {
    }
}