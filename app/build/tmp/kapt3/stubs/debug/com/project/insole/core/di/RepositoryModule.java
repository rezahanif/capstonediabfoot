package com.project.insole.core.di;

@dagger.Module()
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\'J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\'\u00a8\u0006\f"}, d2 = {"Lcom/project/insole/core/di/RepositoryModule;", "", "<init>", "()V", "bindSensorRepository", "Lcom/project/insole/features/sensor/data/repository/SensorRepository;", "sensorRepositoryImpl", "Lcom/project/insole/features/sensor/data/repository/SensorRepositoryImpl;", "bindTrackingSensorRepository", "Lcom/project/insole/features/tracking/data/repository/SensorRepository;", "trackingRepositoryImpl", "Lcom/project/insole/features/tracking/data/repository/SensorRepositoryImpl;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class RepositoryModule {
    
    public RepositoryModule() {
        super();
    }
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.project.insole.features.sensor.data.repository.SensorRepository bindSensorRepository(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.data.repository.SensorRepositoryImpl sensorRepositoryImpl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.project.insole.features.tracking.data.repository.SensorRepository bindTrackingSensorRepository(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.tracking.data.repository.SensorRepositoryImpl trackingRepositoryImpl);
}