package com.project.insole.features.sensor.domain.usecase;

import com.project.insole.features.sensor.domain.service.StepCounterService;
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
public final class ProcessStepCountUseCase_Factory implements Factory<ProcessStepCountUseCase> {
  private final Provider<StepCounterService> stepCounterServiceProvider;

  public ProcessStepCountUseCase_Factory(Provider<StepCounterService> stepCounterServiceProvider) {
    this.stepCounterServiceProvider = stepCounterServiceProvider;
  }

  @Override
  public ProcessStepCountUseCase get() {
    return newInstance(stepCounterServiceProvider.get());
  }

  public static ProcessStepCountUseCase_Factory create(
      Provider<StepCounterService> stepCounterServiceProvider) {
    return new ProcessStepCountUseCase_Factory(stepCounterServiceProvider);
  }

  public static ProcessStepCountUseCase newInstance(StepCounterService stepCounterService) {
    return new ProcessStepCountUseCase(stepCounterService);
  }
}
