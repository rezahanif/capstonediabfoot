package com.project.insole.features.sensor.domain.usecase;

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
public final class AnalyzePressureThresholdUseCase_Factory implements Factory<AnalyzePressureThresholdUseCase> {
  @Override
  public AnalyzePressureThresholdUseCase get() {
    return newInstance();
  }

  public static AnalyzePressureThresholdUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AnalyzePressureThresholdUseCase newInstance() {
    return new AnalyzePressureThresholdUseCase();
  }

  private static final class InstanceHolder {
    private static final AnalyzePressureThresholdUseCase_Factory INSTANCE = new AnalyzePressureThresholdUseCase_Factory();
  }
}
