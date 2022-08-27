package com.tim.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build

class NotificationHelper(
    private val service: Service,
    private val notificationManager: NotificationManager
) {

    fun startNotification() {
        service.startForeground(1, createNotificationBuilder())
    }
    @Suppress("DEPRECATION")
    fun stopNotification() {
        service.stopForeground(true)
        notificationManager.cancel(1)
    }

    @Suppress("DEPRECATION")
    private fun createNotificationBuilder(): Notification {
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
            .setContentTitle("VPN Profile")
            .build()
        /*.setContentIntent(
            PendingIntent.getActivity(
                this, 0, Intent(this, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0
            )
        )*/
        //.setSmallIcon(R.drawable.ic_icon_24)
    }

    companion object {
        const val CHANNEL_ID = "CHANNEL_ID"
        const val CHANNEL_NAME = "CHANNEL_NAME"
    }
}
