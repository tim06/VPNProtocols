package com.tim.vpnprotocols.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.tim.notification.R
import com.tim.notification.VpnServiceNotification
import com.tim.vpnprotocols.view.MainViewActivity

class VpnNotificationImpl(
    private val service: Service,
    private val notificationManager: NotificationManager
) : VpnServiceNotification {

    override fun withTimer(): Boolean = false

    override fun start() {
        val notification = createNotification("")
        service.startForeground(NOTIFICATION_ID, notification)
    }

    override fun stop() {
        ServiceCompat.stopForeground(service, Service.STOP_FOREGROUND_REMOVE)
        notificationManager.cancel(NOTIFICATION_ID)
    }

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
        val builder = NotificationCompat.Builder(service, CHANNEL_ID)
        return builder
            .setWhen(0)
            .setSmallIcon(R.drawable.ic_key)
            //.setColor(ContextCompat.getColor(applicationContext, R.color.material_accent_500))
            //.setTicker(getString(R.string.vpn_started))
            .setContentTitle("VPN Protocols app")
            .setContentIntent(
                PendingIntent.getActivity(
                    service.applicationContext,
                    0,
                    Intent(service.applicationContext, MainViewActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        //.setSmallIcon(R.drawable.ic_icon_24)
    }

    override fun updateNotification(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        private const val NOTIFICATION_ID = 99995
        private const val CHANNEL_ID = "CHANNEL_ID"
        private const val CHANNEL_NAME = "CHANNEL_NAME"
    }
}