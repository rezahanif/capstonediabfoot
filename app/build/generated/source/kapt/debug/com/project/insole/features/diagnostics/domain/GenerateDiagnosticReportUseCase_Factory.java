package com.project.insole.features.diagnostics.domain;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class GenerateDiagnosticReportUseCase_Factory implements Factory<GenerateDiagnosticReportUseCase> {
  @Override
  public GenerateDiagnosticReportUseCase get() {
    return newInstance();
  }

  public static GenerateDiagnosticReportUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GenerateDiagnosticReportUseCase newInstance() {
    return new GenerateDiagnosticReportUseCase();
  }

  private static final class InstanceHolder {
    private static final GenerateDiagnosticReportUseCase_Factory INSTANCE = new GenerateDiagnosticReportUseCase_Factory();
  }
}
