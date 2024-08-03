package com.tim.basevpn.delegate

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.configuration.VpnConfiguration
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.ALLOWED_APPS_SET_EXTRA
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
    private val notificationClassName: String? = null,
    private val allowedApps: Set<String> = emptySet(),
    private val stateListener: ((ConnectionState) -> Unit),
    private val trafficListener: ((Long, Long, Long, Long) -> Unit)? = null
) : ReadOnlyProperty<Context, VPNRunner> {

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
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            stateListener.invoke(ConnectionState.DISCONNECTED)
            vpnService = null
        }
    }

    override fun getValue(thisRef: Context, property: KProperty<*>): VPNRunner {
        thisRef.bind(notificationClassName, allowedApps)
        return object : VPNRunner {

            override fun start(config: VpnConfiguration<*>) {
                vpnService?.startVPN()
            }

            override fun stop() {
                vpnService?.stopVPN()
            }
        }
    }

    fun Context.bind(
        notificationClassName: String? = null,
        allowedApps: Set<String>
    ) {
        bindService(
            createIntent(notificationClassName, allowedApps),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun Context.unbind() {
        unbindService(serviceConnection)
        stopService(Intent(this, clazz))
    }

    private fun Context.createIntent(
        notificationClassName: String? = null,
        allowedApps: Set<String>
    ) = Intent(
        this,
        clazz
    ).apply {
        putExtra(NOTIFICATION_IMPL_CLASS_KEY, notificationClassName)
        putExtra(ALLOWED_APPS_SET_EXTRA, allowedApps.toTypedArray())
    }
}
