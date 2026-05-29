package com.project.insole.features.sensor.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.insole.core.theme.DashboardColors
import com.project.insole.features.sensor.presentation.SensorViewModel
import com.project.insole.features.sensor.presentation.components.*

/**
 * Dashboard screen showing SUMMARY view of all sensor features.
 * Redesigned from the Figma CAPSTONE node 11-2164.
 */
@Composable
fun DashboardScreen(
    viewModel: SensorViewModel,
    onNavigateToPairing: () -> Unit
) {
    val state = viewModel.sensorState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // 1 ── Top App Bar ────────────────────────────────────────────────
        DashboardTopBar()

        // 2 ── Greeting / Header ──────────────────────────────────────────
        GreetingSection(
            userName = "Reza", // In real app: get from Auth state
            lastCheckTime = "10:42 AM" // In real app: get from sensor state timestamp
        )

        // 3 ── Status Card ────────────────────────────────────────────────
        SystemStatusCard()

        // 4 ── Insole Battery Cards ───────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InsoleBatteryCard(
                modifier = Modifier.weight(1f),
                side = "LEFT INSOLE",
                batteryPct = state.batteryLevel,
            )
            InsoleBatteryCard(
                modifier = Modifier.weight(1f),
                side = "RIGHT INSOLE",
                batteryPct = state.batteryLevel, // Assuming same for now or update model
            )
        }

        // 5 ── Metrics Cards (Temperature | Steps) ───────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TemperatureMetricCard(
                modifier = Modifier.weight(1f),
                leftTempC = state.temperature,
                rightTempC = state.temperature, // Placeholder
            )
            StepsMetricCard(
                modifier = Modifier.weight(1f),
                stepCount = state.stepCount,
                stepGoal = 10000,
            )
        }

        // 6 ── Sync CTA ───────────────────────────────────────────────────
        SyncDataButton(onClick = onNavigateToPairing) // Reusing Sync button for Pairing for now
    }
}
