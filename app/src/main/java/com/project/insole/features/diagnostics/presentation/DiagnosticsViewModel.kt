package com.project.insole.features.diagnostics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.features.diagnostics.data.DiagnosticsDataSource
import com.project.insole.features.diagnostics.domain.DiagnosticReport
import com.project.insole.features.diagnostics.domain.GenerateDiagnosticReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagnosticsUiState(
    val isLoading: Boolean = false,
    val diagnosticReport: DiagnosticReport? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel for the technical diagnostics screen (TD-4).
 * Preserves raw numerical sensor readings, RSSI, and protocol metadata for doctor/engineer review.
 */
@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val diagnosticsDataSource: DiagnosticsDataSource,
    private val generateDiagnosticReportUseCase: GenerateDiagnosticReportUseCase
) : ViewModel() {

    private val _diagnosticsState = MutableStateFlow(DiagnosticsUiState())
    val diagnosticsState: StateFlow<DiagnosticsUiState> = _diagnosticsState

    init {
        refreshDiagnostics()
    }

    fun refreshDiagnostics() {
        viewModelScope.launch {
            _diagnosticsState.value = _diagnosticsState.value.copy(isLoading = true)
            val result = diagnosticsDataSource.getDeviceStatus()
            result.onSuccess { status ->
                val report = generateDiagnosticReportUseCase(status)
                _diagnosticsState.value = DiagnosticsUiState(
                    isLoading = false,
                    diagnosticReport = report
                )
            }.onFailure { exception ->
                _diagnosticsState.value = DiagnosticsUiState(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }
}
