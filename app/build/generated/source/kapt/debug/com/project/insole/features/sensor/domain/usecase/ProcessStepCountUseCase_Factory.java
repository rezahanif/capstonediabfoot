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
public final class ProcessStepCountUseCase_Factory implements Factory<ProcessStepCountUseCase> {
  @Override
  public ProcessStepCountUseCase get() {
    return newInstance();
  }

  public static ProcessStepCountUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ProcessStepCountUseCase newInstance() {
    return new ProcessStepCountUseCase();
  }

  private static final class InstanceHolder {
    private static final ProcessStepCountUseCase_Factory INSTANCE = new ProcessStepCountUseCase_Factory();
  }
}
