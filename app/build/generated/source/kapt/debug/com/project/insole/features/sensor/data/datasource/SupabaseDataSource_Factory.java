package com.project.insole.features.sensor.data.datasource;

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
public final class SupabaseDataSource_Factory implements Factory<SupabaseDataSource> {
  @Override
  public SupabaseDataSource get() {
    return newInstance();
  }

  public static SupabaseDataSource_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SupabaseDataSource newInstance() {
    return new SupabaseDataSource();
  }

  private static final class InstanceHolder {
    private static final SupabaseDataSource_Factory INSTANCE = new SupabaseDataSource_Factory();
  }
}
