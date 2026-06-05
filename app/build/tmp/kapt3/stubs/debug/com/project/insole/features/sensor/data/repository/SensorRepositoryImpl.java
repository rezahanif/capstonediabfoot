package com.project.insole.features.sensor.data.repository;

/**
 * Repository is the single source of truth for sensor data.
 * Combines BLE real-time data with Supabase remote data.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\b\u0010\t\u001a\u00020\nH\u0016J*\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\f2\u0006\u0010\u000f\u001a\u00020\u0010H\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0011\u0010\u0012J\u000e\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014H\u0016J\u0010\u0010\u0016\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\u0014H\u0016J\u0010\u0010\u0017\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\u0014H\u0016J\u0010\u0010\u0018\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000e0\u0014H\u0016J$\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\n0\f2\u0006\u0010\u001a\u001a\u00020\u000eH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001b\u0010\u001cR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u001d"}, d2 = {"Lcom/project/insole/features/sensor/data/repository/SensorRepositoryImpl;", "Lcom/project/insole/features/sensor/domain/repository/SensorRepository;", "bleSensorDataSource", "Lcom/project/insole/features/sensor/data/datasource/BleSensorDataSource;", "supabaseDataSource", "Lcom/project/insole/features/sensor/data/datasource/SupabaseDataSource;", "bleManager", "Lcom/project/insole/core/ble/InsoleBleManager;", "(Lcom/project/insole/features/sensor/data/datasource/BleSensorDataSource;Lcom/project/insole/features/sensor/data/datasource/SupabaseDataSource;Lcom/project/insole/core/ble/InsoleBleManager;)V", "disconnect", "", "fetchSensorHistory", "Lkotlin/Result;", "", "Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;", "userId", "", "fetchSensorHistory-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getConnectionState", "Lkotlinx/coroutines/flow/Flow;", "Lcom/project/insole/core/ble/model/BleDeviceState;", "getRawLeftDataFlow", "getRawRightDataFlow", "getSensorDataFlow", "uploadSensorData", "sensorData", "uploadSensorData-gIAlu-s", "(Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class SensorRepositoryImpl implements com.project.insole.features.sensor.domain.repository.SensorRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.data.datasource.BleSensorDataSource bleSensorDataSource = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.data.datasource.SupabaseDataSource supabaseDataSource = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.core.ble.InsoleBleManager bleManager = null;
    
    @javax.inject.Inject()
    public SensorRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.data.datasource.BleSensorDataSource bleSensorDataSource, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.data.datasource.SupabaseDataSource supabaseDataSource, @org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.InsoleBleManager bleManager) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.project.insole.features.sensor.domain.model.InsoleSensorData> getSensorDataFlow() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.lang.String> getRawLeftDataFlow() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.lang.String> getRawRightDataFlow() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.project.insole.core.ble.model.BleDeviceState> getConnectionState() {
        return null;
    }
    
    @java.lang.Override()
    public void disconnect() {
    }
}