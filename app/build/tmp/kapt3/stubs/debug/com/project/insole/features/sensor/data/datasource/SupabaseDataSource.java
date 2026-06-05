package com.project.insole.features.sensor.data.datasource;

/**
 * Pushes threshold alerts and sensor history to Supabase.
 * Handles remote persistence and analytics.
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\t\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J*\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\t\u001a\u00020\nH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u000b\u0010\fJ,\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u00062\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\nH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0011\u0010\u0012J$\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u000e0\u00062\u0006\u0010\u0014\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0015\u0010\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0017"}, d2 = {"Lcom/project/insole/features/sensor/data/datasource/SupabaseDataSource;", "", "supabaseClient", "Lcom/project/insole/core/network/SupabaseClient;", "(Lcom/project/insole/core/network/SupabaseClient;)V", "fetchSensorHistory", "Lkotlin/Result;", "", "Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;", "userId", "", "fetchSensorHistory-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "pushThresholdAlert", "", "message", "severity", "pushThresholdAlert-0E7RQCE", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadSensorData", "sensorData", "uploadSensorData-gIAlu-s", "(Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class SupabaseDataSource {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.core.network.SupabaseClient supabaseClient = null;
    
    @javax.inject.Inject()
    public SupabaseDataSource(@org.jetbrains.annotations.NotNull()
    com.project.insole.core.network.SupabaseClient supabaseClient) {
        super();
    }
}