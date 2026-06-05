package com.project.insole;

import com.project.insole.core.ble.BleConnectionManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<BleConnectionManager> bleConnectionManagerProvider;

  public MainActivity_MembersInjector(Provider<BleConnectionManager> bleConnectionManagerProvider) {
    this.bleConnectionManagerProvider = bleConnectionManagerProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<BleConnectionManager> bleConnectionManagerProvider) {
    return new MainActivity_MembersInjector(bleConnectionManagerProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectBleConnectionManager(instance, bleConnectionManagerProvider.get());
  }

  @InjectedFieldSignature("com.project.insole.MainActivity.bleConnectionManager")
  public static void injectBleConnectionManager(MainActivity instance,
      BleConnectionManager bleConnectionManager) {
    instance.bleConnectionManager = bleConnectionManager;
  }
}
