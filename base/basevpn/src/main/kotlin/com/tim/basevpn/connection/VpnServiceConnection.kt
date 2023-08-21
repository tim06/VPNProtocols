package com.tim.basevpn.connection

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.tim.basevpn.IVPNService
import com.tim.basevpn.configuration.IVpnConfiguration
import com.tim.basevpn.configuration.VpnConfiguration
import com.tim.basevpn.state.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

abstract class VpnServiceConnection(
    private val context: Context,
    private val clazz: Class<out Service>,
    private val stateListener: ((ConnectionState) -> Unit)? = null,
    private val coroutineScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()
    }
) : CoroutineScope by coroutineScope {

    private var serviceConnection: ServiceConnection? = null
    private var vpnService: IVPNService? = null

    init {
        if (stateListener != null) {
            launch {
                attachStateListener()
                    .flowOn(Dispatchers.IO)
                    .collect {
                        stateListener.invoke(it)
                    }
            }
        }
    }

    fun start(
        config: IVpnConfiguration,
        allowedApps: Set<String> = emptySet(),
        notificationClassName: String? = null
    ) = launch {
        getService()?.apply {
            startVPN(
                VpnConfiguration(
                    data = config,
                    allowedApps = allowedApps,
                    notificationClassName = notificationClassName
                )
            )
        }
    }

    fun stop() = launch {
        getService()?.apply {
            stopVPN()
        }
    }

    suspend fun isConnected(): Boolean {
        return getService()?.let { it.state == ConnectionState.CONNECTED } ?: false
    }

    fun stopServiceIfNeed(forceStop: Boolean = false) {
        val needStopService = vpnService?.state != ConnectionState.CONNECTED
        if (needStopService || forceStop) {
            vpnService?.stopVPN()
        }
        runCatching {
            serviceConnection?.let {
                context.unbindService(it)
            }
        }
        if (needStopService || forceStop) {
            context.stopService(Intent(context, clazz))
        }
        cancel()
        serviceConnection = null
        vpnService = null
    }

    private fun attachStateListener() = callbackFlow<ConnectionState> {
        val serv = getService()
        val listener = object : ConnectionListener() {
            override fun stateChanged(status: ConnectionState) {
                trySend(status)
            }

            override fun trafficUpdate(
                txRate: Long,
                rxRate: Long,
                txTotal: Long,
                rxTotal: Long
            ) = Unit
        }
        serv?.registerCallback(listener)

        awaitClose {
            serv?.unregisterCallback(listener)
        }
    }

    private suspend fun getService() = suspendCancellableCoroutine<IVPNService?> { continuation ->
        if (serviceConnection == null || vpnService == null) {
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                    vpnService = IVPNService.Stub.asInterface(p1).also {
                        continuation.resume(it)
                    }
                }

                override fun onServiceDisconnected(p0: ComponentName?) {
                    vpnService = null
                    continuation.resume(null)
                }
            }
            context.bindService(
                Intent(context, clazz),
                serviceConnection!!,
                Context.BIND_AUTO_CREATE
            )
        } else {
            vpnService?.let { continuation.resume(it) }
        }
    }

}