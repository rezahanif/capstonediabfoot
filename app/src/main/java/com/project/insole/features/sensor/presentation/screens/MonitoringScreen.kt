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
                isRecording = bleState.leftDeviceState == com.project.insole.core.ble.model.BleDeviceState.Connected ||
                            bleState.rightDeviceState == com.project.insole.core.ble.model.BleDeviceState.Connected
            )

            // 3 ── Plantar Pressure Map Card ──────────────────────────────
            PlantarPressureCard(
                rawBleLeft = bleState.leftRawData,
                rawBleRight = bleState.rightRawData,
                leftConnected = bleState.isLeftConnected,
                rightConnected = bleState.isRightConnected,
                leftPacketSeq = bleState.leftPacketSeq,
                rightPacketSeq = bleState.rightPacketSeq
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
                    label = "RIGHT FOOT (PEAK)",
                    temp = bleState.rightTempC,
                    modifier = Modifier.weight(1f)
                )
                SingleFootTempCard(
                    label = "LEFT FOOT (PEAK)",
                    temp = bleState.leftTempC,
                    modifier = Modifier.weight(1f)
                )
            }

            // 6 ── End Session Button ─────────────────────────────────────
            Button(
                onClick = { 
                    viewModel.endSession()
                    bleViewModel.disconnect() // Explicitly disconnect both devices
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 32.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBA1A1A),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.StopCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "End Session",
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
                text = "AUTO",
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
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "%.1f".format(temp),
                    color = DashboardColors.Navy,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "°C",
                    color = DashboardColors.TextMuted,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                )
            }
        }
    }
}
