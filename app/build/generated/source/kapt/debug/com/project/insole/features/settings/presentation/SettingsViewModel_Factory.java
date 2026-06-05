package com.project.insole.features.settings.presentation;

import com.project.insole.features.settings.data.SettingsDataSource;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsDataSource> settingsDataSourceProvider;

  public SettingsViewModel_Factory(Provider<SettingsDataSource> settingsDataSourceProvider) {
    this.settingsDataSourceProvider = settingsDataSourceProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsDataSourceProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsDataSource> settingsDataSourceProvider) {
    return new SettingsViewModel_Factory(settingsDataSourceProvider);
  }

  public static SettingsViewModel newInstance(SettingsDataSource settingsDataSource) {
    return new SettingsViewModel(settingsDataSource);
  }
}
