package com.project.insole.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    /**
     * Atomic read-modify-write. The old pattern (SELECT → +diff → REPLACE) ran
     * as three suspend calls and lost increments when two step events landed
     * in the same window. UPDATE-first with INSERT fallback inside one
     * transaction makes the increment lossless.
     */
    @Query("UPDATE hourly_steps SET count = count + :diff WHERE date = :date AND hour = :hour")
    suspend fun incrementSteps(date: String, hour: Int, diff: Int): Int

    @Transaction
    suspend fun addSteps(date: String, hour: Int, diff: Int) {
        if (incrementSteps(date, hour, diff) == 0) {
            insertOrUpdate(HourlyStepEntity(date, hour, diff))
        }
    }

    @Query("DELETE FROM hourly_steps WHERE date NOT IN (SELECT DISTINCT date FROM hourly_steps ORDER BY date DESC LIMIT :daysLimit)")
    suspend fun deleteOldHourlySteps(daysLimit: Int = 8)
}
