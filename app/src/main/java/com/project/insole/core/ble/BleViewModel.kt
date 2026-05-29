package com.project.insole.core.ble

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.core.ble.model.BleDeviceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BleUiState(
    val isBluetoothEnabled: Boolean = false,
    val isScanning: Boolean = false,
    val scannedDevices: List<ScannedDevice> = emptyList(),
    val deviceState: BleDeviceState = BleDeviceState.Disconnected,
    val connectedDeviceName: String = "",
    val errorMessage: String? = null
)

/**
 * ViewModel for BLE scanning and connection management.
 * Used in setup/pairing screens to discover and connect to insole devices.
 */
@HiltViewModel
class BleViewModel @Inject constructor(
    private val bleManager: InsoleBleManager
) : ViewModel() {

    private val _bleState = MutableStateFlow(BleUiState())
    val bleState: StateFlow<BleUiState> = _bleState

    init {
        observeBleState()
        checkBluetoothEnabled()
    }

    private fun observeBleState() {
        viewModelScope.launch {
            bleManager.isBluetoothEnabledFlow.collect { enabled ->
                _bleState.value = _bleState.value.copy(isBluetoothEnabled = enabled)
            }
        }

        viewModelScope.launch {
            bleManager.scannedDevices.collect { devices ->
                _bleState.value = _bleState.value.copy(scannedDevices = devices)
            }
        }

        viewModelScope.launch {
            bleManager.isScanning.collect { isScanning ->
                _bleState.value = _bleState.value.copy(isScanning = isScanning)
            }
        }

        viewModelScope.launch {
            bleManager.bleDeviceState.collect { state ->
                _bleState.value = _bleState.value.copy(
                    deviceState = state,
                    errorMessage = (state as? BleDeviceState.Error)?.message
                )
            }
        }
    }

    fun checkBluetoothEnabled() {
        val enabled = bleManager.isBluetoothEnabled()
        _bleState.value = _bleState.value.copy(isBluetoothEnabled = enabled)
    }

    fun startScanning() {
        checkBluetoothEnabled()
        if (_bleState.value.isBluetoothEnabled) {
            bleManager.startScanning()
        } else {
            _bleState.value = _bleState.value.copy(
                errorMessage = "Bluetooth is not enabled"
            )
        }
    }

    fun stopScanning() {
        bleManager.stopScanning()
    }

    fun connectToDevice(deviceAddress: String, deviceName: String) {
        bleManager.connect(deviceAddress)
        _bleState.value = _bleState.value.copy(connectedDeviceName = deviceName)
    }

    fun disconnect() {
        bleManager.disconnect()
        _bleState.value = _bleState.value.copy(connectedDeviceName = "")
    }
}
