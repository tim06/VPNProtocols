package com.tim.vpnprotocols.xrayNg

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.tim.basevpn.logger.Logger
import com.tim.basevpn.singleProcess.ProtocolsVpnService
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.currentProcess
import com.tim.notification.DefaultVpnServiceNotification
import com.tim.vpnprotocols.xrayNg.file.SockFile
import com.tim.vpnprotocols.xrayNg.file.Tun2SocksFile
import com.tim.vpnprotocols.xrayNg.helper.FdSendHelper
import com.tim.vpnprotocols.xrayNg.helper.Tun2SocksHelper
import com.tim.vpnprotocols.xrayNg.parser.parseConfiguration
import com.tim.vpnprotocols.xrayNg.parser.parseDomainName
import go.Seq
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import libv2ray.Libv2ray
import libv2ray.V2RayPoint
import java.io.File
import kotlin.system.exitProcess

/**
 *
 * To start a VPN you need to send an intent that will contain the Action.START command
 * and the necessary parameters to run it, such as: [configuration, domain, notificationClass].
 * To stop a VPN intent with Action.STOP.
 *
 * Start cycle: prepareVpn() -> startV2Ray() --(async)-> startVpn() -> startTun2socks()
 */
class XRayNgService : ProtocolsVpnService() {

    private var v2rayPoint: V2RayPoint? = null
    private var fdSendHelper: FdSendHelper? = null
    private var tun2SocksHelper: Tun2SocksHelper? = null
    private var vpnInterface: ParcelFileDescriptor? = null

    private var fixedThread: ExecutorCoroutineDispatcher? = null
    private var singleThread: ExecutorCoroutineDispatcher? = null
    private var trafficScope: CoroutineScope? = null
    private var v2rayScope: CoroutineScope? = null
    private var logger: Logger? = null

    override fun initDependencies(intent: Intent) {
        super.initDependencies(intent)
        Seq.setContext(applicationContext)
        logger = Logger(this::class.simpleName.orEmpty())
        logger?.d("initDependencies()")

        fixedThread = newFixedThreadPoolContext(16, "v2ray traffic")
        singleThread = newSingleThreadContext("v2ray")
        trafficScope = lifecycleScope + requireNotNull(fixedThread)
        v2rayScope = lifecycleScope + requireNotNull(singleThread)
        v2rayScope?.launch {
            v2rayPoint = Libv2ray.newV2RayPoint(
                XRayNgCallback(
                    onEmitStatus = { p1, p2 ->
                        0
                    },
                    protect = {
                        protect(it.toInt())
                    },
                    shutdown = {
                        logger?.d("Shutdown from go lib!")
                        stop()
                        0
                    },
                    setup = {
                        logger?.d("Setup from go lib!")
                        establish()
                        0
                    }
                ),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
            )
            fdSendHelper = FdSendHelper(
                sockFile = SockFile(
                    File(
                        applicationContext.filesDir,
                        "sock_path"
                    )
                ),
                coroutineScope = requireNotNull(trafficScope)
            ).apply { initDependencies() }
            tun2SocksHelper = Tun2SocksHelper(
                filesDir = applicationContext.filesDir,
                tun2SocksFile = Tun2SocksFile(
                    File(
                        applicationContext.applicationInfo.nativeLibraryDir,
                        TUN2SOCKS
                    )
                ),
                fdSendHelper = fdSendHelper,
                coroutineScope = requireNotNull(trafficScope)
            ).apply { initDependencies() }
        }
    }

    override fun clearDependencies() {
        super.clearDependencies()
        logger?.d("clearDependencies()")
        logger = null
        v2rayPoint = null
        fdSendHelper?.clearDependencies()
        fdSendHelper = null
        tun2SocksHelper?.clearDependencies()
        tun2SocksHelper = null
        vpnInterface = null

        fixedThread = null
        singleThread = null
        v2rayScope = null
        trafficScope = null
    }

    override fun prepare(intent: Intent) {
        super.prepare(intent)
        v2rayScope?.launch {
            val configuration = intent.parseConfiguration()
            val domain = intent.parseDomainName()
            v2rayPoint?.configureFileContent = configuration
            v2rayPoint?.domainName = domain

            Libv2ray.initV2Env(userAssetPath(), getDeviceIdForXUDPBaseKey())
        }
    }

    override fun start() {
        v2rayScope?.launch {
            runCatching {
                v2rayPoint?.runLoop(false)
            }.onFailure {
                logger?.d("startV2Ray() v2rayPoint.runLoop() error —> $it")
            }
            withContext(Dispatchers.Main) {
                showNotification()
            }
        }
    }

