package com.project.insole.features.sensor.data.repository;

/**
 * Repository is the single source of truth for sensor data.
 * Combines BLE real-time data with Supabase remote data.
 * Exposes immutable StateFlow to presentation layer.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u0010\u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\tH\u0016J\u001e\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\nH\u0096@\u00a2\u0006\u0004\b\u000f\u0010\u0010J$\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00120\f2\u0006\u0010\u0013\u001a\u00020\u0014H\u0096@\u00a2\u0006\u0004\b\u0015\u0010\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/project/insole/features/sensor/data/repository/SensorRepositoryImpl;", "Lcom/project/insole/features/sensor/data/repository/SensorRepository;", "bleSensorDataSource", "Lcom/project/insole/features/sensor/data/datasource/BleSensorDataSource;", "supabaseDataSource", "Lcom/project/insole/features/sensor/data/datasource/SupabaseDataSource;", "<init>", "(Lcom/project/insole/features/sensor/data/datasource/BleSensorDataSource;Lcom/project/insole/features/sensor/data/datasource/SupabaseDataSource;)V", "getSensorDataFlow", "Lkotlinx/coroutines/flow/Flow;", "Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;", "uploadSensorData", "Lkotlin/Result;", "", "sensorData", "uploadSensorData-gIAlu-s", "(Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchSensorHistory", "", "userId", "", "fetchSensorHistory-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class SensorRepositoryImpl implements com.project.insole.features.sensor.data.repository.SensorRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.data.datasource.BleSensorDataSource bleSensorDataSource = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.data.datasource.SupabaseDataSource supabaseDataSource = null;
    
    @javax.inject.Inject()
    public SensorRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.data.datasource.BleSensorDataSource bleSensorDataSource, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.data.datasource.SupabaseDataSource supabaseDataSource) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.project.insole.features.sensor.domain.model.InsoleSensorData> getSensorDataFlow() {
        return null;
    }
}