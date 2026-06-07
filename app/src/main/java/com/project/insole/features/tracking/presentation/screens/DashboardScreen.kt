package com.project.insole.features.tracking.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.insole.features.sensor.presentation.SensorViewModel
import com.project.insole.features.sensor.presentation.components.PlantarPressureCard
import com.project.insole.features.sensor.presentation.components.StepsMetricCard
import com.project.insole.features.tracking.presentation.components.*

/**
 * Dashboard screen showing SUMMARY view of all sensor features.
 * Redesigned from the Figma CAPSTONE node 11-2164.
 * Moved to tracking feature for better organization.
 */
@Composable
fun DashboardScreen(
    viewModel: SensorViewModel,
    bleViewModel: com.project.insole.core.ble.BleViewModel,
    onNavigateToPairing: () -> Unit,
    onLogout: () -> Unit
) {
    val state = viewModel.sensorState.collectAsState().value
    val bleState = bleViewModel.bleState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // 1 ── Top App Bar ────────────────────────────────────────────────
        DashboardTopBar(onLogout = onLogout)

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
                isConnected = bleState.isLeftConnected, // ✅ Real state
                onClick = onNavigateToPairing
            )
            InsoleBatteryCard(
                modifier = Modifier.weight(1f),
                side = "RIGHT INSOLE",
                batteryPct = state.batteryLevel, 
                isConnected = bleState.isRightConnected, // ✅ Real state
                onClick = onNavigateToPairing
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
                leftTempC = bleState.leftTempC,
                rightTempC = bleState.rightTempC,
                leftConnected = bleState.isLeftConnected,
                rightConnected = bleState.isRightConnected,
            )
            StepsMetricCard(
                modifier = Modifier.weight(1f),
                stepCount = bleState.totalSteps,
                walkState = bleState.walkState,
                leftConnected = bleState.isLeftConnected,
                rightConnected = bleState.isRightConnected,
                combinedAccelMag = bleState.combinedAccelMag,
                stepGoal = 10000
            )
        }

        // 6 ── Refresh CTA ───────────────────────────────────────────────────
        SyncDataButton(
            onClick = { viewModel.refreshData() },
            isRefreshing = state.isRefreshing
        )
    }
}
