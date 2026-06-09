package com.project.insole.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.insole.core.database.dao.HourlyStepDao
import com.project.insole.core.database.dao.StepDao
import com.project.insole.core.database.entity.HourlyStepEntity
import com.project.insole.core.database.entity.StepEntity

@Database(entities = [StepEntity::class, HourlyStepEntity::class], version = 2)
abstract class InsoleDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
    abstract fun hourlyStepDao(): HourlyStepDao
}
