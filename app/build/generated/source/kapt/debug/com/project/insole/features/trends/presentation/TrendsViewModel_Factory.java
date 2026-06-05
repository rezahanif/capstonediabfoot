package com.project.insole.features.trends.presentation;

import com.project.insole.features.trends.data.TrendsDataSource;
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
public final class TrendsViewModel_Factory implements Factory<TrendsViewModel> {
  private final Provider<TrendsDataSource> trendsDataSourceProvider;

  public TrendsViewModel_Factory(Provider<TrendsDataSource> trendsDataSourceProvider) {
    this.trendsDataSourceProvider = trendsDataSourceProvider;
  }

  @Override
  public TrendsViewModel get() {
    return newInstance(trendsDataSourceProvider.get());
  }

  public static TrendsViewModel_Factory create(
      Provider<TrendsDataSource> trendsDataSourceProvider) {
    return new TrendsViewModel_Factory(trendsDataSourceProvider);
  }

  public static TrendsViewModel newInstance(TrendsDataSource trendsDataSource) {
    return new TrendsViewModel(trendsDataSource);
  }
}
