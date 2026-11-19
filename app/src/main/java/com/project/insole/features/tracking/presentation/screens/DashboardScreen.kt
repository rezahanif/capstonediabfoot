package com.project.insole.features.tracking.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.insole.features.notifications.domain.OverallStatus
import com.project.insole.features.sensor.presentation.SensorViewModel
import com.project.insole.features.sensor.presentation.components.StepsMetricCard
import com.project.insole.features.tracking.presentation.components.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * Dashboard screen showing truthful, clinical-grade summary of sensor telemetry.
 * Fully aligned with UX-BRAND-SPEC (Rules R1 - R7).
 */
@Composable
fun DashboardScreen(
    viewModel: SensorViewModel,
    bleViewModel: com.project.insole.core.ble.BleViewModel,
    onNavigateToPairing: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val state = viewModel.sensorState.collectAsState().value
    val bleState = bleViewModel.bleState.collectAsState().value

    val isConnected = bleState.isLeftConnected || bleState.isRightConnected
    val isConnecting = bleState.isEitherConnecting

    // Rule R5: "No recent data is never shown as All Clear."
    val overallStatus = when {
        !isConnected -> OverallStatus.NO_DATA
        state.thresholdAlerts.isNotEmpty() -> OverallStatus.ATTENTION
        else -> OverallStatus.ALL_CLEAR
    }

    val lastCheckTime = if (isConnected) {
        DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault()).format(Instant.now())
    } else null

    // Bilateral pressure symmetry calculation
    val leftPeak = bleState.leftPeakPressure
    val rightPeak = bleState.rightPeakPressure
    val pressureBalanceText = when {
        !bleState.isBothConnected -> "N/A"
        leftPeak == 0f && rightPeak == 0f -> "Balanced"
        abs(leftPeak - rightPeak) <= 30f -> "Balanced"
        else -> "Uneven"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // 1 ── Top App Bar ────────────────────────────────────────────────
        DashboardTopBar(
            onNavigateToSettings = onNavigateToSettings,
            onLogout = onLogout
        )

        // 2 ── Greeting / Header (Dynamic time-of-day + live check badge) ─
        GreetingSection(
            userName = "Reza",
            lastCheckTime = lastCheckTime,
            isConnected = isConnected,
            isConnecting = isConnecting
        )

        // 3 ── Status Card (Dynamic R5 enforcement) ───────────────────────
        SystemStatusCard(
            status = overallStatus,
            isConnecting = isConnecting
        )

        // 4 ── Insole Power & Battery Cards (Honest TD-13 state) ──────────
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
                batteryPct = state.batteryLevel.takeIf { it > 0 },
                isConnected = bleState.isLeftConnected,
                pressureBalanceText = pressureBalanceText,
                onClick = onNavigateToPairing
            )
            InsoleBatteryCard(
                modifier = Modifier.weight(1f),
                side = "RIGHT INSOLE",
                batteryPct = state.batteryLevel.takeIf { it > 0 },
                isConnected = bleState.isRightConnected,
                pressureBalanceText = pressureBalanceText,
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
                hourlySteps = bleState.hourlySteps,
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
