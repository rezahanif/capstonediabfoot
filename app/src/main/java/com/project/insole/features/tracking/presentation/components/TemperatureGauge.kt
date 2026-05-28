package com.project.insole.features.tracking.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Stateless composable that displays current temperature reading.
 * Shows a visual gauge of temperature intensity.
 */
@Composable
fun TemperatureGauge(temperature: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Temperature: ${String.format("%.1f", temperature)}°C")
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = (temperature / 50f).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}
