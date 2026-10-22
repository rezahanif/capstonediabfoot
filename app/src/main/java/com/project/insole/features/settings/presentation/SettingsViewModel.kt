package com.project.insole.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.features.settings.data.DoctorPairingStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Patient↔doctor pairing as shown on the Settings screen. */
data class DoctorPairingUiState(
    val isLinked: Boolean = false,
    val doctorName: String = "",
    val code: String = "",          // formatted XXXX-XXXX
    val pairedAt: Long = 0L,
    // True until the cloud API confirms the binding server-side. The UI must
    // show this honestly instead of pretending the doctor can already see data.
    val pendingServerConfirmation: Boolean = true
)

data class SettingsUiState(
    val isLoading: Boolean = false,
    val doctor: DoctorPairingUiState = DoctorPairingUiState(),
    val codeInput: String = "",
    val codeError: String? = null
)

/**
 * Settings screen — device links and the patient↔doctor pairing.
 *
 * The old fake threshold sliders are gone: alert thresholds are clinically
 * informed constants (see SensorConstants) and were never actually wired to
 * the rule evaluator — presenting them as user-adjustable was dishonest.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val doctorPairingStore: DoctorPairingStore
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsUiState())
    val settingsState: StateFlow<SettingsUiState> = _settingsState

    init {
        loadPairing()
    }

    private fun loadPairing() {
        val code = doctorPairingStore.getCode()
        _settingsState.value = _settingsState.value.copy(
            isLoading = false,
            doctor = DoctorPairingUiState(
                isLinked = code != null,
                doctorName = doctorPairingStore.getDoctorName().orEmpty(),
                code = code?.chunked(4)?.joinToString("-") ?: "",
                pairedAt = doctorPairingStore.getPairedAt()
            )
        )
    }

    fun onCodeInputChange(value: String) {
        _settingsState.value = _settingsState.value.copy(codeInput = value, codeError = null)
    }

    /**
     * Links the entered pairing code locally. The doctor web app issues
     * codes; once cloud/server is deployed, this call also POSTs the code to
     * /v1/pairing/link, and the server creates the real doctor→patient row
     * (patient may only ever have ONE doctor — UNIQUE constraint).
     */
    fun linkDoctor() {
        val state = _settingsState.value
        val normalized = state.codeInput.replace(" ", "").replace("-", "").uppercase()
        if (!isValidCode(normalized)) {
            _settingsState.value = state.copy(
                codeError = "A pairing code looks like 4AB7-9CD2 — 8 characters, from your doctor."
            )
            return
        }
        doctorPairingStore.save(code = normalized, doctorName = "Your doctor")
        _settingsState.value = state.copy(codeInput = "", codeError = null)
        loadPairing()
    }

    fun unlinkDoctor() {
        doctorPairingStore.clear()
        loadPairing()
    }

    private fun isValidCode(code: String): Boolean =
        code.length == 8 && code.all { it.isLetterOrDigit() }
}
