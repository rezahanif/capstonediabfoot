package com.project.insole.features.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Settings screen for user preferences and device configuration.
 */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state = viewModel.settingsState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Pressure Threshold Setting
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Pressure Alert Threshold", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${state.pressureThreshold} (0-255)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Slider(
                    value = state.pressureThreshold.toFloat(),
                    onValueChange = { viewModel.updatePressureThreshold(it.toInt()) },
                    valueRange = 100f..255f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }

        // Temperature Threshold Setting
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Temperature Alert Threshold", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${String.format("%.1f", state.temperatureThreshold)}°C",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Slider(
                    value = state.temperatureThreshold,
                    onValueChange = { viewModel.updateTemperatureThreshold(it) },
                    valueRange = 20f..50f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }

        // Notifications Toggle
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Enable Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = state.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications(it) }
                )
            }
        }

        // About Section
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("App Version", style = MaterialTheme.typography.titleMedium)
                Text("1.0.0", style = MaterialTheme.typography.bodySmall)
                Text("Build: 001", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
