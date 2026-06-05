package com.project.insole.features.trends.data;

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
public final class TrendsDataSource_Factory implements Factory<TrendsDataSource> {
  @Override
  public TrendsDataSource get() {
    return newInstance();
  }

  public static TrendsDataSource_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TrendsDataSource newInstance() {
    return new TrendsDataSource();
  }

  private static final class InstanceHolder {
    private static final TrendsDataSource_Factory INSTANCE = new TrendsDataSource_Factory();
  }
}
