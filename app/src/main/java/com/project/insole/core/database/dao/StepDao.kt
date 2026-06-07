package com.project.insole.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.insole.core.database.entity.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM steps WHERE date = :date LIMIT 1")
    suspend fun getStepsForDate(date: String): StepEntity?

    @Query("SELECT * FROM steps WHERE date = :date LIMIT 1")
    fun getStepsFlowForDate(date: String): Flow<StepEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSteps(stepEntity: StepEntity)

    @Query("""
        DELETE FROM steps 
        WHERE date NOT IN (
            SELECT date FROM steps 
            ORDER BY date DESC 
            LIMIT :limit
        )
    """)
    suspend fun deleteOldSteps(limit: Int = 62)
}
