package com.project.insole.features.notifications.data;

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
public final class NotificationDataSource_Factory implements Factory<NotificationDataSource> {
  @Override
  public NotificationDataSource get() {
    return newInstance();
  }

  public static NotificationDataSource_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NotificationDataSource newInstance() {
    return new NotificationDataSource();
  }

  private static final class InstanceHolder {
    private static final NotificationDataSource_Factory INSTANCE = new NotificationDataSource_Factory();
  }
}
