package com.project.insole.core.ble

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.insole.core.ble.model.BleDeviceState

/**
 * BLE Device Pairing Screen - allows user to scan for and connect to insole devices.
 * Shows connection status and list of available devices.
 */
@Composable
fun BleDevicePairingScreen(
    viewModel: BleViewModel,
    onConnected: () -> Unit
) {
    val state = viewModel.bleState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Pair Insole Device",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Bluetooth Status
        BleStatusCard(
            isBluetoothEnabled = state.isBluetoothEnabled,
            connectionState = state.deviceState,
            connectedDeviceName = state.connectedDeviceName
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scan/Stop Button
        when {
            state.isScanning -> {
                Button(
                    onClick = { viewModel.stopScanning() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Stop Scanning")
                }
            }
            else -> {
                Button(
                    onClick = {
                        viewModel.checkBluetoothEnabled()
                        viewModel.startScanning()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Start Scanning")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Devices List
        Text(
            text = "Available Devices (${state.scannedDevices.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (state.scannedDevices.isEmpty()) {
            if (state.isScanning) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text(
                        "Searching for devices...",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                Text(
                    "No devices found. Enable Bluetooth and start scanning.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            LazyColumn {
                items(state.scannedDevices) { device ->
                    DeviceCard(
                        device = device,
                        isConnected = state.connectedDeviceName == device.name,
                        onConnect = { viewModel.connectToDevice(device.address, device.name) }
                    )
                }
            }
        }

        // Error Message
        if (state.errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "❌ ${state.errorMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Connected - proceed button
        if (state.deviceState == BleDeviceState.Connected) {
            Button(
                onClick = onConnected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(top = 16.dp)
            ) {
                Text("Proceed to Dashboard")
            }
        }
    }
}

@Composable
private fun BleStatusCard(
    isBluetoothEnabled: Boolean,
    connectionState: BleDeviceState,
    connectedDeviceName: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bluetooth", style = MaterialTheme.typography.labelMedium)
                Text(
                    if (isBluetoothEnabled) "✅ Enabled" else "❌ Disabled",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Connection", style = MaterialTheme.typography.labelMedium)
                Text(
                    when (connectionState) {
                        BleDeviceState.Disconnected -> "❌ Disconnected"
                        BleDeviceState.Connecting -> "⏳ Connecting..."
                        BleDeviceState.Connected -> "✅ Connected"
                        BleDeviceState.Discovering -> "🔍 Discovering..."
                        is BleDeviceState.Error -> "❌ Error"
                    },
                    style = MaterialTheme.typography.labelSmall
                )
            }

            if (connectedDeviceName.isNotEmpty()) {
                Text(
                    text = "Device: $connectedDeviceName",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: ScannedDevice,
    isConnected: Boolean,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "RSSI: ${device.rssi} dBm",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Button(
                onClick = onConnect,
                enabled = !isConnected,
                modifier = Modifier.height(40.dp)
            ) {
                Text(if (isConnected) "Connected" else "Connect")
            }
        }
    }
}

@Composable
private fun Spacer(modifier: Modifier) {
    androidx.compose.foundation.layout.Spacer(modifier = modifier)
}
