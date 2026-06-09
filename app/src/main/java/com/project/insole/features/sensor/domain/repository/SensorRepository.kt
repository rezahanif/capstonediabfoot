package com.project.insole.features.sensor.domain.repository

import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.core.ble.model.BleDeviceState
import kotlinx.coroutines.flow.Flow

/**
 * Repository is the single source of truth for sensor data.
 */
interface SensorRepository {
    fun getSensorDataFlow(): Flow<InsoleSensorData?>
    fun getRawLeftDataFlow(): Flow<String?>
    fun getRawRightDataFlow(): Flow<String?>
    fun getHourlyStepsFlow(): Flow<List<Int>>
    fun getConnectionState(): Flow<BleDeviceState>
    fun disconnect()
    fun resetSessionStats()
    suspend fun uploadSensorData(sensorData: InsoleSensorData): Result<Unit>
    suspend fun fetchSensorHistory(userId: String): Result<List<InsoleSensorData>>
}
