package com.project.insole.features.diagnostics.presentation;

/**
 * ViewModel for the diagnostics screen.
 * Exposes immutable StateFlow for UI consumption.
 * Only accesses domain use cases and repository.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u000e\u001a\u00020\u000fJ\u0006\u0010\u0010\u001a\u00020\u000fR\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/project/insole/features/diagnostics/presentation/DiagnosticsViewModel;", "Landroidx/lifecycle/ViewModel;", "diagnosticsDataSource", "Lcom/project/insole/features/diagnostics/data/DiagnosticsDataSource;", "generateDiagnosticReportUseCase", "Lcom/project/insole/features/diagnostics/domain/GenerateDiagnosticReportUseCase;", "(Lcom/project/insole/features/diagnostics/data/DiagnosticsDataSource;Lcom/project/insole/features/diagnostics/domain/GenerateDiagnosticReportUseCase;)V", "_diagnosticsState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/features/diagnostics/presentation/DiagnosticsUiState;", "diagnosticsState", "Lkotlinx/coroutines/flow/StateFlow;", "getDiagnosticsState", "()Lkotlinx/coroutines/flow/StateFlow;", "calibrateDevice", "", "refreshDiagnostics", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class DiagnosticsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.diagnostics.data.DiagnosticsDataSource diagnosticsDataSource = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.diagnostics.domain.GenerateDiagnosticReportUseCase generateDiagnosticReportUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.features.diagnostics.presentation.DiagnosticsUiState> _diagnosticsState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.project.insole.features.diagnostics.presentation.DiagnosticsUiState> diagnosticsState = null;
    
    @javax.inject.Inject()
    public DiagnosticsViewModel(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.diagnostics.data.DiagnosticsDataSource diagnosticsDataSource, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.diagnostics.domain.GenerateDiagnosticReportUseCase generateDiagnosticReportUseCase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.project.insole.features.diagnostics.presentation.DiagnosticsUiState> getDiagnosticsState() {
        return null;
    }
    
    public final void refreshDiagnostics() {
    }
    
    public final void calibrateDevice() {
    }
}