    override fun stop() {
        updateState(ConnectionState.DISCONNECTING)
        runCatching { v2rayPoint?.stopLoop() }

        stopNotification()
        runCatching { tun2SocksHelper?.stop() }
        runCatching {
            fixedThread?.cancel()
            fixedThread?.close()
            singleThread?.cancel()
            singleThread?.close()
            v2rayScope?.cancel()
            trafficScope?.cancel()
        }

        runCatching { vpnInterface?.close() }

        updateState(ConnectionState.DISCONNECTED)

        runCatching { stopSelf() }
    }

    override fun unbindService(conn: ServiceConnection) {
        stop()
        super.unbindService(conn)
    }

    override fun onDestroy() {
        super.onDestroy()
        logger?.d("onDestroy()")
        if (currentProcess().contains("xrayNg", true)) { // from manifest
            exitProcess(0)
        }
    }

    @SuppressLint("InlinedApi")
    private fun establish() {
        val builder = Builder()

        builder.setMtu(MTU)
        // ipv4
        builder.addAddress(PRIVATE_VLAN4_CLIENT, 24)
        /*resources.getStringArray(R.array.bypass_private_ip_address).forEach {
            val addr = it.split('/')
            builder.addRoute(addr[0], addr[1].toInt())
        }*/
        builder.addRoute("0.0.0.0", 0)
        // ipv6
        //builder.addAddress(PRIVATE_VLAN6_CLIENT, 126)
        //builder.addRoute("2000::", 3)
        builder.addRoute("::", 0)
        // name
        builder.setSession(applicationContext.applicationInfo.name)
        // dns
        builder.addDnsServer("1.1.1.1")
        builder.addDnsServer("8.8.8.8")
        builder.addDnsServer("8.8.4.4")
        builder.addDnsServer("77.88.8.8")
        builder.addDnsServer("2a02:6b8::feed:0ff")

        allowedApplications?.forEach { builder.addAllowedApplication(it) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        runCatching {
            vpnInterface = builder.establish()?.also { interf ->
                v2rayScope?.launch {
                    tun2SocksHelper?.startTun2socks(interf.fileDescriptor)
                }
            }
            updateState(ConnectionState.CONNECTED)
        }.onFailure {
            logger?.d("startVpn() builder.establish() error —> $it")
            stop()
        }
    }

    companion object {
        const val CONFIGURATION_KEY = "CONFIGURATION_KEY"
        const val CONFIGURATION_DOMAIN_KEY = "CONFIGURATION_DOMAIN_KEY"
        const val PING_KEY = "PING_KEY"

        internal const val MTU = 1500
        internal const val PRIVATE_VLAN4_ROUTER = "26.26.26.2"
        private const val PRIVATE_VLAN4_CLIENT = "26.26.26.1"
        private const val PRIVATE_VLAN6_CLIENT = "da26:2626::1"
        internal const val PRIVATE_VLAN6_ROUTER = "da26:2626::2"
        internal const val TUN2SOCKS = "libtun2socks_xrayng.so"

        fun startService(
            context: Context,
            config: String,
            domain: String,
            notificationClass: String? = null,
            allowedApplications: Array<String> = emptyArray()
        ) {
            val intent = buildIntent(
                context = context,
                config = config,
                domain = domain,
                notificationClass = notificationClass,
                allowedApplications = allowedApplications
            ).apply {
                putExtra(ACTION_KEY, ACTION_START_KEY)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, XRayNgService::class.java).apply {
                setPackage(context.applicationContext.packageName)
                putExtra(ACTION_KEY, ACTION_STOP_KEY)
            }
            context.startService(intent)
        }

        fun buildIntent(
            context: Context,
            config: String,
            domain: String,
            notificationClass: String? = null,
            allowedApplications: Array<String> = emptyArray()
        ): Intent {
            return Intent(context, XRayNgService::class.java).apply {
                setPackage(context.applicationContext.packageName)
                putExtra(CONFIGURATION_KEY, config)
                putExtra(CONFIGURATION_DOMAIN_KEY, domain)
                putExtra(NOTIFICATION_CLASS_KEY, notificationClass)
                putExtra(ALLOWED_APPS_KEY, allowedApplications)
            }
        }

        fun isActive(
            context: Context,
            notificationId: Int = DefaultVpnServiceNotification.NOTIFICATION_ID,
            notificationChannel: String = DefaultVpnServiceNotification.CHANNEL_ID
        ): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getSystemService<NotificationManager>()
                    ?.activeNotifications
                    ?.any { it.id == notificationId }
                    ?: false
            } else {
                context.getSystemService<NotificationManagerCompat>()
                    ?.getNotificationChannel(notificationChannel)
                    .let { it != null }
            }
        }

        fun measurePing(context: Context, url: String) {
            val intent = Intent(context, XRayNgService::class.java).apply {
                setPackage(context.applicationContext.packageName)
                putExtra(ACTION_KEY, ACTION_MEASURE_KEY)
                putExtra(PING_URL_KEY, url)
            }
            context.startService(intent)
        }
    }
}
