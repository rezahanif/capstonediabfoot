package com.project.insole.core.di;

import android.content.Context;
import com.project.insole.core.ble.BleConnectionManager;
import com.project.insole.core.ble.InsoleBleManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideBleConnectionManagerFactory implements Factory<BleConnectionManager> {
  private final Provider<Context> contextProvider;

  private final Provider<InsoleBleManager> bleManagerProvider;

  public AppModule_ProvideBleConnectionManagerFactory(Provider<Context> contextProvider,
      Provider<InsoleBleManager> bleManagerProvider) {
    this.contextProvider = contextProvider;
    this.bleManagerProvider = bleManagerProvider;
  }

  @Override
  public BleConnectionManager get() {
    return provideBleConnectionManager(contextProvider.get(), bleManagerProvider.get());
  }

  public static AppModule_ProvideBleConnectionManagerFactory create(
      Provider<Context> contextProvider, Provider<InsoleBleManager> bleManagerProvider) {
    return new AppModule_ProvideBleConnectionManagerFactory(contextProvider, bleManagerProvider);
  }

  public static BleConnectionManager provideBleConnectionManager(Context context,
      InsoleBleManager bleManager) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBleConnectionManager(context, bleManager));
  }
}
