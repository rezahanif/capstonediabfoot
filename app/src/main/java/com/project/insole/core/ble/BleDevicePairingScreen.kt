package com.project.insole.core.ble

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.ble.model.BleDeviceState
import com.project.insole.core.ble.InsoleUUIDs
import com.project.insole.core.presentation.components.InsoleToast
import com.project.insole.core.presentation.components.ToastData
import com.project.insole.core.presentation.components.ToastType
import com.project.insole.core.theme.DashboardColors
import kotlinx.coroutines.delay

@Composable
fun BleDevicePairingScreen(
    viewModel: BleViewModel,
    onConnected: () -> Unit,
    onBack: () -> Unit
) {
    val state = viewModel.bleState.collectAsState().value

    // Toast State Management
    var toastData by remember { mutableStateOf<ToastData?>(null) }

    // Observe connection states and show Toasts (Simplified for dual states)
    LaunchedEffect(state.leftDeviceState, state.rightDeviceState) {
        val lastState = if (state.leftDeviceState is BleDeviceState.Connecting || state.rightDeviceState is BleDeviceState.Connecting) {
            BleDeviceState.Connecting
        } else if (state.leftDeviceState is BleDeviceState.Connected || state.rightDeviceState is BleDeviceState.Connected) {
            BleDeviceState.Connected
        } else {
            BleDeviceState.Disconnected
        }

        when (lastState) {
            is BleDeviceState.Connecting -> {
                toastData = ToastData(
                    title = "Pairing",
                    description = "Connecting to insole device...",
                    type = ToastType.Pairing,
                    isVisible = true
                )
            }
            is BleDeviceState.Connected -> {
                toastData = ToastData(
                    title = "Success!",
                    description = "Insole connected successfully.",
                    type = ToastType.Success,
                    isVisible = true
                )
                delay(3000)
                toastData = toastData?.copy(isVisible = false)
            }
            else -> {}
        }
    }

    // Auto-refresh Bluetooth status when screen opens
    LaunchedEffect(Unit) {
        viewModel.checkBluetoothEnabled()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardColors.Background)
        ) {
            // ── Top Bar ───────────────────────────
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
                            // Identify the exact side of this specific device card
                            val side = remember(device.serviceUuid) { InsoleUUIDs.identifySide(device.serviceUuid) }

                            // Check connection states independently based on the identified hardware side
                            val isCurrentDeviceConnected = when (side) {
                                "LEFT" -> state.leftDeviceState == BleDeviceState.Connected
                                "RIGHT" -> state.rightDeviceState == BleDeviceState.Connected
                                else -> false
                            }

                            DeviceCard(
                                device = device,
                                isConnected = isCurrentDeviceConnected, // Target isolated flag state
                                onConnect = { viewModel.connectToDevice(device.address) }
                            )
                        }
                    }
                }

                // ── Bottom Action Button Guard Condition ──
                // Ensure the navigation button only appears when BOTH custom elements are securely paired
                if (state.leftDeviceState == BleDeviceState.Connected && state.rightDeviceState == BleDeviceState.Connected) {
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

        // Overlay Toast at the top
        toastData?.let { data ->
            InsoleToast(
                data = data,
                onDismiss = { toastData = toastData?.copy(isVisible = false) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
            )
        }
    }
}

@Composable
private fun DeviceCard(
    device: ScannedDevice,
    isConnected: Boolean,
    onConnect: () -> Unit
) {
    val side = remember(device.serviceUuid) { InsoleUUIDs.identifySide(device.serviceUuid) }
    val isRecognized = side != "UNKNOWN"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecognized) Color(0xFFF0F7FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRecognized) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.name,
                        color = DashboardColors.Navy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (isRecognized) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = if (side == "LEFT") DashboardColors.Brand else Color(0xFFE91E63),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = side,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = device.address,
                    color = DashboardColors.TextMuted,
                    fontSize = 12.sp
                )
                
                if (device.serviceUuid != null) {
                    Text(
                        text = "UUID: ${device.serviceUuid.take(8)}...",
                        color = DashboardColors.TextLightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            
            Button(
                onClick = onConnect,
                enabled = !isConnected,
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) DashboardColors.Green else DashboardColors.Brand
                )
            ) {
                Text(if (isConnected) "Paired" else "Pair", fontSize = 12.sp)
            }
        }
    }
}
