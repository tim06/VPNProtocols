package com.tim.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build

class DefaultVpnServiceNotification(
    private val service: Service,
    private val notificationManager: NotificationManager
): VpnServiceNotification {

    override fun start() {
        val notification = createNotification("")
        service.startForeground(NOTIFICATION_ID, notification)
    }
    //@Suppress("DEPRECATION")
    override fun stop() {
        service.stopForeground(true)
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
        return builder
            .setWhen(0)
            .setSmallIcon(R.drawable.ic_key)
            //.setColor(ContextCompat.getColor(applicationContext, R.color.material_accent_500))
            //.setTicker(getString(R.string.vpn_started))
            .setContentTitle("VPN Service")
            .build()
        /*.setContentIntent(
            PendingIntent.getActivity(
                this, 0, Intent(this, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0
            )
        )*/
        //.setSmallIcon(R.drawable.ic_icon_24)
    }

    override fun updateNotification(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        private const val NOTIFICATION_ID = 99998
        private const val CHANNEL_ID = "CHANNEL_ID"
        private const val CHANNEL_NAME = "CHANNEL_NAME"
    }
}
