package com.tim.basevpn.singleProcess

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tim.basevpn.intent.parseNotificationClass
import com.tim.basevpn.lifecycle.VpnLifecycleService
import com.tim.basevpn.logger.Logger
import com.tim.notification.DefaultVpnServiceNotification
import com.tim.notification.VpnServiceNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

abstract class NotificationVpnService : VpnLifecycleService() {

    private var notificationHelper: VpnServiceNotification? = null
    private var logger: Logger? = null

    open override fun initDependencies(intent: Intent) {
        super.initDependencies(intent)
        logger = Logger("${this::class.simpleName}:NotificationVpnService: ")
        logger?.d("initDependencies()")
        notificationHelper = initNotification(intent.parseNotificationClass())
    }

    open override fun clearDependencies() {
        super.clearDependencies()
        logger?.d("clearDependencies()")
        logger = null
        notificationHelper = null
    }

    fun showNotification() {
        logger?.d("showNotification()")
        notificationHelper?.start()
        val enableTimerLoop = notificationHelper?.withTimer() ?: false
        if (enableTimerLoop) {
            notificationHelper?.let { runCustomNotificationUpdater(it) }
        }
    }

    fun stopNotification() {
        logger?.d("stopNotification()")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        notificationHelper?.stop()
    }

    fun updateNotification(notification: Notification) {
        logger?.d("updateNotification()")
        notificationHelper?.updateNotification(notification)
    }

    private fun initNotification(vpnNotificationClass: String?): VpnServiceNotification {
        val notificationManager = applicationContext.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        val helper = runCatching {
            val cl = Class.forName(vpnNotificationClass)
            cl.getConstructor(
                Service::class.java,
                NotificationManager::class.java
            ).newInstance(this, notificationManager) as VpnServiceNotification
        }.onFailure {
            logger?.d(it.message.orEmpty())
        }.getOrDefault(
            defaultValue = DefaultVpnServiceNotification(
                service = this,
                notificationManager = notificationManager
            )
        )
        logger?.d("Init notification with class: ${helper::class.simpleName}")
        return helper
    }

    private fun runCustomNotificationUpdater(notificationHelper: VpnServiceNotification) {
        lifecycleScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    notificationHelper.updateNotification(notificationHelper.createNotification(""))
                    delay(1.minutes)
                }
            }
        }
    }
}