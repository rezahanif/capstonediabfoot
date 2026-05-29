package com.project.insole.features.tracking.data.repository;

/**
 * Repository is the single source of truth for sensor data.
 * Combines BLE real-time data with Supabase remote data.
 * Exposes immutable StateFlow to presentation layer.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J*\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010\u000b\u001a\u00020\fH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\r\u0010\u000eJ\u0010\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\u0010H\u0016J$\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\b2\u0006\u0010\u0013\u001a\u00020\nH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0014\u0010\u0015R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0016"}, d2 = {"Lcom/project/insole/features/tracking/data/repository/SensorRepositoryImpl;", "Lcom/project/insole/features/tracking/data/repository/SensorRepository;", "bleSensorDataSource", "Lcom/project/insole/features/tracking/data/datasource/BleSensorDataSource;", "supabaseDataSource", "Lcom/project/insole/features/tracking/data/datasource/SupabaseDataSource;", "(Lcom/project/insole/features/tracking/data/datasource/BleSensorDataSource;Lcom/project/insole/features/tracking/data/datasource/SupabaseDataSource;)V", "fetchSensorHistory", "Lkotlin/Result;", "", "Lcom/project/insole/features/tracking/domain/model/InsoleSensorData;", "userId", "", "fetchSensorHistory-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getSensorDataFlow", "Lkotlinx/coroutines/flow/Flow;", "uploadSensorData", "", "sensorData", "uploadSensorData-gIAlu-s", "(Lcom/project/insole/features/tracking/domain/model/InsoleSensorData;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class SensorRepositoryImpl implements com.project.insole.features.tracking.data.repository.SensorRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.tracking.data.datasource.BleSensorDataSource bleSensorDataSource = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.tracking.data.datasource.SupabaseDataSource supabaseDataSource = null;
    
    @javax.inject.Inject()
    public SensorRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.tracking.data.datasource.BleSensorDataSource bleSensorDataSource, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.tracking.data.datasource.SupabaseDataSource supabaseDataSource) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.project.insole.features.tracking.domain.model.InsoleSensorData> getSensorDataFlow() {
        return null;
    }
}