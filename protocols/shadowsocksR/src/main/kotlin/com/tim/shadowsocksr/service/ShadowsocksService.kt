package com.tim.shadowsocksr.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.util.Log
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.R
import com.tim.basevpn.delegate.StateDelegate
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.ALLOWED_APPS_SET_EXTRA
import com.tim.basevpn.utils.CONFIG_EXTRA
import com.tim.basevpn.utils.NOTIFICATION_IMPL_CLASS_KEY
import com.tim.basevpn.utils.sendCallback
import com.tim.notification.DefaultVpnServiceNotification
import com.tim.notification.VpnServiceNotification
import com.tim.shadowsocksr.Native
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.shadowsocksr.config.ConfigWriter
import com.tim.shadowsocksr.log.ShadowsocksRLogger
import com.tim.shadowsocksr.thread.GuardedProcess
import com.tim.shadowsocksr.thread.ShadowsocksRThread
import com.tim.shadowsocksr.thread.TrafficMonitorThread
import com.tim.shadowsocksr.utils.TrafficMonitor
import java.util.Timer
import java.util.TimerTask

/**
 * @Author: Timur Hojatov
 */
class ShadowsocksService : VpnService() {

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

    private var timer: Timer? = null
    private var trafficMonitorThread: TrafficMonitorThread? = null

    private var connection: ParcelFileDescriptor? = null

    private lateinit var config: ShadowsocksRVpnConfig

    private var notificationHelper: VpnServiceNotification? = null

    private var allowedApps: Array<String>? = null

    private val binder = object : IVPNService.Stub() {
        override fun startVPN() {
            start()
        }

        override fun stopVPN() {
            stop()
        }

        override fun registerCallback(cb: IConnectionStateListener?) {
            stateCallback.register(cb)
            if (cb != null && stateCallback.register(cb)) {
                if (stateCallback.registeredCallbackCount != 0 && timer == null) {
                    val task = object : TimerTask() {
                        override fun run() {
                            if (TrafficMonitor.updateRate()) {
                                updateTrafficRate()
                                notificationHelper?.run {
                                    val received = TrafficMonitor.formatTraffic(TrafficMonitor.txRate)
                                    val send = TrafficMonitor.formatTraffic(TrafficMonitor.rxRate)
                                    val stat = "↓ $received    ↑ $send"
                                    updateNotification(createNotification(stat))
                                }
                            }
                        }
                    }
                    timer = Timer(true)
                    timer!!.schedule(task, 1000, 1000)
                }
                TrafficMonitor.updateRate()
                try {
                    cb.trafficUpdate(
                        TrafficMonitor.txRate,
                        TrafficMonitor.rxRate,
                        TrafficMonitor.txTotal,
                        TrafficMonitor.rxTotal
                    )
                } catch (e: RemoteException) {
                    ShadowsocksRLogger.e("ShadowsocksService", "registerCallback: $e")
                    //ShadowsocksApplication.app.track(e)
                }
            }
        }

        override fun unregisterCallback(cb: IConnectionStateListener?) {
            stateCallback.unregister(cb)
        }
    }

