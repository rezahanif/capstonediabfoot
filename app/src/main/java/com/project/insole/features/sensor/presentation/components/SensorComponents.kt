package com.project.insole.features.sensor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ========== SUMMARY CARDS (Dashboard) ==========

/**
 * Summary card showing FSR pressure status (Good/Bad).
 * If ANY FSR > threshold → Bad, else Good.
 */
@Composable
fun FsrStatusCard(fsrValues: List<Int>, modifier: Modifier = Modifier) {
    val maxPressure = fsrValues.maxOrNull() ?: 0
    val status = if (maxPressure > 150) "⚠️ High" else "✅ Good"
    val color = if (maxPressure > 150) Color.Red else Color.Green

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Pressure", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(status, style = MaterialTheme.typography.headlineSmall, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Max: $maxPressure", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * Summary card showing battery status.
 */
@Composable
fun BatteryStatusCard(battery: Int, modifier: Modifier = Modifier) {
    val status = when {
        battery > 50 -> "✅ Good"
        battery > 25 -> "⚠️ Low"
        else -> "🔴 Critical"
    }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Battery", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(status, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$battery%", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * Summary card showing connection quality status.
 */
@Composable
fun ConnectionStatusCard(quality: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Signal", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(quality, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("BLE RSSI", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * Summary card showing temperature status.
 */
@Composable
fun TemperatureCard(temp: Float, modifier: Modifier = Modifier) {
    val status = when {
        temp < 20 -> "❄️ Cold"
        temp in 20f..37f -> "✅ Normal"
        temp < 40 -> "🔥 High"
        else -> "🌡️ Critical"
    }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Temperature", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${String.format("%.1f", temp)}°C", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(status, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * Summary card showing daily steps.
 */
@Composable
fun StepsCard(steps: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Steps", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(steps.toString(), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("steps today", style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ========== DETAILED COMPONENTS (Monitoring) ==========

/**
 * Detailed grid showing each FSR sensor value independently.
 */
@Composable
fun FsrDetailedGrid(fsrValues: List<Int>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        fsrValues.forEachIndexed { index, value ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "FSR ${index + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(0.2f)
                )
                LinearProgressIndicator(
                    progress = value / 255f,
                    modifier = Modifier
                        .weight(0.6f)
                        .height(20.dp)
                )
                Text(
                    "$value",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(0.2f)
                )
            }
        }
    }
}

/**
 * Detailed temperature chart.
 */
@Composable
fun TemperatureDetailChart(temp: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Current: ${String.format("%.1f", temp)}°C")
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = (temp / 50f).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        // In real app: show temperature trend chart (e.g., last hour data)
    }
}

/**
 * Detailed connection quality graph.
 */
@Composable
fun ConnectionQualityGraph(quality: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Status: $quality")
        Spacer(modifier = Modifier.height(8.dp))
        // In real app: show RSSI signal strength over time
        Text("📊 Real-time RSSI graph here", style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * Detailed steps trend chart.
 */
@Composable
fun StepsTrendChart(steps: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Total: $steps steps")
        Spacer(modifier = Modifier.height(8.dp))
        // In real app: show steps per hour or per day
        Text("📈 Step trend chart here", style = MaterialTheme.typography.bodySmall)
    }
}
