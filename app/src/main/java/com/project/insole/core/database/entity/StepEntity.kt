package com.project.insole.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps")
data class StepEntity(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val count: Int,
    // Per-foot split so left/right metrics survive an app restart.
    // Defaults keep rows written before v3 valid (split unknown → 0).
    val leftCount: Int = 0,
    val rightCount: Int = 0
)
