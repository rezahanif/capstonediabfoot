package com.project.insole.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.insole.core.database.dao.DailyFootMetricDao
import com.project.insole.core.database.dao.HourlyStepDao
import com.project.insole.core.database.dao.NotificationDao
import com.project.insole.core.database.dao.StepDao
import com.project.insole.core.database.entity.DailyFootMetricEntity
import com.project.insole.core.database.entity.HourlyStepEntity
import com.project.insole.core.database.entity.NotificationEntity
import com.project.insole.core.database.entity.StepEntity

@Database(
    entities = [
        StepEntity::class,
        HourlyStepEntity::class,
        DailyFootMetricEntity::class,
        NotificationEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class InsoleDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
    abstract fun hourlyStepDao(): HourlyStepDao
    abstract fun dailyFootMetricDao(): DailyFootMetricDao
    abstract fun notificationDao(): NotificationDao
}
