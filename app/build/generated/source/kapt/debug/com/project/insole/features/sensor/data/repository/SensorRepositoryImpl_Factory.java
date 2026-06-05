package com.project.insole.features.sensor.data.repository;

import com.project.insole.core.ble.InsoleBleManager;
import com.project.insole.features.sensor.data.datasource.BleSensorDataSource;
import com.project.insole.features.sensor.data.datasource.SupabaseDataSource;
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
public final class SensorRepositoryImpl_Factory implements Factory<SensorRepositoryImpl> {
  private final Provider<BleSensorDataSource> bleSensorDataSourceProvider;

  private final Provider<SupabaseDataSource> supabaseDataSourceProvider;

  private final Provider<InsoleBleManager> bleManagerProvider;

  public SensorRepositoryImpl_Factory(Provider<BleSensorDataSource> bleSensorDataSourceProvider,
      Provider<SupabaseDataSource> supabaseDataSourceProvider,
      Provider<InsoleBleManager> bleManagerProvider) {
    this.bleSensorDataSourceProvider = bleSensorDataSourceProvider;
    this.supabaseDataSourceProvider = supabaseDataSourceProvider;
    this.bleManagerProvider = bleManagerProvider;
  }

  @Override
  public SensorRepositoryImpl get() {
    return newInstance(bleSensorDataSourceProvider.get(), supabaseDataSourceProvider.get(), bleManagerProvider.get());
  }

  public static SensorRepositoryImpl_Factory create(
      Provider<BleSensorDataSource> bleSensorDataSourceProvider,
      Provider<SupabaseDataSource> supabaseDataSourceProvider,
      Provider<InsoleBleManager> bleManagerProvider) {
    return new SensorRepositoryImpl_Factory(bleSensorDataSourceProvider, supabaseDataSourceProvider, bleManagerProvider);
  }

  public static SensorRepositoryImpl newInstance(BleSensorDataSource bleSensorDataSource,
      SupabaseDataSource supabaseDataSource, InsoleBleManager bleManager) {
    return new SensorRepositoryImpl(bleSensorDataSource, supabaseDataSource, bleManager);
  }
}
