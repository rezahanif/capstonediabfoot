package com.project.insole.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists patient-facing notifications and guidance alert history in Room.
 * Records clinical guidance events emitted by PromptRuleEngine.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String, // UUID string
    val promptId: String,       // e.g. "THERMAL_LEFT", "PRESSURE_RIGHT"
    val title: String,
    val message: String,
    val severity: String,       // "ATTENTION", "INFO"
    val foot: String?,          // "LEFT", "RIGHT", or null
    val timestamp: Long,
    val thresholdVersion: String = "v1",
    val read: Boolean = false
)