    @Suppress("DEPRECATION")
    override fun onBind(intent: Intent?): IBinder? {
        config = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            intent
                ?.extras
                ?.getParcelable(CONFIG_EXTRA)
                ?: return null
        } else {
            intent
                ?.extras
                ?.getParcelable(CONFIG_EXTRA, ShadowsocksRVpnConfig::class.java)
                ?: return null
        }
        initNotification(intent.extras?.getString(NOTIFICATION_IMPL_CLASS_KEY)).run {
            start()
        }
        allowedApps = intent.extras?.getStringArray(ALLOWED_APPS_SET_EXTRA)
        return binder
    }

    override fun onRevoke() {
        stop()
        super.onRevoke()
    }

    private fun start() {
        Log.d("ShadowsocksService", "start()")

        TrafficMonitor.reset()
        trafficMonitorThread = TrafficMonitorThread(
            statPath = statPath,
            update = { send, received ->
                //stateCallback.sendCallback { it.trafficUpdate(send, received) }
            }
        ).also { monitor ->
            monitor.startThread()
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
            Log.e("ShadowsocksService", "sendFd failed")
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

        notificationHelper?.stop()
        connection?.close()
        connection = null

        //updateTrafficTotal(TrafficMonitor.txTotal, TrafficMonitor.rxTotal)
        TrafficMonitor.reset()
        trafficMonitorThread?.stopThread()
        trafficMonitorThread = null
        timer?.cancel()
        timer = null

        updateState(ConnectionState.DISCONNECTED)
    }

    private fun establish(): Int {
        connection = Builder().apply {
            setSession(config.name.orEmpty())
            setMtu(CONNECTION_MTU)
            addAddress(ADDRESS_ROUTE, ADDRESS_PREFIX_LENGTH)
            addDnsServer(config.dnsAddress.orEmpty())

            resources.getStringArray(R.array.bypass_private_route)
                .associate { route ->
                    val split = route.split("/")
                    Pair(split[0], Integer.parseInt(split[1]))
                }.onEach { map ->
                    addRoute(map.key, map.value)
                }

            addRoute(config.dnsAddress.orEmpty(), DNS_PREFIX_LENGTH)

            allowedApps?.forEach {
                addAllowedApplication(it)
            }
        }.establish() ?: run {
            Log.e("ShadowsocksService", "No connection")
            return -1
        }

        val descriptor = connection!!.fd
        tun2socksProcess = GuardedProcess(
            configWriter.buildTun2SocksCmd(
                fd = descriptor.toString(),
                dataDir = dataDir,
                nativeDir = nativeDir
            )
        ).apply {
            start {
                sendFileDescriptor(descriptor)
            }
        }
        return descriptor
    }

    private fun runTunnelProcesses() {
        configWriter.apply {
            printConfigsToFiles(dataDir, protectPath)
            sslocalProcess =
                GuardedProcess(buildShadowSocksDaemonCmd(dataDir, nativeDir)).apply { start() }
            pdnsdProcess = GuardedProcess(buildDnsDaemonCmd(dataDir, nativeDir)).apply { start() }
            sstunnelProcess =
                GuardedProcess(buildDnsTunnelCmd(dataDir, nativeDir)).apply { start() }
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

    private fun initNotification(vpnNotificationClass: String?): VpnServiceNotification {
        val notificationManager = applicationContext.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        val helper = runCatching {
            val cl = Class.forName(vpnNotificationClass)
            cl.getConstructor(
                Service::class.java,
                NotificationManager::class.java
            ).newInstance(this, notificationManager) as VpnServiceNotification
        }.onFailure {
            ShadowsocksRLogger.e("ShadowsocksService", it.message.orEmpty())
        }.getOrDefault(
            defaultValue = DefaultVpnServiceNotification(
                service = this,
                notificationManager = notificationManager
            )
        )
        this.notificationHelper = helper
        return helper
    }

    private fun updateTrafficRate() {
        //handler.post {
        if (stateCallback.registeredCallbackCount > 0) {
            val txRate = TrafficMonitor.txRate
            val rxRate = TrafficMonitor.rxRate
            val txTotal = TrafficMonitor.txTotal
            val rxTotal = TrafficMonitor.rxTotal
            stateCallback.sendCallback {
                it.trafficUpdate(txRate, rxRate, txTotal, rxTotal)
            }
            /*val n = stateCallback.beginBroadcast()
            for (i in 0 until n) {
                try {

                    stateCallback.getBroadcastItem(i)
                        .trafficUpdated(txRate, rxRate, txTotal, rxTotal)
                } catch (e: Exception) {
                    // Ignore
                }

            }
            callbacks.finishBroadcast()*/
        }
        //}
    }

    companion object {
        private const val CONNECTION_MTU = 1500
        private const val ADDRESS_ROUTE = "172.19.0.1"
        private const val ADDRESS_PREFIX_LENGTH = 24
        private const val DNS_PREFIX_LENGTH = 32

        private const val SEND_FILE_DESCRIPTOR_TRIES = 3
        private const val SEND_FILE_DESCRIPTOR_SLEEP = 1000L
    }
}
