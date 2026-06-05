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
public final class SignUpUseCase_Factory implements Factory<SignUpUseCase> {
  @Override
  public SignUpUseCase get() {
    return newInstance();
  }

  public static SignUpUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SignUpUseCase newInstance() {
    return new SignUpUseCase();
  }

  private static final class InstanceHolder {
    private static final SignUpUseCase_Factory INSTANCE = new SignUpUseCase_Factory();
  }
}
