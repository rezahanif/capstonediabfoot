package com.project.insole.features.sensor.data.repository

import com.project.insole.core.ble.model.BleDeviceState
import com.project.insole.features.sensor.data.datasource.BleSensorDataSource
import com.project.insole.features.sensor.data.datasource.SupabaseDataSource
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

    override fun getHourlyStepsFlow(): Flow<List<Int>> = bleSensorDataSource.hourlyStepsFlow

    override fun getConnectionState(): Flow<BleDeviceState> {
        return combine(
            bleManager.leftDeviceState,
            bleManager.rightDeviceState
        ) { left, right ->
            when {
                // If either has an error, show error
                left is BleDeviceState.Error -> left
                right is BleDeviceState.Error -> right
                
                // If both are connected, we are fully connected
                left is BleDeviceState.Connected && right is BleDeviceState.Connected -> BleDeviceState.Connected
                
                // If either is connecting/discovering, we are in a transitional state
                left is BleDeviceState.Connecting || right is BleDeviceState.Connecting -> BleDeviceState.Connecting
                left is BleDeviceState.Discovering || right is BleDeviceState.Discovering -> BleDeviceState.Discovering
                
                // If one is connected and other disconnected, we are "partially" connected (could map to Connecting or a new state)
                // For now, treat partial as Connecting to keep the UI in a "working on it" state
                left is BleDeviceState.Connected || right is BleDeviceState.Connected -> BleDeviceState.Connecting
                
                // Both disconnected
                else -> BleDeviceState.Disconnected
            }
        }
    }

    override fun disconnect() {
        bleManager.disconnect()
    }

    override fun resetSessionStats() {
        bleSensorDataSource.resetSessionStats()
    }

    override suspend fun uploadSensorData(sensorData: InsoleSensorData): Result<Unit> {
        return supabaseDataSource.uploadSensorData(sensorData)
    }

    override suspend fun fetchSensorHistory(userId: String): Result<List<InsoleSensorData>> {
        return supabaseDataSource.fetchSensorHistory(userId)
    }
}
