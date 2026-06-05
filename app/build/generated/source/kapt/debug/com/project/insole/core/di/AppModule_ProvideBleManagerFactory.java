package com.project.insole.core.di;

import android.content.Context;
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
public final class AppModule_ProvideBleManagerFactory implements Factory<InsoleBleManager> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideBleManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public InsoleBleManager get() {
    return provideBleManager(contextProvider.get());
  }

  public static AppModule_ProvideBleManagerFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideBleManagerFactory(contextProvider);
  }

  public static InsoleBleManager provideBleManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBleManager(context));
  }
}
