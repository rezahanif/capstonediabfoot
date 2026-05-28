package com.project.insole.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.project.insole.R

/**
 * Manages push notifications and local alerts for the insole app.
 * Handles both threshold alerts and system notifications.
 */
class InsoleNotificationManager(private val context: Context) {

    private val notificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    /**
     * Creates notification channels for different alert types (Android 8+).
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Critical pressure alerts channel
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL_ALERTS,
                "Critical Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical pressure and health alerts"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(criticalChannel)

            // General notifications channel
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(generalChannel)

            // Device status channel
            val deviceChannel = NotificationChannel(
                CHANNEL_DEVICE_STATUS,
                "Device Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Device connection and battery status"
            }
            notificationManager.createNotificationChannel(deviceChannel)
        }
    }

    /**
     * Sends critical pressure alert notification.
     */
    fun sendPressureAlert(
        title: String,
        message: String,
        affectedZone: String,
        severity: AlertSeverity = AlertSeverity.WARNING
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_CRITICAL_ALERTS)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .apply {
                when (severity) {
                    AlertSeverity.CRITICAL -> {
                        setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
                        setLights(0xFF0000, 1000, 1000)
                    }
                    AlertSeverity.WARNING -> {
                        setVibrate(longArrayOf(0, 250, 250, 250))
                    }
                    else -> {}
                }
            }
            .build()

        notificationManager.notify(
            NOTIFICATION_ID_PRESSURE + affectedZone.hashCode(),
            notification
        )
    }

    /**
     * Sends device connection status notification.
     */
    fun sendConnectionStatusNotification(
        isConnected: Boolean,
        deviceName: String = "Insole Device"
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val (title, message, icon) = if (isConnected) {
            Triple(
                "Connected",
                "$deviceName is now connected",
                android.R.drawable.ic_dialog_info
            )
        } else {
            Triple(
                "Disconnected",
                "$deviceName connection lost",
                android.R.drawable.ic_dialog_alert
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_DEVICE_STATUS)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(icon)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID_CONNECTION, notification)
    }

    /**
     * Sends battery low warning notification.
     */
    fun sendBatteryLowWarning(batteryLevel: Int, deviceName: String = "Insole Device") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_DEVICE_STATUS)
            .setContentTitle("Low Battery")
            .setContentText("$deviceName battery at $batteryLevel%")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500))
            .build()

        notificationManager.notify(NOTIFICATION_ID_BATTERY, notification)
    }

    /**
     * Sends calibration reminder notification.
     */
    fun sendCalibrationReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setContentTitle("Calibration Needed")
            .setContentText("Your insole device needs recalibration. Tap to calibrate.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID_CALIBRATION, notification)
    }

    /**
     * Cancels a notification by ID.
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * Cancels all notifications from the app.
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    companion object {
        private const val CHANNEL_CRITICAL_ALERTS = "critical_alerts"
        private const val CHANNEL_GENERAL = "general_notifications"
        private const val CHANNEL_DEVICE_STATUS = "device_status"

        private const val NOTIFICATION_ID_PRESSURE = 1001
        private const val NOTIFICATION_ID_CONNECTION = 1002
        private const val NOTIFICATION_ID_BATTERY = 1003
        private const val NOTIFICATION_ID_CALIBRATION = 1004
    }
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}
