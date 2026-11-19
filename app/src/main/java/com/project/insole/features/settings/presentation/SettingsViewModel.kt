package com.project.insole.features.settings.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.features.sensor.domain.service.DataExportManager
import com.project.insole.features.settings.data.DoctorPairingRemoteSource
import com.project.insole.features.settings.data.DoctorPairingStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Patient↔doctor pairing as shown on the Settings screen. */
data class DoctorPairingUiState(
    val isLinked: Boolean = false,
    val doctorName: String = "",
    val code: String = "",          // formatted XXXX-XXXX
    val pairedAt: Long = 0L,
    val pendingServerConfirmation: Boolean = false
)

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val doctor: DoctorPairingUiState = DoctorPairingUiState(),
    val codeInput: String = "",
    val codeError: String? = null
)

/**
 * Settings screen — device links, patient↔doctor pairing, and clinical data export.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val doctorPairingStore: DoctorPairingStore,
    private val doctorPairingRemoteSource: DoctorPairingRemoteSource,
    private val dataExportManager: DataExportManager
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsUiState())
    val settingsState: StateFlow<SettingsUiState> = _settingsState

    init {
        loadPairing()
        refreshRemotePairingStatus()
    }

    private fun loadPairing() {
        val code = doctorPairingStore.getCode()
        _settingsState.value = _settingsState.value.copy(
            isLoading = false,
            doctor = DoctorPairingUiState(
                isLinked = code != null,
                doctorName = doctorPairingStore.getDoctorName().orEmpty(),
                code = code?.chunked(4)?.joinToString("-") ?: "",
                pairedAt = doctorPairingStore.getPairedAt(),
                pendingServerConfirmation = false
            )
        )
    }

    private fun refreshRemotePairingStatus() {
        viewModelScope.launch {
            val result = doctorPairingRemoteSource.checkStatus()
            result.onSuccess { status ->
                if (status.linked) {
                    doctorPairingStore.save(
                        code = doctorPairingStore.getCode() ?: "LINKED",
                        doctorName = status.doctorName
                    )
                    loadPairing()
                } else if (doctorPairingStore.isLinked()) {
                    // Server indicates no active link
                    doctorPairingStore.clear()
                    loadPairing()
                }
            }
        }
    }

    fun exportData(context: Context, isJson: Boolean) {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isExporting = true)
            try {
                val today = java.time.LocalDate.now().toString()
                if (isJson) {
                    val content = dataExportManager.exportWeeklyJson()
                    dataExportManager.shareExport(
                        context = context,
                        content = content,
                        filename = "diabfoot_batch_${today}.json",
                        mimeType = "application/json",
                        chooserTitle = "Export DATA_CONTRACT JSON"
                    )
                } else {
                    val content = dataExportManager.exportWeeklyClinicalCsv()
                    dataExportManager.shareExport(
                        context = context,
                        content = content,
                        filename = "diabfoot_report_${today}.csv",
                        mimeType = "text/csv",
                        chooserTitle = "Export Clinical CSV"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Failed to export data: ${e.message}")
            } finally {
                _settingsState.value = _settingsState.value.copy(isExporting = false)
            }
        }
    }

    fun onCodeInputChange(value: String) {
        _settingsState.value = _settingsState.value.copy(codeInput = value, codeError = null)
    }

    /**
     * Submits the 8-character pairing code to the cloud API (POST /v1/pairing/link)
     * and binds this patient to their doctor server-side.
     */
    fun linkDoctor() {
        val state = _settingsState.value
        val normalized = state.codeInput.replace(" ", "").replace("-", "").uppercase()
        if (!isValidCode(normalized)) {
            _settingsState.value = state.copy(
                codeError = "Please enter the 8-character code provided by your clinic (e.g. 4AB7-9CD2)."
            )
            return
        }

        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isLoading = true, codeError = null)
            val result = doctorPairingRemoteSource.linkDoctor(normalized)
            result.onSuccess { response ->
                doctorPairingStore.save(code = normalized, doctorName = response.doctorName)
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    codeInput = "",
                    codeError = null,
                    doctor = DoctorPairingUiState(
                        isLinked = true,
                        doctorName = response.doctorName,
                        code = normalized.chunked(4).joinToString("-"),
                        pairedAt = System.currentTimeMillis(),
                        pendingServerConfirmation = false
                    )
                )
            }.onFailure { err ->
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    codeError = err.message?.ifBlank { null }
                        ?: "Unable to link with doctor right now. Please check your connection and try again."
                )
            }
        }
    }

    fun unlinkDoctor() {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isLoading = true)
            doctorPairingRemoteSource.unlink()
            doctorPairingStore.clear()
            loadPairing()
        }
    }

    private fun isValidCode(code: String): Boolean =
        code.length == 8 && code.all { it.isLetterOrDigit() }
}
