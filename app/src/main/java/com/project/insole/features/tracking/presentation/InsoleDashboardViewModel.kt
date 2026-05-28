package com.project.insole.features.tracking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.features.tracking.data.repository.SensorRepository
import com.project.insole.features.tracking.domain.model.InsoleSensorData
import com.project.insole.features.tracking.domain.model.PressureZone
import com.project.insole.features.tracking.domain.usecase.AnalyzePressureThresholdUseCase
import com.project.insole.features.tracking.domain.usecase.MapPressureToGridUseCase
import com.project.insole.features.tracking.domain.usecase.ProcessStepCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val pressureGrid: List<List<PressureZone>>? = null,
    val temperature: Float = 0f,
    val stepCount: Int = 0,
    val thresholdAlerts: List<String> = emptyList(),
    val errorMessage: String? = null
)

/**
 * ViewModel for the insole dashboard screen.
 * Exposes immutable StateFlow for UI consumption.
 * Only accesses domain use cases - no data layer access directly.
 * Repository is accessed to get sensor data, then use cases are applied.
 */
@HiltViewModel
class InsoleDashboardViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val mapPressureToGridUseCase: MapPressureToGridUseCase,
    private val processStepCountUseCase: ProcessStepCountUseCase,
    private val analyzePressureThresholdUseCase: AnalyzePressureThresholdUseCase
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardUiState())
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState

    init {
        observeSensorData()
    }

    private fun observeSensorData() {
        viewModelScope.launch {
            sensorRepository.getSensorDataFlow().collect { sensorData ->
                if (sensorData != null) {
                    _dashboardState.value = _dashboardState.value.copy(isLoading = false)
                    updateDashboard(sensorData)
                }
            }
        }
    }

    private fun updateDashboard(sensorData: InsoleSensorData) {
        val pressureGrid = mapPressureToGridUseCase(sensorData)
        val stepCount = processStepCountUseCase(sensorData)
        val alerts = analyzePressureThresholdUseCase(sensorData)

        _dashboardState.value = DashboardUiState(
            isLoading = false,
            pressureGrid = pressureGrid,
            temperature = sensorData.temperature,
            stepCount = stepCount,
            thresholdAlerts = alerts.map { it.message }
        )
    }
}
