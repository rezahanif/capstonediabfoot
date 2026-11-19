package com.project.insole.features.diagnostics.presentation.seeder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.features.notifications.domain.FootSide
import com.project.insole.features.sensor.data.datasource.BleSensorDataSource
import com.project.insole.features.sensor.domain.model.ClinicalScoringVariables
import com.project.insole.features.sensor.domain.model.RiskLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeederUiState(
    val yesterdayDate: String = "",
    val hasYesterdayRecord: Boolean = false,
    val yesterdayWarmerFoot: FootSide? = null,
    val yesterdayAsymmetryC: Float = 0f,
    val consecutiveThermalDays: Int = 0,
    val sustainedThermalSide: FootSide? = null,
    val isSustainedHotspot2Day: Boolean = false,
    val currentRiskLevel: RiskLevel = RiskLevel.LOW,
    val isValidSwell: Boolean = false,
    val statusMessage: String? = null,
    val isBusy: Boolean = false
)

@HiltViewModel
class ClinicalSeederViewModel @Inject constructor(
    private val seeder: ClinicalScenarioSeeder,
    private val bleSensorDataSource: BleSensorDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SeederUiState(yesterdayDate = seeder.getYesterdayDate())
    )
    val uiState: StateFlow<SeederUiState> = _uiState.asStateFlow()

    init {
        observeYesterdayRecord()
        observeClinicalVariables()
    }

    private fun observeYesterdayRecord() {
        viewModelScope.launch {
            seeder.getYesterdayRecordFlow().collect { entity ->
                if (entity != null) {
                    val deltaC = if (entity.cleanLeftSamples > 0 && entity.cleanRightSamples > 0) {
                        entity.cleanThermalAsymmetryC
                    } else {
                        entity.thermalAsymmetryC
                    }
                    val warmer = when {
                        entity.cleanLeftAvgTemp > entity.cleanRightAvgTemp -> FootSide.LEFT
                        entity.cleanRightAvgTemp > entity.cleanLeftAvgTemp -> FootSide.RIGHT
                        else -> null
                    }
                    _uiState.update {
                        it.copy(
                            yesterdayDate = entity.date,
                            hasYesterdayRecord = true,
                            yesterdayWarmerFoot = warmer,
                            yesterdayAsymmetryC = deltaC
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            yesterdayDate = seeder.getYesterdayDate(),
                            hasYesterdayRecord = false,
                            yesterdayWarmerFoot = null,
                            yesterdayAsymmetryC = 0f
                        )
                    }
                }
            }
        }
    }

    private fun observeClinicalVariables() {
        viewModelScope.launch {
            bleSensorDataSource.clinicalScoringFlow.collect { clinical ->
                _uiState.update {
                    it.copy(
                        consecutiveThermalDays = clinical.consecutiveThermalDays,
                        sustainedThermalSide = clinical.sustainedThermalSide,
                        isSustainedHotspot2Day = clinical.isSustainedHotspot2Day,
                        currentRiskLevel = clinical.riskLevel,
                        isValidSwell = clinical.isValidSwell
                    )
                }
            }
        }
    }

    fun seedYesterdayLeftHotspot() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            seeder.seedYesterdayHotspot(foot = FootSide.LEFT, asymmetryDeltaC = 2.5f)
            _uiState.update {
                it.copy(
                    isBusy = false,
                    statusMessage = "Seeded Yesterday: LEFT Hotspot (+2.5°C). Ready for Day-2 trigger!"
                )
            }
        }
    }

    fun seedYesterdayRightHotspot() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            seeder.seedYesterdayHotspot(foot = FootSide.RIGHT, asymmetryDeltaC = 2.5f)
            _uiState.update {
                it.copy(
                    isBusy = false,
                    statusMessage = "Seeded Yesterday: RIGHT Hotspot (+2.5°C). Ready for Day-2 trigger!"
                )
            }
        }
    }

    fun seedYesterdayNormal() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            seeder.seedYesterdayBaseline()
            _uiState.update {
                it.copy(
                    isBusy = false,
                    statusMessage = "Seeded Yesterday: Normal Baseline (ΔT = 0.2°C)."
                )
            }
        }
    }

    fun clearSeededYesterday() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            seeder.clearYesterdayRecord()
            _uiState.update {
                it.copy(
                    isBusy = false,
                    statusMessage = "Cleared yesterday's seeded metrics."
                )
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
