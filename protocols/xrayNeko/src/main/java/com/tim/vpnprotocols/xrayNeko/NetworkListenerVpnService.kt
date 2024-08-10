package com.tim.vpnprotocols.xrayNeko

import android.annotation.TargetApi
import android.content.Intent
import android.net.ConnectivityManager
import android.net.DnsResolver
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.system.ErrnoException
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.tim.basevpn.singleProcess.ProtocolsVpnService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import libcore.ExchangeContext
import libcore.Libcore
import libcore.LocalDNSTransport
import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class NetworkListenerVpnService : ProtocolsVpnService() {

    private var mainHandler: Handler? = null
    private var fallback: Boolean = false
    private var request: NetworkRequest? = null
    private var connectivity: ConnectivityManager? = null

    private var networkActor: SendChannel<NetworkMessage>? = null
    private var callback: Callback? = null
    private var localDnsResolver: LocalDNSTransport? = null
    private var underlyingNetwork: Network? = null

    @OptIn(ObsoleteCoroutinesApi::class)
    override fun initDependencies(intent: Intent) {
        super.initDependencies(intent)
        mainHandler = Handler(Looper.getMainLooper())
        fallback = false
        connectivity = getSystemService()
        callback = Callback()

        request = NetworkRequest.Builder().apply {
            addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            if (Build.VERSION.SDK_INT == 23) {  // workarounds for OEM bugs
                removeCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                removeCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)
            }
        }.build()

        networkActor = lifecycleScope.actor<NetworkMessage>(Dispatchers.Unconfined) {
            val listeners = mutableMapOf<Any, (Network?) -> Unit>()
            var network: Network? = null
            val pendingRequests = arrayListOf<NetworkMessage.Get>()
            for (message in channel) when (message) {
                is NetworkMessage.Start -> {
                    if (listeners.isEmpty()) register()
                    listeners[message.key] = message.listener
                    if (network != null) message.listener(network)
                }

                is NetworkMessage.Get -> {
                    check(listeners.isNotEmpty()) { "Getting network without any listeners is not supported" }
                    if (network == null) pendingRequests += message else message.response.complete(
                        network
                    )
                }

                is NetworkMessage.Stop -> if (listeners.isNotEmpty() && // was not empty
                    listeners.remove(message.key) != null && listeners.isEmpty()
                ) {
                    network = null
                    unregister()
                }

                is NetworkMessage.Put -> {
                    network = message.network
                    pendingRequests.forEach { it.response.complete(message.network) }
                    pendingRequests.clear()
                    listeners.values.forEach { it(network) }
                }

                is NetworkMessage.Update -> if (network == message.network) listeners.values.forEach {
                    it(network)
                }

                is NetworkMessage.Lost -> if (network == message.network) {
                    network = null
                    listeners.values.forEach { it(null) }
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            startNetworkListener(Unit) {
                underlyingNetwork = it
            }
        }

        localDnsResolver = LocalResolverImpl()
    }

    override fun clearDependencies() {
        super.clearDependencies()
        stopNetworkListener(Unit)

        request = null
        localDnsResolver = null
        mainHandler?.removeCallbacks {}
        mainHandler = null
        networkActor?.close()
        networkActor = null
        fallback = false
        connectivity = null
        callback = null
        localDnsResolver = null
        underlyingNetwork = null
    }

    fun registerDns() {
        localDnsResolver?.let { Libcore.registerLocalDNSTransport(it) }
    }

    private suspend fun startNetworkListener(key: Any, listener: (Network?) -> Unit) =
        networkActor?.send(NetworkMessage.Start(key, listener))

    private fun stopNetworkListener(key: Any) {
        networkActor?.trySend(NetworkMessage.Stop(key))
    }

    /**
     * Unfortunately registerDefaultNetworkCallback is going to return VPN interface since Android P DP1:
     * https://android.googlesource.com/platform/frameworks/base/+/dda156ab0c5d66ad82bdcf76cda07cbc0a9c8a2e
     *
     * This makes doing a requestNetwork with REQUEST necessary so that we don't get ALL possible networks that
     * satisfies default network capabilities but only THE default network. Unfortunately, we need to have
     * android.permission.CHANGE_NETWORK_STATE to be able to call requestNetwork.
     *
     * Source: https://android.googlesource.com/platform/frameworks/base/+/2df4c7d/services/core/java/com/android/server/ConnectivityService.java#887
     */
    private fun register() {
        try {
            fallback = false
            when (Build.VERSION.SDK_INT) {
                in 31..Int.MAX_VALUE -> @TargetApi(31) {
                    let4(
                        connectivity,
                        request,
                        callback,
                        mainHandler
                    ) { connectivity, request, callback, mainHandler ->
                        connectivity.registerBestMatchingNetworkCallback(
                            request, callback, mainHandler
                        )
                    }
                }

                in 28 until 31 -> @TargetApi(28) {  // we want REQUEST here instead of LISTEN
                    let4(
                        connectivity,
                        request,
                        callback,
                        mainHandler
                    ) { connectivity, request, callback, mainHandler ->
                        connectivity.requestNetwork(request, callback, mainHandler)
                    }
                }

                in 26 until 28 -> @TargetApi(26) {
                    let3(
                        connectivity,
                        callback,
                        mainHandler
                    ) { connectivity, callback, mainHandler ->
                        connectivity.registerDefaultNetworkCallback(callback, mainHandler)
                    }
                }

                in 24 until 26 -> @TargetApi(24) {
                    let2(connectivity, callback) { connectivity, callback ->
                        connectivity.registerDefaultNetworkCallback(callback)
                    }
                }

                else -> {
                    let3(connectivity, callback, request) { connectivity, callback, request ->
                        connectivity.requestNetwork(request, callback)
                    }
                    // known bug on API 23: https://stackoverflow.com/a/33509180/2245107
                }
            }
        } catch (e: Exception) {
            //Logs.w(e)
            fallback = true
        }
    }

    private fun unregister() {
        let2(connectivity, callback) { connectivity, callback ->
            connectivity.unregisterNetworkCallback(callback)
        }
    }

    // NB: this runs in ConnectivityThread, and this behavior cannot be changed until API 26
    private inner class Callback : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            runBlocking {
                networkActor?.send(NetworkMessage.Put(network))
            }
        }

        override fun onCapabilitiesChanged(
            network: Network, networkCapabilities: NetworkCapabilities
        ) { // it's a good idea to refresh capabilities
            runBlocking {
                networkActor?.send(NetworkMessage.Update(network))
            }
        }

        override fun onLost(network: Network) {
            runBlocking {
                networkActor?.send(NetworkMessage.Lost(network))
            }
        }
    }

    private sealed class NetworkMessage {
        class Start(val key: Any, val listener: (Network?) -> Unit) : NetworkMessage()
        class Get : NetworkMessage() {
            val response = CompletableDeferred<Network>()
        }

        class Stop(val key: Any) : NetworkMessage()

        class Put(val network: Network) : NetworkMessage()
        class Update(val network: Network) : NetworkMessage()
        class Lost(val network: Network) : NetworkMessage()
    }

    private inner class LocalResolverImpl : LocalDNSTransport {

        // new local
        private val RCODE_NXDOMAIN = 3

        override fun raw(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun exchange(ctx: ExchangeContext, message: ByteArray) {
            return runBlocking {
                suspendCoroutine { continuation ->
                    val signal = CancellationSignal()
                    ctx.onCancel(signal::cancel)
                    val callback = object : DnsResolver.Callback<ByteArray> {
                        override fun onAnswer(answer: ByteArray, rcode: Int) {
                            // exchange don't generate rcode error
                            ctx.rawSuccess(answer)
                            continuation.resume(Unit)
                        }

                        override fun onError(error: DnsResolver.DnsException) {
                            when (val cause = error.cause) {
                                is ErrnoException -> {
                                    ctx.errnoCode(cause.errno)
                                    continuation.resume(Unit)
                                    return
                                }
                            }
                            continuation.tryResumeWithException(error)
                        }
                    }
                    DnsResolver.getInstance().rawQuery(
                        underlyingNetwork,
                        message,
                        DnsResolver.FLAG_NO_RETRY,
                        Dispatchers.IO.asExecutor(),
                        signal,
                        callback
                    )
                }
            }
        }

        override fun lookup(ctx: ExchangeContext, network: String, domain: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return runBlocking {
                    suspendCoroutine { continuation ->
                        val signal = CancellationSignal()
                        ctx.onCancel(signal::cancel)
                        val callback = object : DnsResolver.Callback<Collection<InetAddress>> {
                            override fun onAnswer(answer: Collection<InetAddress>, rcode: Int) {
                                if (rcode == 0) {
                                    ctx.success((answer as Collection<InetAddress?>).mapNotNull { it?.hostAddress }
                                        .joinToString("\n"))
                                } else {
                                    ctx.errorCode(rcode)
                                }
                                continuation.resume(Unit)
                            }

                            override fun onError(error: DnsResolver.DnsException) {
                                when (val cause = error.cause) {
                                    is ErrnoException -> {
                                        ctx.errnoCode(cause.errno)
                                        continuation.resume(Unit)
                                        return
                                    }
                                }
                                continuation.tryResumeWithException(error)
                            }
                        }
                        val type = when {
                            network.endsWith("4") -> DnsResolver.TYPE_A
                            network.endsWith("6") -> DnsResolver.TYPE_AAAA
                            else -> null
                        }
                        if (type != null) {
                            DnsResolver.getInstance().query(
                                underlyingNetwork,
                                domain,
                                type,
                                DnsResolver.FLAG_NO_RETRY,
                                Dispatchers.IO.asExecutor(),
                                signal,
                                callback
                            )
                        } else {
                            DnsResolver.getInstance().query(
                                underlyingNetwork,
                                domain,
                                DnsResolver.FLAG_NO_RETRY,
                                Dispatchers.IO.asExecutor(),
                                signal,
                                callback
                            )
                        }
                    }
                }
            } else {
                val answer = try {
                    val u = underlyingNetwork
                    if (u != null) {
                        u.getAllByName(domain)
                    } else {
                        InetAddress.getAllByName(domain)
                    }
                } catch (e: UnknownHostException) {
                    ctx.errorCode(RCODE_NXDOMAIN)
                    return
                }
                ctx.success(answer.mapNotNull { it.hostAddress }.joinToString("\n"))
            }
        }
    }

    private inline fun <T1 : Any, T2 : Any, R : Any> let2(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
        return if (p1 != null && p2 != null) block(p1, p2) else null
    }

    private inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> let3(
        p1: T1?,
        p2: T2?,
        p3: T3?,
        block: (T1, T2, T3) -> R?
    ): R? {
        return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
    }

    private inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> let4(
        p1: T1?,
        p2: T2?,
        p3: T3?,
        p4: T4?,
        block: (T1, T2, T3, T4) -> R?
    ): R? {
        return if (p1 != null && p2 != null && p3 != null && p4 != null) block(
            p1,
            p2,
            p3,
            p4
        ) else null
    }

    private fun <T> Continuation<T>.tryResumeWithException(exception: Throwable) {
        try {
            resumeWith(Result.failure(exception))
        } catch (ignored: IllegalStateException) {
        }
    }
}