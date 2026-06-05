package com.project.insole.features.diagnostics.presentation;

import com.project.insole.features.diagnostics.data.DiagnosticsDataSource;
import com.project.insole.features.diagnostics.domain.GenerateDiagnosticReportUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class DiagnosticsViewModel_Factory implements Factory<DiagnosticsViewModel> {
  private final Provider<DiagnosticsDataSource> diagnosticsDataSourceProvider;

  private final Provider<GenerateDiagnosticReportUseCase> generateDiagnosticReportUseCaseProvider;

  public DiagnosticsViewModel_Factory(Provider<DiagnosticsDataSource> diagnosticsDataSourceProvider,
      Provider<GenerateDiagnosticReportUseCase> generateDiagnosticReportUseCaseProvider) {
    this.diagnosticsDataSourceProvider = diagnosticsDataSourceProvider;
    this.generateDiagnosticReportUseCaseProvider = generateDiagnosticReportUseCaseProvider;
  }

  @Override
  public DiagnosticsViewModel get() {
    return newInstance(diagnosticsDataSourceProvider.get(), generateDiagnosticReportUseCaseProvider.get());
  }

  public static DiagnosticsViewModel_Factory create(
      Provider<DiagnosticsDataSource> diagnosticsDataSourceProvider,
      Provider<GenerateDiagnosticReportUseCase> generateDiagnosticReportUseCaseProvider) {
    return new DiagnosticsViewModel_Factory(diagnosticsDataSourceProvider, generateDiagnosticReportUseCaseProvider);
  }

  public static DiagnosticsViewModel newInstance(DiagnosticsDataSource diagnosticsDataSource,
      GenerateDiagnosticReportUseCase generateDiagnosticReportUseCase) {
    return new DiagnosticsViewModel(diagnosticsDataSource, generateDiagnosticReportUseCase);
  }
}
