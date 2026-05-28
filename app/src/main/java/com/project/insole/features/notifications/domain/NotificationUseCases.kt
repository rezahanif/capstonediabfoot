package com.project.insole.features.notifications.domain

/**
 * Pure Kotlin domain use cases for notification management.
 * No Android or BLE dependencies - only business logic.
 */

class FilterNotificationsByTypeUseCase {
    operator fun invoke(notifications: List<NotificationSummary>, type: String): List<NotificationSummary> {
        return notifications.filter { it.type == type }
    }
}

class SortNotificationsByTimeUseCase {
    operator fun invoke(notifications: List<NotificationSummary>): List<NotificationSummary> {
        return notifications.sortedByDescending { it.timestamp }
    }
}

class MarkNotificationAsReadUseCase {
    operator fun invoke(notificationId: String): NotificationSummary {
        // Mark as read in repository
        return NotificationSummary(notificationId, "Title", "Message", "INFO", System.currentTimeMillis(), true)
    }
}

data class NotificationSummary(
    val id: String,
    val title: String,
    val message: String,
    val type: String,  // "PRESSURE", "BATTERY", "CONNECTION", "TEMPERATURE"
    val timestamp: Long,
    val read: Boolean = false
)
