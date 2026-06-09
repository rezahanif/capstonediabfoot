package com.project.insole.core.database.entity

import androidx.room.Entity

@Entity(
    tableName = "hourly_steps",
    primaryKeys = ["date", "hour"]
)
data class HourlyStepEntity(
    val date: String, // Format: YYYY-MM-DD
    val hour: Int,   // 0-23
    val count: Int   // Steps taken in this specific hour
)
