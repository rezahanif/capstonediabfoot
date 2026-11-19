package com.project.insole.features.sensor.domain.repository

import com.project.insole.core.ble.model.BleDeviceState
import com.project.insole.core.database.entity.DailyFootMetricEntity
import com.project.insole.features.sensor.domain.model.InsoleSensorData
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

    // TD-7: Local Daily Clinical Foot Metrics
    fun getDailyFootMetricsFlow(startDate: String): Flow<List<DailyFootMetricEntity>>
    suspend fun getDailyFootMetricsSince(startDate: String): List<DailyFootMetricEntity>
    suspend fun getDailyFootMetricForDate(date: String): DailyFootMetricEntity?

    // Clinical Scoring & Clean Data Foundation
    fun getClinicalScoringVariablesFlow(): Flow<com.project.insole.features.sensor.domain.model.ClinicalScoringVariables>
}
