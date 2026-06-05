package com.project.insole.core.ble

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.core.ble.model.BleDeviceState
import com.project.insole.features.sensor.domain.model.SensorPacket
import com.project.insole.features.sensor.domain.model.WalkState
import com.project.insole.features.sensor.domain.service.StepCounterService
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
    val walkState: WalkState = WalkState.STANDING,
    val combinedAccelMag: Float = 0f,
    val errorMessage: String? = null,
) {
    val isLeftConnected get() = leftDeviceState == BleDeviceState.Connected
    val isRightConnected get() = rightDeviceState == BleDeviceState.Connected
    val isBothConnected get() = isLeftConnected && isRightConnected
    val isEitherConnecting get() =
        leftDeviceState == BleDeviceState.Connecting ||
        rightDeviceState == BleDeviceState.Connecting ||
        leftDeviceState == BleDeviceState.Discovering ||
        rightDeviceState == BleDeviceState.Discovering
}

@HiltViewModel
class BleViewModel @Inject constructor(
    private val bleManager: InsoleBleManager,
    private val stepCounterService: StepCounterService
) : ViewModel() {

    private val _bleState = MutableStateFlow(BleUiState())
    val bleState: StateFlow<BleUiState> = _bleState

    init {
        observeBleState()
        observeTelemetry()
        checkBluetoothEnabled()
    }

    private fun observeTelemetry() {
        // Subscribe to the real telemetry flow - Fixed the dead pipeline bug
        viewModelScope.launch {
            bleManager.telemetryFlow.collect { packetData ->
                val raw = String(packetData.data, Charsets.UTF_8)
                val isLeft = packetData.isLeft
                
                android.util.Log.d("BLE_DATA", 
                    "${if (isLeft) "LEFT" else "RIGHT"} [${packetData.deviceAddress}] raw: '$raw'")

                val sensorPacket = SensorPacket.fromBleString(raw)
                if (sensorPacket == null) {
                    android.util.Log.e("BLE_DATA", "  Failed to parse packet from ${packetData.deviceAddress}")
                    return@collect
                }

                android.util.Log.d("BLE_DATA", 
                    "  Parsed: Temp=${sensorPacket.temperature}, Press=${sensorPacket.pressure}")

                // Process steps via Domain Service
                stepCounterService.processPacket(sensorPacket, isLeft)

                _bleState.update { state ->
                    if (isLeft) state.copy(
                        leftRawData   = raw,
                        leftPacketSeq = state.leftPacketSeq + 1,
                        leftTempC     = sensorPacket.temperature,
                        leftPressure  = sensorPacket.pressure,
                        totalSteps    = stepCounterService.totalSteps,
                        walkState     = stepCounterService.walkState,
                        combinedAccelMag = stepCounterService.combinedAccelMag
                    ) else state.copy(
                        rightRawData   = raw,
                        rightPacketSeq = state.rightPacketSeq + 1,
                        rightTempC     = sensorPacket.temperature,
                        rightPressure  = sensorPacket.pressure,
                        totalSteps     = stepCounterService.totalSteps,
                        walkState      = stepCounterService.walkState,
                        combinedAccelMag = stepCounterService.combinedAccelMag
                    )
                }
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
