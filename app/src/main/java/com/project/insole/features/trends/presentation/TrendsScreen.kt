package com.project.insole.features.trends.presentation.components

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
import com.project.insole.features.trends.presentation.TrendsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Trends screen showing medical summary by timeframe (daily, weekly, monthly).
 */
@Composable
fun TrendsScreen(viewModel: TrendsViewModel) {
    val state = viewModel.trendsState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Trends & Analytics",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Weekly Health Score
        HealthScoreCard(score = state.weeklyHealthScore)

        // Daily summaries
        Text(
            text = "Last 7 Days",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        if (state.dailySummaries.isEmpty()) {
            Text("No data available", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(state.dailySummaries) { daily ->
                    DailySummaryCard(daily)
                }
            }
        }
    }
}

@Composable
private fun HealthScoreCard(score: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Weekly Health Score",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = String.format("%.1f/100", score),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = when {
                    score >= 80 -> "Excellent foot health"
                    score >= 60 -> "Good, monitor pressure zones"
                    else -> "Needs attention - review alerts"
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun DailySummaryCard(summary: com.project.insole.features.trends.data.DailySummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = formatDate(summary.date),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Steps: ${summary.totalSteps} | Avg Pressure: ${summary.avgPressure}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Alerts: ${summary.pressureAlerts} pressure, ${summary.temperatureAlerts} temp",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
