package com.project.insole.features.notifications.data;

/**
 * Data source for notification management.
 * Fetches and stores notification history from local database.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J.\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\bH\u0086@\u00a2\u0006\u0004\b\u000b\u0010\fJ&\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\u00052\b\b\u0002\u0010\u0010\u001a\u00020\u0011H\u0086@\u00a2\u0006\u0004\b\u0012\u0010\u0013J \u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\b\u0002\u0010\u0015\u001a\u00020\u0011H\u0086@\u00a2\u0006\u0004\b\u0016\u0010\u0013\u00a8\u0006\u0017"}, d2 = {"Lcom/project/insole/features/notifications/data/NotificationDataSource;", "", "<init>", "()V", "saveNotification", "Lkotlin/Result;", "", "title", "", "message", "severity", "saveNotification-BWLJW6A", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getNotificationHistory", "", "Lcom/project/insole/features/notifications/data/NotificationRecord;", "limit", "", "getNotificationHistory-gIAlu-s", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearOldNotifications", "hoursAgo", "clearOldNotifications-gIAlu-s", "app_debug"})
public final class NotificationDataSource {
    
    @javax.inject.Inject()
    public NotificationDataSource() {
        super();
    }
}