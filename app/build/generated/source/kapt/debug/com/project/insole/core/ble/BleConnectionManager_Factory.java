package com.project.insole.core.ble;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class BleConnectionManager_Factory implements Factory<BleConnectionManager> {
  private final Provider<Context> contextProvider;

  private final Provider<InsoleBleManager> bleManagerProvider;

  public BleConnectionManager_Factory(Provider<Context> contextProvider,
      Provider<InsoleBleManager> bleManagerProvider) {
    this.contextProvider = contextProvider;
    this.bleManagerProvider = bleManagerProvider;
  }

  @Override
  public BleConnectionManager get() {
    return newInstance(contextProvider.get(), bleManagerProvider.get());
  }

  public static BleConnectionManager_Factory create(Provider<Context> contextProvider,
      Provider<InsoleBleManager> bleManagerProvider) {
    return new BleConnectionManager_Factory(contextProvider, bleManagerProvider);
  }

  public static BleConnectionManager newInstance(Context context, InsoleBleManager bleManager) {
    return new BleConnectionManager(context, bleManager);
  }
}
