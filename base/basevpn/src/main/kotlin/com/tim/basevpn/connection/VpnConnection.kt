package com.tim.basevpn.connection

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.tim.basevpn.IVPNService
import com.tim.basevpn.configuration.IVpnConfiguration
import com.tim.basevpn.configuration.VpnConfiguration
import com.tim.basevpn.delegate.VPNRunner
import com.tim.basevpn.state.ConnectionState

abstract class VpnConnection<T : Service>(
    private val context: Context,
    private val clazz: Class<T>,
    private val stateListener: ((ConnectionState) -> Unit)? = null
) : VPNRunner {

    private var vpnService: IVPNService? = null

    private var connectionListener: ConnectionListener? = null

    private var postInitialState: Boolean = true

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            vpnService = IVPNService.Stub.asInterface(p1)
            connectionListener?.let { listener ->
                vpnService?.apply {
                    registerCallback(listener)
                    if (postInitialState) {
                        listener.stateChanged(state)
                    }
                }
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            vpnService = null
        }
    }

    init {
        if (stateListener != null) {
            connectionListener = object : ConnectionListener() {
                override fun stateChanged(status: ConnectionState) {
                    Handler(Looper.getMainLooper()).post {
                        stateListener.invoke(status)
                    }
                }

                override fun trafficUpdate(
                    txRate: Long,
                    rxRate: Long,
                    txTotal: Long,
                    rxTotal: Long
                ) = Unit
            }
        }
    }

    override fun start(config: VpnConfiguration<*>) {
        vpnService?.startVPN(config)
    }

    override fun stop() {
        vpnService?.stopVPN()
    }

    fun start(
        config: IVpnConfiguration,
        allowedApps: Set<String> = emptySet(),
        notificationClassName: String? = null
    ) {
        start(
            config = VpnConfiguration(
                data = config,
                allowedApps = allowedApps,
                notificationClassName = notificationClassName
            )
        )
    }

    fun bindService(withPostInitialState: Boolean) {
        postInitialState = withPostInitialState
        context.bindService(
            Intent(context, clazz),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun stopServiceIfNeed() {
        connectionListener?.let { listener ->
            vpnService?.unregisterCallback(listener)
        }
        val needStopService = vpnService?.state != ConnectionState.CONNECTED
        if (needStopService) {
            vpnService?.stopVPN()
        }
        context.unbindService()
        if (needStopService) {
            context.stopService(Intent(context, clazz))
        }
    }

    fun clear() {
        connectionListener = null
        vpnService = null
    }

    private fun Context.unbindService() {
        runCatching {
            unbindService(serviceConnection)
        }
    }
}