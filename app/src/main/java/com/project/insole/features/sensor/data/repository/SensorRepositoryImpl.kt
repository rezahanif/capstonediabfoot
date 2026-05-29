package com.project.insole.features.sensor.data.repository

import com.project.insole.features.sensor.data.datasource.BleSensorDataSource
import com.project.insole.features.sensor.data.datasource.SupabaseDataSource
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Repository is the single source of truth for sensor data.
 * Combines BLE real-time data with Supabase remote data.
 * Exposes immutable StateFlow to presentation layer.
 */
class SensorRepositoryImpl @Inject constructor(
    private val bleSensorDataSource: BleSensorDataSource,
    private val supabaseDataSource: SupabaseDataSource,
    private val bleManager: com.project.insole.core.ble.InsoleBleManager
) : SensorRepository {

    override fun getSensorDataFlow(): Flow<InsoleSensorData?> {
        // Combine BLE live data with any remote cached data
        return bleSensorDataSource.sensorDataFlow
    }

    override fun getConnectionState(): Flow<com.project.insole.core.ble.model.BleDeviceState> {
        return bleManager.bleDeviceState
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

interface SensorRepository {
    fun getSensorDataFlow(): Flow<InsoleSensorData?>
    fun getConnectionState(): Flow<com.project.insole.core.ble.model.BleDeviceState>
    fun disconnect()
    suspend fun uploadSensorData(sensorData: InsoleSensorData): Result<Unit>
    suspend fun fetchSensorHistory(userId: String): Result<List<InsoleSensorData>>
}
