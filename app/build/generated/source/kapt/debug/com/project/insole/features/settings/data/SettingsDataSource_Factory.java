package com.project.insole.features.settings.data;

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
public final class SettingsDataSource_Factory implements Factory<SettingsDataSource> {
  @Override
  public SettingsDataSource get() {
    return newInstance();
  }

  public static SettingsDataSource_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SettingsDataSource newInstance() {
    return new SettingsDataSource();
  }

  private static final class InstanceHolder {
    private static final SettingsDataSource_Factory INSTANCE = new SettingsDataSource_Factory();
  }
}
