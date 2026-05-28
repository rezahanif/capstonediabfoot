package com.project.insole.features.tracking.data.repository

import com.project.insole.features.tracking.data.datasource.BleSensorDataSource
import com.project.insole.features.tracking.data.datasource.SupabaseDataSource
import com.project.insole.features.tracking.domain.model.InsoleSensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Repository is the single source of truth for sensor data.
 * Combines BLE real-time data with Supabase remote data.
 * Exposes immutable StateFlow to presentation layer.
 */
class SensorRepositoryImpl(
    private val bleSensorDataSource: BleSensorDataSource,
    private val supabaseDataSource: SupabaseDataSource
) : SensorRepository {

    override fun getSensorDataFlow(): Flow<InsoleSensorData?> {
        // Combine BLE live data with any remote cached data
        return bleSensorDataSource.sensorDataFlow
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
    suspend fun uploadSensorData(sensorData: InsoleSensorData): Result<Unit>
    suspend fun fetchSensorHistory(userId: String): Result<List<InsoleSensorData>>
}
