package com.project.insole.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.features.settings.data.SettingsDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val pressureThreshold: Int = 200,
    val temperatureThreshold: Float = 39f,
    val notificationsEnabled: Boolean = true,
    val dataSyncInterval: Int = 300,
    val errorMessage: String? = null
)

/**
 * ViewModel for settings screen.
 * Manages user preferences and device configuration.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataSource: SettingsDataSource
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsUiState())
    val settingsState: StateFlow<SettingsUiState> = _settingsState

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isLoading = true)
            // Load settings from data source
            _settingsState.value = _settingsState.value.copy(isLoading = false)
        }
    }

    fun updatePressureThreshold(value: Int) {
        viewModelScope.launch {
            settingsDataSource.saveSetting("pressure_threshold", value.toString())
            _settingsState.value = _settingsState.value.copy(pressureThreshold = value)
        }
    }

    fun updateTemperatureThreshold(value: Float) {
        viewModelScope.launch {
            settingsDataSource.saveSetting("temperature_threshold", value.toString())
            _settingsState.value = _settingsState.value.copy(temperatureThreshold = value)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataSource.saveSetting("notification_enabled", enabled.toString())
            _settingsState.value = _settingsState.value.copy(notificationsEnabled = enabled)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsDataSource.clearAllSettings()
            _settingsState.value = SettingsUiState()
        }
    }
}
