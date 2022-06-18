package com.tim.basevpn.delegate

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.extension.getActivity
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.CONFIG_EXTRA
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * ServiceConnection delegate
 *
 * @param lifecycleOwner For lifecycle events
 * @param config User configuration
 * @param clazz Class of VpnService
 * @param stateListener Receive state of connection
 *
 * @Author: Timur Hojatov
 */
class VpnConnectionServiceDelegate<T : Parcelable>(
    private val lifecycleOwner: LifecycleOwner,
    private val config: T,
    private val clazz: Class<out VpnService>,
    private val stateListener: ((ConnectionState) -> Unit)
) : ReadOnlyProperty<Any, VPNRunner> {

    private var isBound = false
    private val listener = object : IConnectionStateListener.Stub() {
        override fun stateChanged(status: ConnectionState?) {
            status?.let { state ->
                lifecycleOwner.lifecycleScope.launch {
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

    override fun getValue(thisRef: Any, property: KProperty<*>): VPNRunner {
        return object : VPNRunner {
            override fun start() {
                stateListener.invoke(ConnectionState.CONNECTING)
                when (lifecycleOwner) {
                    is Fragment -> {
                        lifecycleOwner.requireActivity().bind()
                    }
                    is Activity -> {
                        lifecycleOwner.bind()
                    }
                }
            }

            override fun stop() {
                vpnService?.stopVPN()
                when (lifecycleOwner) {
                    is Fragment -> {
                        lifecycleOwner.requireActivity().unbind()
                    }
                    is Activity -> {
                        lifecycleOwner.unbind()
                    }
                }
            }
        }
    }

    private fun Activity.bind() {
        isBound = bindService(
            createIntent(config),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun Activity.unbind() {
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
