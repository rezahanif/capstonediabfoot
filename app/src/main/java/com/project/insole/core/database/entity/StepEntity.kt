package com.project.insole.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps")
data class StepEntity(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val count: Int
)
