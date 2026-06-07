package com.project.insole.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.insole.core.database.dao.StepDao
import com.project.insole.core.database.entity.StepEntity

@Database(entities = [StepEntity::class], version = 1)
abstract class InsoleDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
}
