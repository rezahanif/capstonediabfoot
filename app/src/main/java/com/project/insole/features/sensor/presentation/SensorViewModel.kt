package com.project.insole.features.sensor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.core.ble.model.BleDeviceState
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.repository.SensorRepository
import com.project.insole.features.sensor.domain.usecase.AnalyzePressureThresholdUseCase
import com.project.insole.features.sensor.domain.usecase.MapPressureToGridUseCase
import com.project.insole.features.sensor.domain.usecase.ProcessStepCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SensorUiState(
    val isLoading: Boolean = false,
    val fsrValues: List<Int> = emptyList(),
    val temperature: Float = 0f,
    val stepCount: Int = 0,
    val batteryLevel: Int = 0,
    val connectionQuality: String = "Unknown",
    val thresholdAlerts: List<String> = emptyList(),
    val errorMessage: String? = null,
    val isConnected: Boolean = false,
    val sessionDurationSeconds: Long = 0L,
    val rawBleLeft: String? = null,
    val rawBleRight: String? = null,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class SensorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val mapPressureToGridUseCase: MapPressureToGridUseCase,
    private val processStepCountUseCase: ProcessStepCountUseCase,
    private val analyzePressureThresholdUseCase: AnalyzePressureThresholdUseCase
) : ViewModel() {

    private val _sensorState = MutableStateFlow(SensorUiState())
    val sensorState: StateFlow<SensorUiState> = _sensorState

    private var timerJob: Job? = null
    private var sessionStartTime: Long = 0L

    init {
        observeSensorData()
        observeConnectionState()
    }

    private fun observeSensorData() {
        viewModelScope.launch {
            sensorRepository.getSensorDataFlow().collect { sensorData ->
                if (sensorData != null) {
                    updateSensorState(sensorData)
                }
            }
        }
        
        viewModelScope.launch {
            sensorRepository.getRawLeftDataFlow().collect { rawData ->
                _sensorState.value = _sensorState.value.copy(rawBleLeft = rawData)
            }
        }

        viewModelScope.launch {
            sensorRepository.getRawRightDataFlow().collect { rawData ->
                _sensorState.value = _sensorState.value.copy(rawBleRight = rawData)
            }
        }
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            sensorRepository.getConnectionState().collect { state ->
                val isConnected = state == BleDeviceState.Connected
                
                if (isConnected) {
                    _sensorState.value = _sensorState.value.copy(isConnected = true)
                    startSessionTimer()
                } else {
                    stopSessionTimer()
                    // Reset UI data to default values upon disconnection
                    _sensorState.value = _sensorState.value.copy(
                        isConnected = false,
                        fsrValues = emptyList(),
                        temperature = 0f,
                        stepCount = 0,
                        batteryLevel = 0,
                        thresholdAlerts = emptyList(),
                        connectionQuality = "Disconnected"
                    )
                }
            }
        }
    }

    private fun startSessionTimer() {
        if (timerJob != null) return // Already running
        
        sessionStartTime = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (true) {
                val elapsed = (System.currentTimeMillis() - sessionStartTime) / 1000
                _sensorState.value = _sensorState.value.copy(sessionDurationSeconds = elapsed)
                delay(1000)
            }
        }
    }

    private fun stopSessionTimer() {
        timerJob?.cancel()
        timerJob = null
        _sensorState.value = _sensorState.value.copy(sessionDurationSeconds = 0)
    }

    fun endSession() {
        sensorRepository.disconnect()
        stopSessionTimer()
    }

    fun refreshData() {
        viewModelScope.launch {
            _sensorState.value = _sensorState.value.copy(isRefreshing = true)
            delay(1500) // Simulate refresh
            _sensorState.value = _sensorState.value.copy(isRefreshing = false)
        }
    }

    private fun updateSensorState(sensorData: InsoleSensorData) {
        val stepCount = processStepCountUseCase(sensorData)
        val alerts = analyzePressureThresholdUseCase(sensorData)

        _sensorState.value = _sensorState.value.copy(
            isLoading = false,
            fsrValues = sensorData.pressureValues,
            temperature = sensorData.temperature,
            stepCount = stepCount,
            batteryLevel = sensorData.batteryLevel,
            connectionQuality = "Good",
            thresholdAlerts = alerts.map { it.message }
        )
    }
}
