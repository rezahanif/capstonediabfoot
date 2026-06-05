package com.project.insole.features.auth.domain;

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
public final class CheckSessionUseCase_Factory implements Factory<CheckSessionUseCase> {
  @Override
  public CheckSessionUseCase get() {
    return newInstance();
  }

  public static CheckSessionUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CheckSessionUseCase newInstance() {
    return new CheckSessionUseCase();
  }

  private static final class InstanceHolder {
    private static final CheckSessionUseCase_Factory INSTANCE = new CheckSessionUseCase_Factory();
  }
}
