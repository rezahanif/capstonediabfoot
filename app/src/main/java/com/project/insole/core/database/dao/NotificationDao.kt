package com.project.insole.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.insole.core.database.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentNotificationsFlow(limit: Int = 50): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentNotifications(limit: Int = 50): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET read = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM notifications WHERE id NOT IN (SELECT id FROM notifications ORDER BY timestamp DESC LIMIT :keepLimit)")
    suspend fun deleteOldNotifications(keepLimit: Int = 100)
}
