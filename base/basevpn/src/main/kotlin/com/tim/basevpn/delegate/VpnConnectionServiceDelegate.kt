package com.tim.basevpn.delegate

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcelable
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.ALLOWED_APPS_SET_EXTRA
import com.tim.basevpn.utils.CONFIG_EXTRA
import com.tim.basevpn.utils.NOTIFICATION_IMPL_CLASS_KEY
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * ServiceConnection delegate
 *
 * @param clazz Class of VpnService
 * @param stateListener Receive state of connection
 *
 * @Author: Timur Hojatov
 */
class VpnConnectionServiceDelegate(
    private val clazz: Class<out VpnService>,
    private val stateListener: ((ConnectionState) -> Unit),
    private val trafficListener: ((Long, Long, Long, Long) -> Unit)? = null
) : ReadOnlyProperty<Context, VPNRunner> {

    private var isBound = false
    private val listener = object : IConnectionStateListener.Stub() {
        override fun stateChanged(status: ConnectionState?) {
            status?.let { state ->
                Handler(Looper.getMainLooper()).post {
                    stateListener.invoke(state)
                }
            }
        }

        override fun trafficUpdate(txRate: Long, rxRate: Long, txTotal: Long, rxTotal: Long) {
            trafficListener?.invoke(txRate, rxRate, txTotal, rxTotal)
        }
    }

    private var vpnService: IVPNService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            vpnService = IVPNService.Stub.asInterface(p1)
            vpnService?.registerCallback(listener)
            vpnService?.startVPN()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            stateListener.invoke(ConnectionState.DISCONNECTED)
            vpnService = null
        }
    }

    override fun getValue(thisRef: Context, property: KProperty<*>): VPNRunner {
        return object : VPNRunner {

            override fun <T : Parcelable> start(
                config: T,
                notificationClassName: String?,
                allowedApps: Set<String>
            ) {
                stateListener.invoke(ConnectionState.CONNECTING)
                thisRef.bind(config, notificationClassName, allowedApps)
            }

            override fun stop() {
                vpnService?.stopVPN()
                thisRef.unbind()
            }
        }
    }

    private fun <T: Parcelable> Context.bind(
        config: T,
        notificationClassName: String? = null,
        allowedApps: Set<String>
    ) {
        isBound = bindService(
            createIntent(config, notificationClassName, allowedApps),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun Context.unbind() {
        if (isBound) {
            unbindService(serviceConnection)
            stopService(Intent(this, clazz))
            isBound = false
        }
    }

    private fun <T: Parcelable> Context.createIntent(
        config: T,
        notificationClassName: String? = null,
        allowedApps: Set<String>
    ) = Intent(
        this,
        clazz
    ).apply {
        putExtra(CONFIG_EXTRA, config)
        putExtra(NOTIFICATION_IMPL_CLASS_KEY, notificationClassName)
        putExtra(ALLOWED_APPS_SET_EXTRA, allowedApps.toTypedArray())
    }
}
