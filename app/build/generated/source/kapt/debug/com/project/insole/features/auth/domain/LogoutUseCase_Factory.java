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
public final class LogoutUseCase_Factory implements Factory<LogoutUseCase> {
  @Override
  public LogoutUseCase get() {
    return newInstance();
  }

  public static LogoutUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LogoutUseCase newInstance() {
    return new LogoutUseCase();
  }

  private static final class InstanceHolder {
    private static final LogoutUseCase_Factory INSTANCE = new LogoutUseCase_Factory();
  }
}
