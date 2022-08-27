package com.tim.basevpn.delegate

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcelable
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.permission.isVpnPermissionGranted
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.CONFIG_EXTRA
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * ServiceConnection delegate
 *
 * @param config User configuration
 * @param clazz Class of VpnService
 * @param stateListener Receive state of connection
 *
 * @Author: Timur Hojatov
 */
class VpnConnectionServiceDelegate<T : Parcelable>(
    private val config: T,
    private val clazz: Class<out VpnService>,
    private val stateListener: ((ConnectionState) -> Unit)
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
            override fun start() {
                if (thisRef.isVpnPermissionGranted()) {
                    stateListener.invoke(ConnectionState.CONNECTING)
                    thisRef.bind()
                } else {
                    when (thisRef) {
                        is Activity -> {
                            thisRef.startActivityForResult(Intent(VpnService.prepare(thisRef)), 0)
                        }
                        else -> throw IllegalArgumentException("Not implemented type of context: $thisRef")
                    }
                    stateListener.invoke(ConnectionState.PERMISSION_NOT_GRANTED)
                }
            }

            override fun stop() {
                vpnService?.stopVPN()
                thisRef.unbind()
            }
        }
    }

    private fun Context.bind() {
        isBound = bindService(
            createIntent(config),
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

    private fun Context.createIntent(config: T) = Intent(
        this,
        clazz
    ).putExtra(CONFIG_EXTRA, config)
}
