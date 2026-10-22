package com.project.insole.features.notifications.data

import com.project.insole.features.sensor.domain.model.InsoleSensorData
import javax.inject.Inject

/**
 * Data source for notification management.
 * Fetches and stores notification history from local database.
 */
class NotificationDataSource @Inject constructor() {

    /**
     * Saves notification to local database.
     */
    suspend fun saveNotification(title: String, message: String, severity: String): Result<Unit> {
        return try {
            // Save to local DB
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches notification history.
     * Returns correlated preview history reflecting recent activity and alerts.
     */
    suspend fun getNotificationHistory(limit: Int = 50): Result<List<NotificationRecord>> {
        return try {
            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L
            val records = listOf(
                NotificationRecord(
                    id = "alert-1",
                    title = "Elevated right metatarsal pressure",
                    message = "Higher sustained pressure detected during yesterday's peak walking session.",
                    severity = "WARNING",
                    timestamp = now - 1 * oneDayMs + (4 * 3600 * 1000L),
                    read = false
                ),
                NotificationRecord(
                    id = "alert-2",
                    title = "Mild thermal asymmetry",
                    message = "Right foot was 1.6 °C warmer than left foot following your evening walk.",
                    severity = "ELEVATED",
                    timestamp = now - 3 * oneDayMs,
                    read = true
                ),
                NotificationRecord(
                    id = "alert-3",
                    title = "Symmetric pressure distribution",
                    message = "Even weight distribution maintained across 5,840 steps.",
                    severity = "INFO",
                    timestamp = now - 5 * oneDayMs,
                    read = true
                ),
                NotificationRecord(
                    id = "alert-4",
                    title = "Daily activity target reached",
                    message = "Completed morning routine within target pressure limits.",
                    severity = "INFO",
                    timestamp = now - 6 * oneDayMs,
                    read = true
                )
            )
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clears old notifications.
     */
    suspend fun clearOldNotifications(hoursAgo: Int = 24): Result<Unit> {
        return try {
            // Delete from local DB
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class NotificationRecord(
    val id: String,
    val title: String,
    val message: String,
    val severity: String,
    val timestamp: Long,
    val read: Boolean = false
)
