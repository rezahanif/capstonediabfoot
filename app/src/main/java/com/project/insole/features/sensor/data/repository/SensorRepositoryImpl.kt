package com.project.insole.features.sensor.data.repository

import com.project.insole.features.sensor.data.datasource.BleSensorDataSource
import com.project.insole.features.sensor.data.datasource.SupabaseDataSource
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository is the single source of truth for sensor data.
 * Combines BLE real-time data with Supabase remote data.
 */
class SensorRepositoryImpl @Inject constructor(
    private val bleSensorDataSource: BleSensorDataSource,
    private val supabaseDataSource: SupabaseDataSource,
    private val bleManager: com.project.insole.core.ble.InsoleBleManager
) : SensorRepository {

    override fun getSensorDataFlow(): Flow<InsoleSensorData?> = bleSensorDataSource.sensorDataFlow

    override fun getRawLeftDataFlow(): Flow<String?> = bleSensorDataSource.rawLeftDataFlow
    
    override fun getRawRightDataFlow(): Flow<String?> = bleSensorDataSource.rawRightDataFlow

    override fun getConnectionState(): Flow<com.project.insole.core.ble.model.BleDeviceState> {
        return bleManager.leftDeviceState 
    }

    override fun disconnect() {
        bleManager.disconnect()
    }

    override suspend fun uploadSensorData(sensorData: InsoleSensorData): Result<Unit> {
        return supabaseDataSource.uploadSensorData(sensorData)
    }

    override suspend fun fetchSensorHistory(userId: String): Result<List<InsoleSensorData>> {
        return supabaseDataSource.fetchSensorHistory(userId)
    }
}
