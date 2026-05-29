package com.project.insole.core.ble

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.ble.model.BleDeviceState
import com.project.insole.core.theme.DashboardColors

@Composable
fun BleDevicePairingScreen(
    viewModel: BleViewModel,
    onConnected: () -> Unit,
    onBack: () -> Unit
) {
    val state = viewModel.bleState.collectAsState().value

    // Auto-refresh Bluetooth status when screen opens
    LaunchedEffect(Unit) {
        viewModel.checkBluetoothEnabled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardColors.Background)
    ) {
        // ── Top Bar (Matching Live Monitor style) ───────────────────────────
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
                text = "Pair Insole Device",
                color = DashboardColors.Navy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { 
                    viewModel.checkBluetoothEnabled()
                    viewModel.startScanning() 
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = DashboardColors.Navy
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // ── Bluetooth Status Card ───────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Bluetooth Status",
                            color = DashboardColors.TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isBluetoothEnabled) "Enabled" else "Disabled",
                            color = if (state.isBluetoothEnabled) DashboardColors.Green else Color.Red,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = if (state.isBluetoothEnabled) DashboardColors.Brand else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // ── Devices Section ─────────────────────────────────────────────
            Text(
                text = "AVAILABLE DEVICES",
                color = DashboardColors.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )

            if (state.scannedDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isScanning) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = DashboardColors.Brand)
                            Spacer(Modifier.height(12.dp))
                            Text("Searching for your Insole...", color = DashboardColors.TextMuted)
                        }
                    } else {
                        Button(
                            onClick = { 
                                viewModel.checkBluetoothEnabled()
                                viewModel.startScanning() 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DashboardColors.Brand)
                        ) {
                            Text("Start Scanning")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(state.scannedDevices) { device ->
                        DeviceCard(
                            device = device,
                            isSelected = state.connectedDeviceName == device.name,
                            onConnect = { viewModel.connectToDevice(device.address, device.name) }
                        )
                    }
                }
            }

            // ── Bottom Action Button ────────────────────────────────────────
            if (state.deviceState == BleDeviceState.Connected) {
                Button(
                    onClick = onConnected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DashboardColors.GreenMint)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Proceed to Dashboard", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: ScannedDevice,
    isSelected: Boolean,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    color = DashboardColors.Navy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = device.address,
                    color = DashboardColors.TextMuted,
                    fontSize = 12.sp
                )
            }
            
            Button(
                onClick = onConnect,
                enabled = !isSelected,
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) DashboardColors.Green else DashboardColors.Brand
                )
            ) {
                Text(if (isSelected) "Connected" else "Connect", fontSize = 12.sp)
            }
        }
    }
}
