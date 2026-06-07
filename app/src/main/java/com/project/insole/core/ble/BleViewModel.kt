package com.project.insole.core.ble

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.core.ble.model.BleDeviceState
import com.project.insole.features.sensor.domain.model.WalkState
import com.project.insole.features.sensor.domain.repository.SensorRepository
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
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _bleState = MutableStateFlow(BleUiState())
    val bleState: StateFlow<BleUiState> = _bleState

    init {
        observeBleState()
        observeSensorData()
        checkBluetoothEnabled()
    }

    private fun observeSensorData() {
        // Consolidated logic: observe parsed sensor data from repository
        viewModelScope.launch {
            sensorRepository.getSensorDataFlow().collect { sensorData ->
                if (sensorData != null) {
                    _bleState.update { state ->
                        state.copy(
                            leftTempC = sensorData.leftTemperature,
                            rightTempC = sensorData.rightTemperature,
                            leftPressure = sensorData.leftPressure.toFloat(),
                            rightPressure = sensorData.rightPressure.toFloat(),
                            totalSteps = sensorData.stepCount,
                            walkState = sensorData.walkState,
                            combinedAccelMag = sensorData.combinedAccelMag
                        )
                    }
                }
            }
        }

        // Keep raw sequence for UI bar charts
        viewModelScope.launch {
            sensorRepository.getRawLeftDataFlow().collect { raw ->
                _bleState.update { it.copy(leftRawData = raw, leftPacketSeq = it.leftPacketSeq + 1) }
            }
        }
        viewModelScope.launch {
            sensorRepository.getRawRightDataFlow().collect { raw ->
                _bleState.update { it.copy(rightRawData = raw, rightPacketSeq = it.rightPacketSeq + 1) }
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
