package com.project.insole.features.sensor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.features.sensor.data.repository.SensorRepository
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.usecase.AnalyzePressureThresholdUseCase
import com.project.insole.features.sensor.domain.usecase.MapPressureToGridUseCase
import com.project.insole.features.sensor.domain.usecase.ProcessStepCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SensorUiState(
    val isLoading: Boolean = false,
    val fsrValues: List<Int> = emptyList(),      // 5 FSR values from ESP32
    val temperature: Float = 0f,
    val stepCount: Int = 0,
    val batteryLevel: Int = 0,
    val connectionQuality: String = "Unknown",
    val thresholdAlerts: List<String> = emptyList(),
    val errorMessage: String? = null
)

/**
 * ViewModel for all sensor-related screens (Dashboard & Monitoring).
 * Exposes immutable StateFlow with all ESP32 sensor data.
 * Both DashboardScreen and MonitoringScreen use this same ViewModel.
 * Only accesses domain use cases - no data layer access directly.
 */
@HiltViewModel
class SensorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val mapPressureToGridUseCase: MapPressureToGridUseCase,
    private val processStepCountUseCase: ProcessStepCountUseCase,
    private val analyzePressureThresholdUseCase: AnalyzePressureThresholdUseCase
) : ViewModel() {

    private val _sensorState = MutableStateFlow(SensorUiState())
    val sensorState: StateFlow<SensorUiState> = _sensorState

    init {
        observeSensorData()
    }

    private fun observeSensorData() {
        viewModelScope.launch {
            sensorRepository.getSensorDataFlow().collect { sensorData ->
                if (sensorData != null) {
                    _sensorState.value = _sensorState.value.copy(isLoading = false)
                    updateSensorState(sensorData)
                }
            }
        }
    }

    private fun updateSensorState(sensorData: InsoleSensorData) {
        val stepCount = processStepCountUseCase(sensorData)
        val alerts = analyzePressureThresholdUseCase(sensorData)

        _sensorState.value = SensorUiState(
            isLoading = false,
            fsrValues = sensorData.pressureValues,
            temperature = sensorData.temperature,
            stepCount = stepCount,
            batteryLevel = 85,  // TODO: Get from BLE data
            connectionQuality = "Good",  // TODO: Get RSSI from BLE data
            thresholdAlerts = alerts.map { it.message }
        )
    }
}
