package com.project.insole.features.notifications.presentation;

/**
 * ViewModel for notifications screen.
 * Exposes immutable StateFlow for UI consumption.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0006\u0010\r\u001a\u00020\u000eJ\u0006\u0010\u000f\u001a\u00020\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u0010"}, d2 = {"Lcom/project/insole/features/notifications/presentation/NotificationsViewModel;", "Landroidx/lifecycle/ViewModel;", "notificationDataSource", "Lcom/project/insole/features/notifications/data/NotificationDataSource;", "<init>", "(Lcom/project/insole/features/notifications/data/NotificationDataSource;)V", "_notificationsState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/features/notifications/presentation/NotificationsUiState;", "notificationsState", "Lkotlinx/coroutines/flow/StateFlow;", "getNotificationsState", "()Lkotlinx/coroutines/flow/StateFlow;", "loadNotifications", "", "clearNotifications", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class NotificationsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.notifications.data.NotificationDataSource notificationDataSource = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.features.notifications.presentation.NotificationsUiState> _notificationsState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.project.insole.features.notifications.presentation.NotificationsUiState> notificationsState = null;
    
    @javax.inject.Inject()
    public NotificationsViewModel(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.notifications.data.NotificationDataSource notificationDataSource) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.project.insole.features.notifications.presentation.NotificationsUiState> getNotificationsState() {
        return null;
    }
    
    public final void loadNotifications() {
    }
    
    public final void clearNotifications() {
    }
}