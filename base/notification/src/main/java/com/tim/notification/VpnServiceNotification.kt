package com.tim.notification

import android.app.Notification

interface VpnServiceNotification {

    fun withTimer(): Boolean

    fun start()

    fun stop()

    fun createNotification(description: String): Notification

    fun updateNotification(notification: Notification)
}