package com.project.insole.features.trends.data

import com.project.insole.features.sensor.domain.model.InsoleSensorData
import javax.inject.Inject

/**
 * Data source for sensor trends data.
 *
 * The fabricated `getDailySummary` (hardcoded 5000 steps) and
 * `getWeeklyHealthScore` (hardcoded 85) were deleted — TD-4: the Trends tab
 * now reads real step history from Room via TrendsViewModel, and no
 * invented score or number may be presented to the patient.
 * `fetchSensorDataByTimeframe` remains the honest remote-fetch scaffold for
 * when the cloud API exists (the cloud is a dumb store — see cloud/README).
 */
class TrendsDataSource @Inject constructor() {

    /**
     * Fetches sensor data for a specific timeframe.
     */
    suspend fun fetchSensorDataByTimeframe(
        startTime: Long,
        endTime: Long
    ): Result<List<InsoleSensorData>> {
        return try {
            // Fetch from cloud backend (scaffold — not wired yet)
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
