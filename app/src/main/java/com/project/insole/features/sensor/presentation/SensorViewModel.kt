package com.project.insole.features.sensor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.core.ble.model.BleDeviceState
import com.project.insole.core.notifications.InsoleNotificationManager
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.repository.SensorRepository
import com.project.insole.features.sensor.domain.usecase.AnalyzePressureThresholdUseCase
import com.project.insole.features.sensor.domain.usecase.MapPressureToGridUseCase
import com.project.insole.features.sensor.domain.usecase.SyncCloudDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Pre-rendered plantar pressure grids for the monitoring screen's heatmap.
 * Grids are row-major 5×10 [Float]s produced by [MapPressureToGridUseCase].
 * [isZoned] is true when at least one foot delivered the 5-zone v2 payload;
 * false means legacy single-FSR data (uniform grid, no zone info).
 */
data class PressureMapState(
    val leftGrid: List<Float> = emptyList(),
    val rightGrid: List<Float> = emptyList(),
    val isZoned: Boolean = false,
    val leftZonePressures: List<Float> = emptyList(),
    val rightZonePressures: List<Float> = emptyList(),
)

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
    val isRefreshing: Boolean = false,
    val pressureMap: PressureMapState = PressureMapState()
)

@HiltViewModel
class SensorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val mapPressureToGridUseCase: MapPressureToGridUseCase,
    private val analyzePressureThresholdUseCase: AnalyzePressureThresholdUseCase,
    private val syncCloudDataUseCase: SyncCloudDataUseCase,
    private val notificationManager: InsoleNotificationManager
) : ViewModel() {

    private val _sensorState = MutableStateFlow(SensorUiState())
    val sensorState: StateFlow<SensorUiState> = _sensorState

    private var timerJob: Job? = null
    private var sessionStartTime: Long = 0L

    private var lastPressureNotifyMs = 0L
    private var lastThermalNotifyMs = 0L
    private val NOTIFY_COOLDOWN_MS = 15_000L

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
                _sensorState.update { it.copy(rawBleLeft = rawData) }
            }
        }

        viewModelScope.launch {
            sensorRepository.getRawRightDataFlow().collect { rawData ->
                _sensorState.update { it.copy(rawBleRight = rawData) }
            }
        }
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            sensorRepository.getConnectionState().collect { state ->
                val isConnected = state == BleDeviceState.Connected
                
                if (isConnected) {
                    _sensorState.update { it.copy(isConnected = true) }
                    startSessionTimer()
                } else {
                    stopSessionTimer()
                    analyzePressureThresholdUseCase.reset()
                    _sensorState.update { it.copy(
                        isConnected = false,
                        fsrValues = emptyList(),
                        temperature = 0f,
                        stepCount = 0,
                        batteryLevel = 0,
                        thresholdAlerts = emptyList(),
                        connectionQuality = "Disconnected",
                        pressureMap = PressureMapState()
                    )}
                }
            }
        }
    }

    private fun startSessionTimer() {
        if (timerJob != null) return
        
        sessionStartTime = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (true) {
                val elapsed = (System.currentTimeMillis() - sessionStartTime) / 1000
                _sensorState.update { it.copy(sessionDurationSeconds = elapsed) }
                delay(1000)
            }
        }
    }

    private fun stopSessionTimer() {
        timerJob?.cancel()
        timerJob = null
        _sensorState.update { it.copy(sessionDurationSeconds = 0) }
    }

    fun endSession() {
        sensorRepository.disconnect()
        stopSessionTimer()
    }

    fun refreshData() {
        viewModelScope.launch {
            _sensorState.update { it.copy(isRefreshing = true, errorMessage = null) }
            val result = syncCloudDataUseCase()
            result.onFailure { error ->
                _sensorState.update { it.copy(errorMessage = error.message) }
            }
            _sensorState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun updateSensorState(sensorData: InsoleSensorData) {
        val alerts = analyzePressureThresholdUseCase(sensorData)

        // Post Android heads-up system notification when alerts fire
        val now = System.currentTimeMillis()
        for (alert in alerts) {
            if (alert.affectedZones.contains("THERMAL")) {
                if (now - lastThermalNotifyMs >= NOTIFY_COOLDOWN_MS) {
                    lastThermalNotifyMs = now
                    notificationManager.sendThermalAlert(
                        title = "DiabFoot: Temperature Asymmetry",
                        message = alert.message
                    )
                }
            } else {
                if (now - lastPressureNotifyMs >= NOTIFY_COOLDOWN_MS) {
                    lastPressureNotifyMs = now
                    notificationManager.sendPressureAlert(
                        title = "DiabFoot: High Pressure Warning",
                        message = alert.message,
                        affectedZone = alert.affectedZones.firstOrNull() ?: "FOOT"
                    )
                }
            }
        }

        // Render the per-foot 5×10 pressure grids. A 5-entry payload is the
        // zoned v2 stream; anything else maps to an empty/uniform grid.
        val leftGrid = mapPressureToGridUseCase(sensorData.leftZonePressuresKpa)
        val rightGrid = mapPressureToGridUseCase(sensorData.rightZonePressuresKpa)
        val isZoned = sensorData.leftZonePressuresKpa.size == MapPressureToGridUseCase.ZONED_INPUT_SIZE ||
            sensorData.rightZonePressuresKpa.size == MapPressureToGridUseCase.ZONED_INPUT_SIZE

        _sensorState.update { it.copy(
            isLoading = false,
            fsrValues = sensorData.pressureValues,
            temperature = sensorData.temperature,
            stepCount = sensorData.stepCount,
            batteryLevel = sensorData.batteryLevel,
            connectionQuality = "Good",
            thresholdAlerts = alerts.map { it.message },
            pressureMap = PressureMapState(
                leftGrid = leftGrid,
                rightGrid = rightGrid,
                isZoned = isZoned,
                leftZonePressures = sensorData.leftZonePressuresKpa,
                rightZonePressures = sensorData.rightZonePressuresKpa,
            )
        )}
    }
}
