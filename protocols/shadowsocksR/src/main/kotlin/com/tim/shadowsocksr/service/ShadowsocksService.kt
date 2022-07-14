package com.tim.shadowsocksr.service

import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.delegate.StateDelegate
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.CONFIG_EXTRA
import com.tim.basevpn.utils.sendCallback
import com.tim.notification.NotificationHelper
import com.tim.shadowsocksr.Native
import com.tim.basevpn.R
import com.tim.shadowsocksr.BuildConfig
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.shadowsocksr.config.ConfigWriter
import com.tim.shadowsocksr.thread.GuardedProcess
import com.tim.shadowsocksr.thread.ShadowsocksRThread
import com.tim.shadowsocksr.thread.TrafficMonitorThread

/**
 * @Author: Timur Hojatov
 */
internal class ShadowsocksService : VpnService() {

    private val dataDir: String by lazy {
        applicationInfo.dataDir
    }
    private val nativeDir: String by lazy {
        applicationInfo.nativeLibraryDir
    }
    private val protectPath: String by lazy {
        "${applicationInfo.dataDir}/protect_path"
    }
    private val statPath: String by lazy {
        "${applicationInfo.dataDir}/stat_path"
    }

    private val configWriter: ConfigWriter by lazy {
        ConfigWriter(config)
    }

    private val stateCallback by StateDelegate()

    private var shadowsocksRThread: ShadowsocksRThread? = null
    private var sslocalProcess: GuardedProcess? = null
    private var sstunnelProcess: GuardedProcess? = null
    private var pdnsdProcess: GuardedProcess? = null
    private var tun2socksProcess: GuardedProcess? = null

    private var trafficMonitorThread: TrafficMonitorThread? = null

    private var connection: ParcelFileDescriptor? = null

    private lateinit var config: ShadowsocksRVpnConfig

    private val notificationHelper by lazy {
        NotificationHelper(
            service = this,
            notificationManager = applicationContext.getSystemService(
                NOTIFICATION_SERVICE
            ) as NotificationManager
        )
    }

    private val binder = object : IVPNService.Stub() {
        override fun startVPN() {
            start()
        }

        override fun stopVPN() {
            stop()
        }

        override fun registerCallback(cb: IConnectionStateListener?) {
            stateCallback.register(cb)
        }

        override fun unregisterCallback(cb: IConnectionStateListener?) {
            stateCallback.unregister(cb)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        config = intent
            ?.extras
            ?.getParcelable(CONFIG_EXTRA)
            ?: return null
        notificationHelper.startNotification()
        return binder
    }

    override fun onRevoke() {
        stop()
        super.onRevoke()
    }

    private fun start() {
        trafficMonitorThread = TrafficMonitorThread(statPath).also { monitor ->
            monitor.start()
        }
        updateState(ConnectionState.CONNECTING)

        killProcesses()
        shadowsocksRThread = ShadowsocksRThread(
            protectPath = protectPath,
            protectFileDescriptor = { socket ->
                protect(socket)
            }
        ).also {
            it.start()
        }

        val fd = establish()
        if (!sendFileDescriptor(fd)) {
            if (BuildConfig.DEBUG) {
                Log.e("ShadowsocksService","sendFd failed")
            }
            stop()
            return
        }
        runTunnelProcesses()
        updateState(ConnectionState.CONNECTED)
    }

    private fun stop() {
        shadowsocksRThread?.stopThread()
        shadowsocksRThread = null

        updateState(ConnectionState.DISCONNECTING)

        killProcesses()

        notificationHelper.stopNotification()
        connection?.close()
        connection = null

        trafficMonitorThread?.stopThread()
        trafficMonitorThread = null
    }

    private fun establish(): Int {
        connection = Builder().apply {
            setSession(config.name)
            setMtu(CONNECTION_MTU)
            addAddress(ADDRESS_ROUTE, ADDRESS_PREFIX_LENGTH)
            addDnsServer(config.dnsAddress)

            resources.getStringArray(R.array.bypass_private_route)
                .associate { route ->
                    val split = route.split("/")
                    Pair(split[0], Integer.parseInt(split[1]))
                }.onEach { map ->
                    addRoute(map.key, map.value)
                }

            addRoute(config.dnsAddress, DNS_PREFIX_LENGTH)
        }.establish() ?: run {
            if (BuildConfig.DEBUG) {
                Log.e("ShadowsocksService", "No connection")
            }
            return -1
        }

        val descriptor = connection!!.fd
        tun2socksProcess = GuardedProcess(
            configWriter.buildTun2SocksCmd(
                fd = descriptor.toString(),
                dataDir = dataDir,
                nativeDir = nativeDir
            )
        ).start {
            sendFileDescriptor(descriptor)
        }
        return descriptor
    }

    private fun runTunnelProcesses() {
        configWriter.apply {
            printConfigsToFiles(dataDir, protectPath)
            sslocalProcess = GuardedProcess(buildShadowSocksDaemonCmd(dataDir, nativeDir)).start()
            pdnsdProcess = GuardedProcess(buildDnsDaemonCmd(dataDir, nativeDir)).start()
            sstunnelProcess = GuardedProcess(buildDnsTunnelCmd(dataDir, nativeDir)).start()
        }
    }

    private fun updateState(newState: ConnectionState) {
        stateCallback.sendCallback { it.stateChanged(newState) }
    }

    private fun sendFileDescriptor(fd: Int): Boolean {
        if (fd != -1) {
            var tries = 1
            while (tries < SEND_FILE_DESCRIPTOR_TRIES) {
                Thread.sleep(SEND_FILE_DESCRIPTOR_SLEEP * tries)
                if (Native.sendfd(fd, "$dataDir/sock_path") != -1) {
                    return true
                }
                tries += 1
            }
        }
        return false
    }

    private fun killProcesses() {
        sslocalProcess?.destroy()
        sslocalProcess = null
        sstunnelProcess?.destroy()
        sstunnelProcess = null
        tun2socksProcess?.destroy()
        tun2socksProcess = null
        pdnsdProcess?.destroy()
        pdnsdProcess = null
    }

    internal companion object {
        private const val CONNECTION_MTU = 1500
        private const val ADDRESS_ROUTE = "172.19.0.1"
        private const val ADDRESS_PREFIX_LENGTH = 24
        private const val DNS_PREFIX_LENGTH = 32

        private const val SEND_FILE_DESCRIPTOR_TRIES = 5
        private const val SEND_FILE_DESCRIPTOR_SLEEP = 1000L
    }
}
