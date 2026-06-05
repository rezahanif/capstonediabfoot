package com.project.insole.core.ble

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.core.ble.model.BleDeviceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BleUiState(
    val isBluetoothEnabled: Boolean = false,
    val isScanning: Boolean = false,
    val scannedDevices: List<ScannedDevice> = emptyList(),
    val leftDeviceState: BleDeviceState = BleDeviceState.Disconnected,
    val rightDeviceState: BleDeviceState = BleDeviceState.Disconnected,
    val leftRawData: String? = null,
    val rightRawData: String? = null,
    val leftPacketSeq: Long = 0L,
    val rightPacketSeq: Long = 0L,
    val leftTempC: Float = 0f,
    val rightTempC: Float = 0f,
    val leftPressure: Float = 0f,
    val rightPressure: Float = 0f,
    val totalSteps: Int = 0,
    val walkState: com.project.insole.features.sensor.presentation.components.WalkState = com.project.insole.features.sensor.presentation.components.WalkState.STANDING,
    val errorMessage: String? = null,
) {
    // Convenience helpers used across screens
    val isLeftConnected get() = leftDeviceState == BleDeviceState.Connected
    val isRightConnected get() = rightDeviceState == BleDeviceState.Connected

    val isBothConnected get() = isLeftConnected && isRightConnected

    val isEitherConnecting get() =
        leftDeviceState == BleDeviceState.Connecting ||
        rightDeviceState == BleDeviceState.Connecting ||
        leftDeviceState == BleDeviceState.Discovering ||
        rightDeviceState == BleDeviceState.Discovering
}

/**
 * ViewModel for BLE scanning and connection management.
 * Enhanced to support Dual-Insole pairing and activity-scoped lifecycle persistence.
 */
@HiltViewModel
class BleViewModel @Inject constructor(
    private val bleManager: InsoleBleManager
) : ViewModel() {

    // ✅ Lives in ViewModel — survives recomposition and navigation
    private val stepCounter = com.project.insole.features.sensor.presentation.components.DualFootStepCounter()

    private val _bleState = MutableStateFlow(BleUiState())
    val bleState: StateFlow<BleUiState> = _bleState

    init {
        observeBleState()
        checkBluetoothEnabled()

        // ✅ Route BLE data and parse for both sides
        bleManager.setOnCharacteristicChangedListener { data, isLeft, address ->
            val raw = String(data, Charsets.UTF_8)
            android.util.Log.d("BLE_DATA", "${if (isLeft) "LEFT" else "RIGHT"} [$address] raw: '$raw'")
            
            val packet = com.project.insole.features.sensor.domain.model.SensorPacket.fromBleString(raw)
            if (packet == null) {
                android.util.Log.e("BLE_DATA", "  Failed to parse packet from $address")
            } else {
                android.util.Log.d("BLE_DATA", "  Parsed: Temp=${packet.temperature}, Press=${packet.pressure}")
            }

            // ✅ Process steps in ViewModel, not composable
            val newTotalSteps = packet?.let {
                if (isLeft) stepCounter.processLeft(it)
                else stepCounter.processRight(it)
            }

            _bleState.update { state ->
                if (isLeft) state.copy(
                    leftRawData   = raw,
                    leftPacketSeq = state.leftPacketSeq + 1,
                    leftTempC     = packet?.temperature ?: state.leftTempC,
                    leftPressure  = packet?.pressure    ?: state.leftPressure,
                    totalSteps    = newTotalSteps       ?: state.totalSteps,
                    walkState     = stepCounter.dominantState
                ) else state.copy(
                    rightRawData   = raw,
                    rightPacketSeq = state.rightPacketSeq + 1,
                    rightTempC     = packet?.temperature ?: state.rightTempC,
                    rightPressure  = packet?.pressure    ?: state.rightPressure,
                    totalSteps     = newTotalSteps       ?: state.totalSteps,
                    walkState      = stepCounter.dominantState
                )
            }
        }
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
            bleManager.isScanning.collect { scanning ->
                _bleState.value = _bleState.value.copy(isScanning = scanning)
            }
        }
        // Observe left and right independently
        viewModelScope.launch {
            bleManager.leftDeviceState.collect { state ->
                _bleState.value = _bleState.value.copy(
                    leftDeviceState = state,
                    errorMessage = (state as? BleDeviceState.Error)?.message
                        ?: _bleState.value.errorMessage
                )
            }
        }
        viewModelScope.launch {
            bleManager.rightDeviceState.collect { state ->
                _bleState.value = _bleState.value.copy(
                    rightDeviceState = state,
                    errorMessage = (state as? BleDeviceState.Error)?.message
                        ?: _bleState.value.errorMessage
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
            _bleState.value = _bleState.value.copy(errorMessage = "Bluetooth is not enabled")
        }
    }

    fun stopScanning() {
        bleManager.stopScanning()
    }

    fun connectToDevice(deviceAddress: String) {
        bleManager.connect(deviceAddress)
    }

    fun disconnect(deviceAddress: String? = null) {
        bleManager.disconnect(deviceAddress)
    }
}
