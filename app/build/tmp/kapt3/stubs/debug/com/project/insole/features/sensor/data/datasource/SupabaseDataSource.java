package com.project.insole.features.sensor.data.datasource;

/**
 * Pushes threshold alerts and sensor history to Supabase.
 * Handles remote persistence and analytics.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0004\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u001e\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0004\b\t\u0010\nJ&\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\rH\u0086@\u00a2\u0006\u0004\b\u000f\u0010\u0010J$\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00120\u00052\u0006\u0010\u0013\u001a\u00020\rH\u0086@\u00a2\u0006\u0004\b\u0014\u0010\u0015\u00a8\u0006\u0016"}, d2 = {"Lcom/project/insole/features/sensor/data/datasource/SupabaseDataSource;", "", "<init>", "()V", "uploadSensorData", "Lkotlin/Result;", "", "sensorData", "Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;", "uploadSensorData-gIAlu-s", "(Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "pushThresholdAlert", "message", "", "severity", "pushThresholdAlert-0E7RQCE", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchSensorHistory", "", "userId", "fetchSensorHistory-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class SupabaseDataSource {
    
    @javax.inject.Inject()
    public SupabaseDataSource() {
        super();
    }
}