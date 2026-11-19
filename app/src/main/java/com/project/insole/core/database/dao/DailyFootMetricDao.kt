package com.project.insole.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.insole.core.database.entity.DailyFootMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyFootMetricDao {
    @Query("SELECT * FROM daily_foot_metrics WHERE date = :date LIMIT 1")
    suspend fun getMetricsForDate(date: String): DailyFootMetricEntity?

    @Query("SELECT * FROM daily_foot_metrics WHERE date = :date LIMIT 1")
    fun getMetricsFlowForDate(date: String): Flow<DailyFootMetricEntity?>

    @Query("SELECT * FROM daily_foot_metrics WHERE date >= :startDate ORDER BY date ASC")
    suspend fun getMetricsSince(startDate: String): List<DailyFootMetricEntity>

    @Query("SELECT * FROM daily_foot_metrics WHERE date >= :startDate ORDER BY date ASC")
    fun getMetricsFlowSince(startDate: String): Flow<List<DailyFootMetricEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetrics(entity: DailyFootMetricEntity)

    @Query("DELETE FROM daily_foot_metrics WHERE date = :date")
    suspend fun deleteMetricsForDate(date: String)

    @Query("DELETE FROM daily_foot_metrics")
    suspend fun clearAllMetrics()

    @Query("""
        DELETE FROM daily_foot_metrics 
        WHERE date NOT IN (
            SELECT date FROM daily_foot_metrics 
            ORDER BY date DESC 
            LIMIT :limit
        )
    """)
    suspend fun deleteOldMetrics(limit: Int = 62)
}
