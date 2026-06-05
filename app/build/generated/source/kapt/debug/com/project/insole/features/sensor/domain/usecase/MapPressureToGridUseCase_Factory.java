package com.project.insole.features.sensor.domain.usecase;

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
public final class MapPressureToGridUseCase_Factory implements Factory<MapPressureToGridUseCase> {
  @Override
  public MapPressureToGridUseCase get() {
    return newInstance();
  }

  public static MapPressureToGridUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MapPressureToGridUseCase newInstance() {
    return new MapPressureToGridUseCase();
  }

  private static final class InstanceHolder {
    private static final MapPressureToGridUseCase_Factory INSTANCE = new MapPressureToGridUseCase_Factory();
  }
}
