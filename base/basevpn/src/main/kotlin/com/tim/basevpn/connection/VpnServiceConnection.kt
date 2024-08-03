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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

abstract class VpnServiceConnection(
    private val context: Context,
    private val clazz: Class<out Service>,
    private val stateListener: ((ConnectionState) -> Unit)? = null,
    private val coroutineScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()
    }
) : CoroutineScope by coroutineScope {

    private val mutex = Mutex()
    private var serviceConnection: ServiceConnection? = null
    private var vpnService: IVPNService? = null

    fun start(
        config: IVpnConfiguration,
        allowedApps: Set<String> = emptySet(),
        notificationClassName: String? = null
    ) {
        attachListener()

        launch {
            getService()?.startVPN()
        }
    }

    fun stop() {
        if (vpnService != null) {
            vpnService?.stopVPN()
        } else {
            launch {
                getService()?.apply {
                    stopVPN()
                }
            }
        }
        if (serviceConnection != null) {
            context.unbindService(serviceConnection!!)
        }
        if (vpnService != null) {
            vpnService = null
        }
    }

    suspend fun isConnected(): Boolean {
        return getService()?.let { it.state == ConnectionState.CONNECTED } ?: false
    }

    fun attachListener() {
        launch {
            val service = getService()
            if (service != null) {
                if (stateListener != null) {
                    attachStateListener(service)
                        .flowOn(Dispatchers.IO)
                        .collect {
                            stateListener.invoke(it)
                        }
                }
            }
        }
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
        if (forceStop) {
            stateListener?.invoke(ConnectionState.DISCONNECTING)
            stateListener?.invoke(ConnectionState.DISCONNECTED)
        }
        if (needStopService || forceStop) {
            context.stopService(Intent(context, clazz))
        }
        //cancel()
        serviceConnection = null
        vpnService = null
    }

    private fun attachStateListener(service: IVPNService) = callbackFlow<ConnectionState> {
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
        service.registerCallback(listener)
        service.state?.let {
            trySend(it)
        }


        awaitClose {
            service.unregisterCallback(listener)
        }
    }

    private suspend fun getService(): IVPNService? = mutex.withLock {
        suspendCancellableCoroutine<IVPNService?> { continuation ->
            if (serviceConnection == null || vpnService == null) {
                serviceConnection = object : ServiceConnection {
                    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                        if (continuation.isActive) {
                            try {
                                vpnService = IVPNService.Stub.asInterface(p1)
                                continuation.resume(vpnService)
                            } catch (e: Exception) {
                                continuation.resumeWithException(e)
                            }
                        }
                    }

                    override fun onServiceDisconnected(p0: ComponentName?) {
                        if (continuation.isActive) {
                            vpnService = null
                            continuation.resume(null)
                        }
                    }
                }
                if (continuation.isActive) {
                    try {
                        context.bindService(
                            Intent(context, clazz),
                            serviceConnection!!,
                            Context.BIND_AUTO_CREATE
                        )
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            } else {
                continuation.resume(vpnService)
            }

            continuation.invokeOnCancellation {
                try {
                    serviceConnection?.let { context.unbindService(it) }
                } catch (e: Exception) {
                    // Handle or log exception if needed
                } finally {
                    serviceConnection = null
                    vpnService = null
                }
            }
        }
    }
}