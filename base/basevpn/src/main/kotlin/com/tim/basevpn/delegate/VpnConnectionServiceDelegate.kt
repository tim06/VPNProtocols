package com.tim.basevpn.delegate

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.launch
import androidx.lifecycle.lifecycleScope
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.permission.VpnActivityResultContract
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.CONFIG_EXTRA
import kotlinx.coroutines.launch
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
    activityResultRegistryOwner: ActivityResultRegistryOwner,
    private val config: T,
    private val clazz: Class<out VpnService>,
    private val stateListener: ((ConnectionState) -> Unit)
) : ReadOnlyProperty<Any, VPNRunner> {

    private val activity: ComponentActivity = activityResultRegistryOwner as ComponentActivity

    private var vpnPermission: ActivityResultLauncher<Unit>? = null

    private val vpnRunner = object : VPNRunner {
        override fun start() {
            vpnPermission?.launch()
        }

        override fun stop() {
            vpnService?.stopVPN()
            activity.unbind()
        }
    }

    private var isBound = false
    private val listener = object : IConnectionStateListener.Stub() {
        override fun stateChanged(status: ConnectionState?) {
            status?.let { state ->
                activity.lifecycleScope.launch {
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

    init {
        vpnPermission = activityResultRegistryOwner.activityResultRegistry.register(
            VPN_PERMISSION_RESULT_KEY,
            VpnActivityResultContract()
        ) { isGranted ->
            if (isGranted) {
                stateListener.invoke(ConnectionState.CONNECTING)
                activity.bind()
            } else {
                stateListener.invoke(ConnectionState.PERMISSION_NOT_GRANTED)
            }
        }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): VPNRunner {
        return vpnRunner
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

    private companion object {
        private const val VPN_PERMISSION_RESULT_KEY = "VPN_PERMISSION_RESULT_KEY"
    }
}
