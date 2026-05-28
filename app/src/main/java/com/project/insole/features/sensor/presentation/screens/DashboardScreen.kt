package com.project.insole.features.sensor.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.insole.features.sensor.presentation.SensorViewModel
import com.project.insole.features.sensor.presentation.components.FsrStatusCard
import com.project.insole.features.sensor.presentation.components.BatteryStatusCard
import com.project.insole.features.sensor.presentation.components.ConnectionStatusCard
import com.project.insole.features.sensor.presentation.components.TemperatureCard
import com.project.insole.features.sensor.presentation.components.StepsCard

/**
 * Dashboard screen showing SUMMARY view of all 4 sensor features.
 * Displays FSR pressure, battery, connection, temperature, and steps as summary cards.
 */
@Composable
fun DashboardScreen(viewModel: SensorViewModel) {
    val state = viewModel.sensorState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 4 Summary Cards (Good/Bad status)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FsrStatusCard(
                fsrValues = state.fsrValues,
                modifier = Modifier.weight(1f)
            )
            BatteryStatusCard(
                battery = state.batteryLevel,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ConnectionStatusCard(
                quality = state.connectionQuality,
                modifier = Modifier.weight(1f)
            )
            TemperatureCard(
                temp = state.temperature,
                modifier = Modifier.weight(1f)
            )
        }

        StepsCard(
            steps = state.stepCount,
            modifier = Modifier.fillMaxWidth()
        )

        // Alerts section
        if (state.thresholdAlerts.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("⚠️ Alerts", style = MaterialTheme.typography.titleMedium)
                    state.thresholdAlerts.forEach { alert ->
                        Text(alert, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
