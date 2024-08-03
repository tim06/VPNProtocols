package com.tim.basevpn.notification

import android.app.NotificationManager
import android.app.Service
import android.net.VpnService.NOTIFICATION_SERVICE
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tim.basevpn.lifecycle.VpnLifecycleService
import com.tim.notification.DefaultVpnServiceNotification
import com.tim.notification.VpnServiceNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration.Companion.minutes

interface VpnNotification {

    fun initNotification(
        vpnNotificationClass: String?,
        onFailure: (exception: Throwable) -> Unit
    )

    fun start()

    fun stop()

    fun clear()
}

private class VpnNotificationImpl(
    private val service: VpnLifecycleService,
    private val onClear: () -> Unit
) : VpnNotification {

    private var notificationHelper: VpnServiceNotification? = null

    override fun initNotification(
        vpnNotificationClass: String?,
        onFailure: (exception: Throwable) -> Unit
    ) {
        val notificationManager = service.applicationContext.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        val helper = runCatching {
            val cl = Class.forName(vpnNotificationClass)
            val clazz = cl.getConstructor(
                Service::class.java,
                NotificationManager::class.java
            ).newInstance(this, notificationManager) as VpnServiceNotification
            //runCustomNotificationUpdater(clazz)
            clazz
        }.onFailure(onFailure)
            .getOrDefault(
                defaultValue = DefaultVpnServiceNotification(
                    service = service,
                    notificationManager = notificationManager
                )
            )
        this.notificationHelper = helper
    }

    override fun start() {
        notificationHelper?.start()
    }

    override fun stop() {
        notificationHelper?.stop()
    }

    override fun clear() {
        notificationHelper = null
        onClear.invoke()
    }

    private fun runCustomNotificationUpdater(notificationHelper: VpnServiceNotification) {
        service.lifecycleScope.launch(Dispatchers.IO) {
            service.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    notificationHelper.updateNotification(notificationHelper.createNotification(""))
                    delay(1.minutes)
                }
            }
        }
    }
}

class VpnNotificationDelegate : ReadOnlyProperty<VpnLifecycleService, VpnNotification> {

    private var vpnNotification: VpnNotification? = null
    private var observer = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            vpnNotification?.clear()
            vpnNotification = null
        }
    }

    override fun getValue(thisRef: VpnLifecycleService, property: KProperty<*>): VpnNotification {
        thisRef.lifecycle.removeObserver(observer)
        thisRef.lifecycle.addObserver(observer)
        return vpnNotification ?: VpnNotificationImpl(
            service = thisRef,
            onClear = {
                thisRef.lifecycle.removeObserver(observer)
            }
        ).also {
            vpnNotification = it
        }
    }
}

fun notificationDelegate() = VpnNotificationDelegate()