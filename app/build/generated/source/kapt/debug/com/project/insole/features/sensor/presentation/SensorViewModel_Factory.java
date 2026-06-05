package com.project.insole.features.sensor.presentation;

import com.project.insole.features.sensor.domain.repository.SensorRepository;
import com.project.insole.features.sensor.domain.usecase.AnalyzePressureThresholdUseCase;
import com.project.insole.features.sensor.domain.usecase.MapPressureToGridUseCase;
import com.project.insole.features.sensor.domain.usecase.ProcessStepCountUseCase;
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
public final class SensorViewModel_Factory implements Factory<SensorViewModel> {
  private final Provider<SensorRepository> sensorRepositoryProvider;

  private final Provider<MapPressureToGridUseCase> mapPressureToGridUseCaseProvider;

  private final Provider<ProcessStepCountUseCase> processStepCountUseCaseProvider;

  private final Provider<AnalyzePressureThresholdUseCase> analyzePressureThresholdUseCaseProvider;

  public SensorViewModel_Factory(Provider<SensorRepository> sensorRepositoryProvider,
      Provider<MapPressureToGridUseCase> mapPressureToGridUseCaseProvider,
      Provider<ProcessStepCountUseCase> processStepCountUseCaseProvider,
      Provider<AnalyzePressureThresholdUseCase> analyzePressureThresholdUseCaseProvider) {
    this.sensorRepositoryProvider = sensorRepositoryProvider;
    this.mapPressureToGridUseCaseProvider = mapPressureToGridUseCaseProvider;
    this.processStepCountUseCaseProvider = processStepCountUseCaseProvider;
    this.analyzePressureThresholdUseCaseProvider = analyzePressureThresholdUseCaseProvider;
  }

  @Override
  public SensorViewModel get() {
    return newInstance(sensorRepositoryProvider.get(), mapPressureToGridUseCaseProvider.get(), processStepCountUseCaseProvider.get(), analyzePressureThresholdUseCaseProvider.get());
  }

  public static SensorViewModel_Factory create(Provider<SensorRepository> sensorRepositoryProvider,
      Provider<MapPressureToGridUseCase> mapPressureToGridUseCaseProvider,
      Provider<ProcessStepCountUseCase> processStepCountUseCaseProvider,
      Provider<AnalyzePressureThresholdUseCase> analyzePressureThresholdUseCaseProvider) {
    return new SensorViewModel_Factory(sensorRepositoryProvider, mapPressureToGridUseCaseProvider, processStepCountUseCaseProvider, analyzePressureThresholdUseCaseProvider);
  }

  public static SensorViewModel newInstance(SensorRepository sensorRepository,
      MapPressureToGridUseCase mapPressureToGridUseCase,
      ProcessStepCountUseCase processStepCountUseCase,
      AnalyzePressureThresholdUseCase analyzePressureThresholdUseCase) {
    return new SensorViewModel(sensorRepository, mapPressureToGridUseCase, processStepCountUseCase, analyzePressureThresholdUseCase);
  }
}
