package com.project.insole.features.sensor.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J*\u0010\u0004\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00060\u00052\u0006\u0010\b\u001a\u00020\tH\u00a6@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\n\u0010\u000bJ\u000e\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rH&J\u0010\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\rH&J$\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00030\u00052\u0006\u0010\u0011\u001a\u00020\u0007H\u00a6@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0012\u0010\u0013\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0014"}, d2 = {"Lcom/project/insole/features/sensor/data/repository/SensorRepository;", "", "disconnect", "", "fetchSensorHistory", "Lkotlin/Result;", "", "Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;", "userId", "", "fetchSensorHistory-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getConnectionState", "Lkotlinx/coroutines/flow/Flow;", "Lcom/project/insole/core/ble/model/BleDeviceState;", "getSensorDataFlow", "uploadSensorData", "sensorData", "uploadSensorData-gIAlu-s", "(Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface SensorRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.project.insole.features.sensor.domain.model.InsoleSensorData> getSensorDataFlow();
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.project.insole.core.ble.model.BleDeviceState> getConnectionState();
    
    public abstract void disconnect();
}