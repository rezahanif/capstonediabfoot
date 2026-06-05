package com.project.insole.core.ble;

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

  public BleViewModel_Factory(Provider<InsoleBleManager> bleManagerProvider) {
    this.bleManagerProvider = bleManagerProvider;
  }

  @Override
  public BleViewModel get() {
    return newInstance(bleManagerProvider.get());
  }

  public static BleViewModel_Factory create(Provider<InsoleBleManager> bleManagerProvider) {
    return new BleViewModel_Factory(bleManagerProvider);
  }

  public static BleViewModel newInstance(InsoleBleManager bleManager) {
    return new BleViewModel(bleManager);
  }
}
