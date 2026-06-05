package com.project.insole.core.ble;

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
public final class BleViewModel_Factory implements Factory<BleViewModel> {
  private final Provider<InsoleBleManager> bleManagerProvider;

  private final Provider<StepCounterService> stepCounterServiceProvider;

  public BleViewModel_Factory(Provider<InsoleBleManager> bleManagerProvider,
      Provider<StepCounterService> stepCounterServiceProvider) {
    this.bleManagerProvider = bleManagerProvider;
    this.stepCounterServiceProvider = stepCounterServiceProvider;
  }

  @Override
  public BleViewModel get() {
    return newInstance(bleManagerProvider.get(), stepCounterServiceProvider.get());
  }

  public static BleViewModel_Factory create(Provider<InsoleBleManager> bleManagerProvider,
      Provider<StepCounterService> stepCounterServiceProvider) {
    return new BleViewModel_Factory(bleManagerProvider, stepCounterServiceProvider);
  }

  public static BleViewModel newInstance(InsoleBleManager bleManager,
      StepCounterService stepCounterService) {
    return new BleViewModel(bleManager, stepCounterService);
  }
}
