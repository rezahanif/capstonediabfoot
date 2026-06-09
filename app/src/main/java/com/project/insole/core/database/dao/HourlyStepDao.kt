package com.project.insole.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.insole.core.database.entity.HourlyStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HourlyStepDao {
    @Query("SELECT * FROM hourly_steps WHERE date = :date ORDER BY hour ASC")
    fun getHourlyStepsForDate(date: String): Flow<List<HourlyStepEntity>>

    @Query("SELECT * FROM hourly_steps WHERE date = :date AND hour = :hour LIMIT 1")
    suspend fun getStepsForHour(date: String, hour: Int): HourlyStepEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: HourlyStepEntity)

    @Query("DELETE FROM hourly_steps WHERE date NOT IN (SELECT DISTINCT date FROM hourly_steps ORDER BY date DESC LIMIT :daysLimit)")
    suspend fun deleteOldHourlySteps(daysLimit: Int = 8)
}
