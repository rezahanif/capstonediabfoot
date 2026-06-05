package com.project.insole.features.sensor.data.datasource;

/**
 * Collects raw BLE data from InsoleBleManager and aggregates it into domain models.
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\u000b\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\u0017\u001a\u00020\n2\b\u0010\u0018\u001a\u0004\u0018\u00010\u00072\b\u0010\u0019\u001a\u0004\u0018\u00010\u0007H\u0002J\u0018\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002R\u0016\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\t\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0019\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0019\u0010\u0011\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0010R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0010\u00a8\u0006 "}, d2 = {"Lcom/project/insole/features/sensor/data/datasource/BleSensorDataSource;", "", "bleManager", "Lcom/project/insole/core/ble/InsoleBleManager;", "(Lcom/project/insole/core/ble/InsoleBleManager;)V", "_rawLeftDataFlow", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_rawRightDataFlow", "_sensorDataFlow", "Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;", "currentLeftData", "currentRightData", "rawLeftDataFlow", "Lkotlinx/coroutines/flow/Flow;", "getRawLeftDataFlow", "()Lkotlinx/coroutines/flow/Flow;", "rawRightDataFlow", "getRawRightDataFlow", "scope", "Lkotlinx/coroutines/CoroutineScope;", "sensorDataFlow", "getSensorDataFlow", "aggregateSensorData", "leftRaw", "rightRaw", "onBleCharacteristicChanged", "", "rawData", "", "isLeft", "", "app_debug"})
@android.annotation.SuppressLint(value = {"MissingPermission"})
public final class BleSensorDataSource {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.core.ble.InsoleBleManager bleManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.features.sensor.domain.model.InsoleSensorData> _sensorDataFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<com.project.insole.features.sensor.domain.model.InsoleSensorData> sensorDataFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _rawLeftDataFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.lang.String> rawLeftDataFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _rawRightDataFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.lang.String> rawRightDataFlow = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentLeftData;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentRightData;
    
    @javax.inject.Inject()
    public BleSensorDataSource(@org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.InsoleBleManager bleManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.project.insole.features.sensor.domain.model.InsoleSensorData> getSensorDataFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.lang.String> getRawLeftDataFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.lang.String> getRawRightDataFlow() {
        return null;
    }
    
    private final void onBleCharacteristicChanged(byte[] rawData, boolean isLeft) {
    }
    
    private final com.project.insole.features.sensor.domain.model.InsoleSensorData aggregateSensorData(java.lang.String leftRaw, java.lang.String rightRaw) {
        return null;
    }
}