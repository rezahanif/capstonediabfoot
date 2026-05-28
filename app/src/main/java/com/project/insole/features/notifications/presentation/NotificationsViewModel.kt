package com.project.insole.features.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.features.notifications.data.NotificationDataSource
import com.project.insole.features.notifications.domain.NotificationSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationSummary> = emptyList(),
    val unreadCount: Int = 0,
    val errorMessage: String? = null
)

/**
 * ViewModel for notifications screen.
 * Exposes immutable StateFlow for UI consumption.
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationDataSource: NotificationDataSource
) : ViewModel() {

    private val _notificationsState = MutableStateFlow(NotificationsUiState())
    val notificationsState: StateFlow<NotificationsUiState> = _notificationsState

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _notificationsState.value = _notificationsState.value.copy(isLoading = true)
            val result = notificationDataSource.getNotificationHistory()
            result.onSuccess { notifications ->
                val summaries = notifications.map {
                    NotificationSummary(
                        id = it.id,
                        title = it.title,
                        message = it.message,
                        type = it.severity,
                        timestamp = it.timestamp,
                        read = it.read
                    )
                }
                val unreadCount = summaries.count { !it.read }
                _notificationsState.value = NotificationsUiState(
                    isLoading = false,
                    notifications = summaries,
                    unreadCount = unreadCount
                )
            }.onFailure { exception ->
                _notificationsState.value = NotificationsUiState(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            notificationDataSource.clearOldNotifications(24)
            loadNotifications()
        }
    }
}
