package com.project.insole.core.notifications;

/**
 * Manages push notifications and local alerts for the insole app.
 * Handles both threshold alerts and system notifications.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0006\u0018\u0000 \u001c2\u00020\u0001:\u0001\u001cB\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\b\u0010\b\u001a\u00020\tH\u0002J(\u0010\n\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\f2\b\b\u0002\u0010\u000f\u001a\u00020\u0010J\u0018\u0010\u0011\u001a\u00020\t2\u0006\u0010\u0012\u001a\u00020\u00132\b\b\u0002\u0010\u0014\u001a\u00020\fJ\u0018\u0010\u0015\u001a\u00020\t2\u0006\u0010\u0016\u001a\u00020\u00172\b\b\u0002\u0010\u0014\u001a\u00020\fJ\u0006\u0010\u0018\u001a\u00020\tJ\u000e\u0010\u0019\u001a\u00020\t2\u0006\u0010\u001a\u001a\u00020\u0017J\u0006\u0010\u001b\u001a\u00020\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/project/insole/core/notifications/InsoleNotificationManager;", "", "context", "Landroid/content/Context;", "<init>", "(Landroid/content/Context;)V", "notificationManager", "Landroid/app/NotificationManager;", "createNotificationChannels", "", "sendPressureAlert", "title", "", "message", "affectedZone", "severity", "Lcom/project/insole/core/notifications/AlertSeverity;", "sendConnectionStatusNotification", "isConnected", "", "deviceName", "sendBatteryLowWarning", "batteryLevel", "", "sendCalibrationReminder", "cancelNotification", "notificationId", "cancelAllNotifications", "Companion", "app_debug"})
public final class InsoleNotificationManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final android.app.NotificationManager notificationManager = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_CRITICAL_ALERTS = "critical_alerts";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_GENERAL = "general_notifications";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_DEVICE_STATUS = "device_status";
    private static final int NOTIFICATION_ID_PRESSURE = 1001;
    private static final int NOTIFICATION_ID_CONNECTION = 1002;
    private static final int NOTIFICATION_ID_BATTERY = 1003;
    private static final int NOTIFICATION_ID_CALIBRATION = 1004;
    @org.jetbrains.annotations.NotNull()
    public static final com.project.insole.core.notifications.InsoleNotificationManager.Companion Companion = null;
    
    public InsoleNotificationManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Creates notification channels for different alert types (Android 8+).
     */
    private final void createNotificationChannels() {
    }
    
    /**
     * Sends critical pressure alert notification.
     */
    public final void sendPressureAlert(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.NotNull()
    java.lang.String affectedZone, @org.jetbrains.annotations.NotNull()
    com.project.insole.core.notifications.AlertSeverity severity) {
    }
    
    /**
     * Sends device connection status notification.
     */
    public final void sendConnectionStatusNotification(boolean isConnected, @org.jetbrains.annotations.NotNull()
    java.lang.String deviceName) {
    }
    
    /**
     * Sends battery low warning notification.
     */
    public final void sendBatteryLowWarning(int batteryLevel, @org.jetbrains.annotations.NotNull()
    java.lang.String deviceName) {
    }
    
    /**
     * Sends calibration reminder notification.
     */
    public final void sendCalibrationReminder() {
    }
    
    /**
     * Cancels a notification by ID.
     */
    public final void cancelNotification(int notificationId) {
    }
    
    /**
     * Cancels all notifications from the app.
     */
    public final void cancelAllNotifications() {
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/project/insole/core/notifications/InsoleNotificationManager$Companion;", "", "<init>", "()V", "CHANNEL_CRITICAL_ALERTS", "", "CHANNEL_GENERAL", "CHANNEL_DEVICE_STATUS", "NOTIFICATION_ID_PRESSURE", "", "NOTIFICATION_ID_CONNECTION", "NOTIFICATION_ID_BATTERY", "NOTIFICATION_ID_CALIBRATION", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}