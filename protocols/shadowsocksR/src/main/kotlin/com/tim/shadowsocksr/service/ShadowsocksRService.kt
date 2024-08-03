package com.tim.shadowsocksr.service

import android.content.Context
import android.content.Intent
import android.net.IpPrefix
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.lifecycle.lifecycleScope
import com.tim.basevpn.R
import com.tim.basevpn.logger.Logger
import com.tim.basevpn.singleProcess.ProtocolsVpnService
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.currentProcess
import com.tim.shadowsocksr.Native
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.shadowsocksr.config.ConfigWriter
import com.tim.shadowsocksr.parser.parseConfiguration
import com.tim.shadowsocksr.thread.GuardedProcess
import com.tim.shadowsocksr.thread.ShadowsocksRThread
import com.tim.shadowsocksr.thread.TrafficMonitorThread
import com.tim.shadowsocksr.utils.TrafficMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet6Address
import kotlin.system.exitProcess

class ShadowsocksRService : ProtocolsVpnService() {

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
    private var logger: Logger? = null

    private var configWriter: ConfigWriter? = null
    private var config: ShadowsocksRVpnConfig? = null

    private var shadowsocksRThread: ShadowsocksRThread? = null
    private var sslocalProcess: GuardedProcess? = null
    private var sstunnelProcess: GuardedProcess? = null
    private var pdnsdProcess: GuardedProcess? = null
    private var tun2socksProcess: GuardedProcess? = null

    private var trafficMonitorThread: TrafficMonitorThread? = null
    private var connection: ParcelFileDescriptor? = null

    private var stateCached: ConnectionState = ConnectionState.READYFORCONNECT

    override fun initDependencies(intent: Intent) {
        super.initDependencies(intent)
        logger = Logger(this::class.simpleName.orEmpty())
        logger?.d("initDependencies()")
    }

    override fun prepare(intent: Intent) {
        super.prepare(intent)
        config = intent.parseConfiguration()
    }

    override fun start() {
        logger?.d("start()")
        lifecycleScope.launch { showNotification() }

        configWriter = ConfigWriter(requireNotNull(config))
        TrafficMonitor.reset()
        trafficMonitorThread = TrafficMonitorThread(
            statPath = statPath,
            update = { send, received ->
                //stateCallback.sendCallback { it.trafficUpdate(send, received) }
            }
        ).also { monitor ->
            monitor.startThread()
        }
        killProcesses()
        shadowsocksRThread = ShadowsocksRThread(
            protectPath = protectPath,
            protectFileDescriptor = { socket ->
                protect(socket)
            }
        ).also {
            it.start()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val fd = establish()
            if (!sendFileDescriptor(fd)) {
                logger?.d("sendFd failed")
                stop()
                return@launch
            }
            runTunnelProcesses()
            updateState(ConnectionState.CONNECTED)
        }
    }

    override fun stop() {
        lifecycleScope.launch { stopNotification() }
        shadowsocksRThread?.stopThread()
        shadowsocksRThread = null

        updateState(ConnectionState.DISCONNECTING)

        killProcesses()

        connection?.close()
        connection = null

        TrafficMonitor.reset()
        trafficMonitorThread?.stopThread()
        trafficMonitorThread = null

        updateState(ConnectionState.DISCONNECTED)

        if (currentProcess().contains("ssr", true)) { // from manifest
            exitProcess(0)
        } else {
            runCatching { stopSelf() }
        }
    }

    override fun clearDependencies() {
        super.clearDependencies()
        logger?.d("clearDependencies()")
        logger = null
    }

    private fun establish(): Int {
        connection = Builder().apply {
            setSession(requireNotNull(config).name.orEmpty())
            setMtu(CONNECTION_MTU)
            addAddress(ADDRESS_ROUTE, ADDRESS_PREFIX_LENGTH)
            addDnsServer(requireNotNull(config).dnsAddress.orEmpty())

            resources.getStringArray(R.array.bypass_private_route)
                .associate { route ->
                    val split = route.split("/")
                    Pair(split[0], Integer.parseInt(split[1]))
                }.onEach { map ->
                    addRoute(map.key, map.value)
                }

            addRoute(requireNotNull(config).dnsAddress.orEmpty(), DNS_PREFIX_LENGTH)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                runCatching {
                    val anyIpv6 = Inet6Address.getByAddress(
                        "::",
                        byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                        0
                    )
                    excludeRoute(IpPrefix(anyIpv6, 0))
                }.onFailure {
                    logger?.d("ipv6 setup error: $it")
                }
            }

            allowedApplications?.forEach {
                addAllowedApplication(it)
            }
        }.establish() ?: run {
            logger?.d("No connection")
            return -1
        }

        val descriptor = connection!!.fd
        val command = configWriter?.buildTun2SocksCmd(
            fd = descriptor.toString(),
            dataDir = dataDir,
            nativeDir = nativeDir
        ) ?: emptyList()
        tun2socksProcess = GuardedProcess(command).apply {
            start {
                if (stateCached != ConnectionState.DISCONNECTED) {
                    // TODO avoid global scope
                    lifecycleScope.launch(Dispatchers.IO) {
                        sendFileDescriptor(descriptor)
                    }
                }
            }
        }
        return descriptor
    }

    private fun runTunnelProcesses() {
        configWriter?.apply {
            printConfigsToFiles(dataDir, protectPath)
            sslocalProcess = GuardedProcess(
                buildShadowSocksDaemonCmd(dataDir, nativeDir)
            ).apply { start() }
            pdnsdProcess = GuardedProcess(
                buildDnsDaemonCmd(dataDir, nativeDir)
            ).apply { start() }
            sstunnelProcess = GuardedProcess(
                buildDnsTunnelCmd(dataDir, nativeDir)
            ).apply { start() }
        }
    }

    private suspend fun sendFileDescriptor(fd: Int): Boolean = withContext(Dispatchers.IO) {
        var success = false
        repeat(5) {
            if (!success) {
                delay(1000)
                success = Native.sendfd(fd, "$dataDir/sock_path") != -1
            }
        }
        success
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

    companion object {
        const val CONFIGURATION_KEY = "CONFIGURATION_KEY"

        private const val CONNECTION_MTU = 1500
        private const val ADDRESS_ROUTE = "172.19.0.1"
        private const val ADDRESS_PREFIX_LENGTH = 24
        private const val DNS_PREFIX_LENGTH = 32

        fun startService(
            context: Context,
            config: ShadowsocksRVpnConfig,
            notificationClass: String? = null,
            allowedApplications: Array<String> = emptyArray()
        ) {
            val intent = Intent(context, ShadowsocksRService::class.java).apply {
                setPackage(context.applicationContext.packageName)
                putExtra(ACTION_KEY, ACTION_START_KEY)
                putExtra(CONFIGURATION_KEY, config)
                putExtra(NOTIFICATION_CLASS_KEY, notificationClass)
                putExtra(ALLOWED_APPS_KEY, allowedApplications)
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ShadowsocksRService::class.java).apply {
                setPackage(context.applicationContext.packageName)
                putExtra(ACTION_KEY, ACTION_STOP_KEY)
            }
            context.startService(intent)
        }
    }
}