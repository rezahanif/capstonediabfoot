package com.project.insole.core.di

import com.project.insole.features.sensor.data.repository.SensorRepository as SensorRepo
import com.project.insole.features.sensor.data.repository.SensorRepositoryImpl as SensorRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSensorRepository(
        sensorRepositoryImpl: SensorRepoImpl
    ): SensorRepo
}
