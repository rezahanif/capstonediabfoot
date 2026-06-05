package com.project.insole.features.sensor.data.datasource;

import com.project.insole.core.ble.InsoleBleManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class BleSensorDataSource_Factory implements Factory<BleSensorDataSource> {
  private final Provider<InsoleBleManager> bleManagerProvider;

  public BleSensorDataSource_Factory(Provider<InsoleBleManager> bleManagerProvider) {
    this.bleManagerProvider = bleManagerProvider;
  }

  @Override
  public BleSensorDataSource get() {
    return newInstance(bleManagerProvider.get());
  }

  public static BleSensorDataSource_Factory create(Provider<InsoleBleManager> bleManagerProvider) {
    return new BleSensorDataSource_Factory(bleManagerProvider);
  }

  public static BleSensorDataSource newInstance(InsoleBleManager bleManager) {
    return new BleSensorDataSource(bleManager);
  }
}
