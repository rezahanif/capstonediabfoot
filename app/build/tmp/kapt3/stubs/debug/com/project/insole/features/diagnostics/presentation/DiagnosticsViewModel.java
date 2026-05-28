package com.project.insole.features.diagnostics.presentation;

/**
 * ViewModel for the diagnostics screen.
 * Exposes immutable StateFlow for UI consumption.
 * Only accesses domain use cases and repository.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u0006\u0010\u000f\u001a\u00020\u0010J\u0006\u0010\u0011\u001a\u00020\u0010R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\n0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0012"}, d2 = {"Lcom/project/insole/features/diagnostics/presentation/DiagnosticsViewModel;", "Landroidx/lifecycle/ViewModel;", "diagnosticsDataSource", "Lcom/project/insole/features/diagnostics/data/DiagnosticsDataSource;", "generateDiagnosticReportUseCase", "Lcom/project/insole/features/diagnostics/domain/GenerateDiagnosticReportUseCase;", "<init>", "(Lcom/project/insole/features/diagnostics/data/DiagnosticsDataSource;Lcom/project/insole/features/diagnostics/domain/GenerateDiagnosticReportUseCase;)V", "_diagnosticsState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/features/diagnostics/presentation/DiagnosticsUiState;", "diagnosticsState", "Lkotlinx/coroutines/flow/StateFlow;", "getDiagnosticsState", "()Lkotlinx/coroutines/flow/StateFlow;", "refreshDiagnostics", "", "calibrateDevice", "app_debug"})
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