package com.project.insole.features.sensor.data.repository

import com.project.insole.core.ble.model.BleDeviceState
import com.project.insole.core.database.dao.DailyFootMetricDao
import com.project.insole.core.database.entity.DailyFootMetricEntity
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
    private val bleManager: com.project.insole.core.ble.InsoleBleManager,
    private val dailyFootMetricDao: DailyFootMetricDao
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
                // If at least one insole is connected, telemetry stream and live session are active
                left is BleDeviceState.Connected || right is BleDeviceState.Connected -> BleDeviceState.Connected
                
                // If neither is connected, check for transitional connecting/discovering states
                left is BleDeviceState.Connecting || right is BleDeviceState.Connecting -> BleDeviceState.Connecting
                left is BleDeviceState.Discovering || right is BleDeviceState.Discovering -> BleDeviceState.Discovering
                
                // If neither is connected and there is an error, report error
                left is BleDeviceState.Error -> left
                right is BleDeviceState.Error -> right
                
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

    override fun getDailyFootMetricsFlow(startDate: String): Flow<List<DailyFootMetricEntity>> {
        return dailyFootMetricDao.getMetricsFlowSince(startDate)
    }

    override suspend fun getDailyFootMetricsSince(startDate: String): List<DailyFootMetricEntity> {
        return dailyFootMetricDao.getMetricsSince(startDate)
    }

    override suspend fun getDailyFootMetricForDate(date: String): DailyFootMetricEntity? {
        return dailyFootMetricDao.getMetricsForDate(date)
    }

    override fun getClinicalScoringVariablesFlow(): Flow<com.project.insole.features.sensor.domain.model.ClinicalScoringVariables> {
        return bleSensorDataSource.clinicalScoringFlow
    }
}
