package com.project.insole.features.notifications.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Notifications screen showing all threshold alerts and system notifications.
 */
@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel) {
    val state = viewModel.notificationsState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (state.unreadCount > 0) {
            Text(
                text = "${state.unreadCount} unread",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (state.notifications.isEmpty()) {
            Text("No notifications", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(state.notifications) { notification ->
                    NotificationItemCard(notification)
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(notification: NotificationSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = formatTime(notification.timestamp),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
