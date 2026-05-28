package com.project.insole.features.sensor.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.insole.features.sensor.presentation.SensorViewModel
import com.project.insole.features.sensor.presentation.components.FsrDetailedGrid
import com.project.insole.features.sensor.presentation.components.TemperatureDetailChart
import com.project.insole.features.sensor.presentation.components.ConnectionQualityGraph
import com.project.insole.features.sensor.presentation.components.StepsTrendChart

/**
 * Monitoring screen showing DETAILED view of each sensor independently.
 * Displays each FSR sensor value, temperature trend, connection quality, and step trend.
 */
@Composable
fun MonitoringScreen(viewModel: SensorViewModel) {
    val state = viewModel.sensorState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Monitoring",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Each FSR sensor independently
        Text(
            text = "Pressure Sensors (FSR)",
            style = MaterialTheme.typography.titleMedium
        )
        FsrDetailedGrid(fsrValues = state.fsrValues)

        // Temperature trend
        Text(
            text = "Temperature Trend",
            style = MaterialTheme.typography.titleMedium
        )
        TemperatureDetailChart(temp = state.temperature)

        // Connection quality
        Text(
            text = "Connection Quality",
            style = MaterialTheme.typography.titleMedium
        )
        ConnectionQualityGraph(quality = state.connectionQuality)

        // Steps trend
        Text(
            text = "Daily Steps",
            style = MaterialTheme.typography.titleMedium
        )
        StepsTrendChart(steps = state.stepCount)
    }
}
