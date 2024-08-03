package com.tim.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.ServiceCompat

class DefaultVpnServiceNotification(
    private val service: Service,
    private val notificationManager: NotificationManager
) : VpnServiceNotification {

    override fun withTimer(): Boolean = false

    override fun start() {
        val notification = createNotification("")
        ServiceCompat.startForeground(
            service,
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
    }

    //@Suppress("DEPRECATION")
    override fun stop() {
        ServiceCompat.stopForeground(service, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.cancel(NOTIFICATION_ID)
    }

    @Suppress("DEPRECATION")
    override fun createNotification(description: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MIN
                )
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(service, CHANNEL_ID)
        } else {
            Notification.Builder(service)
        }
        val packageName = service.applicationContext.packageName
        val launchIntent = service.packageManager.getLaunchIntentForPackage(packageName)
        return builder
            .setWhen(0)
            .setSmallIcon(R.drawable.ic_key)
            //.setColor(ContextCompat.getColor(applicationContext, R.color.material_accent_500))
            //.setTicker(getString(R.string.vpn_started))
            .setContentTitle("VPN Service")
            .setContentText(description)
            .setContentIntent(
                PendingIntent.getActivity(
                    service, 0, launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        //.setSmallIcon(R.drawable.ic_icon_24)
    }

    override fun updateNotification(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 99995
        const val CHANNEL_ID = "CHANNEL_ID"
        private const val CHANNEL_NAME = "CHANNEL_NAME"
    }
}
