package com.project.insole.features.sensor.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors
import com.project.insole.features.sensor.presentation.SensorViewModel
import com.project.insole.features.sensor.presentation.components.*

/**
 * Monitoring screen showing DETAILED view of each sensor independently.
 * Redesigned from the Figma CAPSTONE node 11:2347.
 */
@Composable
fun MonitoringScreen(
    viewModel: SensorViewModel,
    bleViewModel: com.project.insole.core.ble.BleViewModel,
    onBack: () -> Unit = {}
) {
    val state = viewModel.sensorState.collectAsState().value
    val bleState = bleViewModel.bleState.collectAsState().value
    val isConnected = bleState.isLeftConnected || bleState.isRightConnected
    val hasActiveSession = isConnected && (state.sessionDurationSeconds > 0 || state.isConnected)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardColors.Background)
    ) {
        // 1 ── Top App Bar ────────────────────────────────────────────────
        MonitoringTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 2 ── Timer Section ──────────────────────────────────────────
            SessionTimerSection(
                durationSeconds = state.sessionDurationSeconds,
                isRecording = isConnected,
                isConnected = isConnected
            )

            // 3 ── Plantar Pressure Map Card ──────────────────────────────
            PlantarPressureCard(
                pressureMap = state.pressureMap,
                leftConnected = bleState.isLeftConnected,
                rightConnected = bleState.isRightConnected,
                leftPeakPressure = bleState.leftPeakPressure,
                rightPeakPressure = bleState.rightPeakPressure
            )



            // 4 ── Temperature Asymmetry Card ─────────────────────────────
            TempAsymmetryCard(
                leftTemp = bleState.leftTempC,
                rightTemp = bleState.rightTempC
            )

            // 5 ── Foot Temperature Row ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(114.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SingleFootTempCard(
                    label = "LEFT FOOT (PEAK)",
                    temp = bleState.leftPeakTempC,
                    modifier = Modifier.weight(1f)
                )
                SingleFootTempCard(
                    label = "RIGHT FOOT (PEAK)",
                    temp = bleState.rightPeakTempC,
                    modifier = Modifier.weight(1f)
                )
            }

            // 6 ── Reset Live Session Button ──────────────────────────────
            Button(
                onClick = { 
                    viewModel.endSession()
                    bleViewModel.resetSessionStats() // Resets live session peaks & timer
                },
                enabled = hasActiveSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 32.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DashboardColors.Brand,
                    contentColor = DashboardColors.CardBg,
                    disabledContainerColor = DashboardColors.ProgressTrack,
                    disabledContentColor = DashboardColors.TextLightGray
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (hasActiveSession) 2.dp else 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.StopCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (hasActiveSession) "Reset Live Session" else "Monitoring Inactive",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MonitoringTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(73.dp)
            .background(DashboardColors.Background)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = DashboardColors.Navy
            )
        }
        
        Text(
            text = "Live Monitor",
            color = DashboardColors.Navy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = DashboardColors.Navy,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                text = "LIVE",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SingleFootTempCard(
    label: String,
    temp: Float,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor, subtitle) = when {
        temp <= 0f -> Triple("No Data", DashboardColors.TextLightGray, "Awaiting reading")
        temp >= 35.0f -> Triple("Elevated", DashboardColors.Yellow, "Warm skin status")
        else -> Triple("Optimal", DashboardColors.GreenMint, "Normal thermal range")
    }

    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = DashboardColors.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = subtitle,
                color = DashboardColors.TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
