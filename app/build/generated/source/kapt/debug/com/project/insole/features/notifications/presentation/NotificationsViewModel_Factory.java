package com.project.insole.features.notifications.presentation;

import com.project.insole.features.notifications.data.NotificationDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class NotificationsViewModel_Factory implements Factory<NotificationsViewModel> {
  private final Provider<NotificationDataSource> notificationDataSourceProvider;

  public NotificationsViewModel_Factory(
      Provider<NotificationDataSource> notificationDataSourceProvider) {
    this.notificationDataSourceProvider = notificationDataSourceProvider;
  }

  @Override
  public NotificationsViewModel get() {
    return newInstance(notificationDataSourceProvider.get());
  }

  public static NotificationsViewModel_Factory create(
      Provider<NotificationDataSource> notificationDataSourceProvider) {
    return new NotificationsViewModel_Factory(notificationDataSourceProvider);
  }

  public static NotificationsViewModel newInstance(NotificationDataSource notificationDataSource) {
    return new NotificationsViewModel(notificationDataSource);
  }
}
