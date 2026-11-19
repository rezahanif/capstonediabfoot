package com.project.insole.features.notifications.data

import com.project.insole.core.database.dao.NotificationDao
import com.project.insole.core.database.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for notification management.
 * Fetches and stores patient guidance notification history in local Room database.
 */
@Singleton
class NotificationDataSource @Inject constructor(
    private val notificationDao: NotificationDao
) {

    /**
     * Observes recent notification history from Room as a reactive Flow.
     */
    fun getNotificationsFlow(limit: Int = 50): Flow<List<NotificationRecord>> {
        return notificationDao.getRecentNotificationsFlow(limit).map { entities ->
            entities.map { it.toRecord() }
        }
    }

    /**
     * Fetches notification history from Room database.
     */
    suspend fun getNotificationHistory(limit: Int = 50): Result<List<NotificationRecord>> {
        return try {
            val entities = notificationDao.getRecentNotifications(limit)
            Result.success(entities.map { it.toRecord() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves a guidance or alert notification event to Room.
     */
    suspend fun saveNotification(
        promptId: String,
        title: String,
        message: String,
        severity: String,
        foot: String? = null,
        thresholdVersion: String = "v1"
    ): Result<Unit> {
        return try {
            val entity = NotificationEntity(
                id = UUID.randomUUID().toString(),
                promptId = promptId,
                title = title,
                message = message,
                severity = severity,
                foot = foot,
                timestamp = System.currentTimeMillis(),
                thresholdVersion = thresholdVersion,
                read = false
            )
            notificationDao.insertNotification(entity)
            notificationDao.deleteOldNotifications(keepLimit = 100)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marks a notification as read.
     */
    suspend fun markAsRead(id: String): Result<Unit> {
        return try {
            notificationDao.markAsRead(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Trims older notifications keeping the most recent [keepLimit] records.
     */
    suspend fun clearOldNotifications(keepLimit: Int = 50): Result<Unit> {
        return try {
            notificationDao.deleteOldNotifications(keepLimit)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun NotificationEntity.toRecord(): NotificationRecord = NotificationRecord(
        id = id,
        promptId = promptId,
        title = title,
        message = message,
        severity = severity,
        foot = foot,
        timestamp = timestamp,
        thresholdVersion = thresholdVersion,
        read = read
    )
}

data class NotificationRecord(
    val id: String,
    val promptId: String = "",
    val title: String,
    val message: String,
    val severity: String,
    val foot: String? = null,
    val timestamp: Long,
    val thresholdVersion: String = "v1",
    val read: Boolean = false
)
