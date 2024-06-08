package com.tim.vpnprotocols.xrayNg

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.configuration.VpnConfiguration
import com.tim.basevpn.delegate.StateDelegate
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.sendCallback
import com.tim.notification.DefaultVpnServiceNotification
import com.tim.notification.VpnServiceNotification
import com.tim.vpn.xrayNg.R
import com.tim.vpnprotocols.xrayNg.file.FilesDir
import com.tim.vpnprotocols.xrayNg.file.SockFile
import com.tim.vpnprotocols.xrayNg.file.Tun2SocksFile
import com.tim.vpnprotocols.xrayNg.helper.FdSendHelper
import com.tim.vpnprotocols.xrayNg.helper.Tun2SocksHelper
import com.tim.vpnprotocols.xrayNg.log.Logger
import com.tim.vpnprotocols.xrayNg.parser.Action
import com.tim.vpnprotocols.xrayNg.parser.actionFromIntent
import com.tim.vpnprotocols.xrayNg.parser.parseAllowedApplications
import com.tim.vpnprotocols.xrayNg.parser.parseConfiguration
import com.tim.vpnprotocols.xrayNg.parser.parseDomainName
import com.tim.vpnprotocols.xrayNg.parser.parseNotificationClass
import go.Seq
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import libv2ray.Libv2ray
import libv2ray.V2RayPoint
import java.io.File
import java.util.Timer
import java.util.TimerTask
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 *
 * To start a VPN you need to send an intent that will contain the Action.START command
 * and the necessary parameters to run it, such as: [configuration, domain, notificationClass].
 * To stop a VPN intent with Action.STOP.
 *
 * Start cycle: prepareVpn() -> startV2Ray() --(async)-> startVpn() -> startTun2socks()
 */
class XRayNgService : VpnService() {

    private lateinit var job: Job
    private lateinit var scope: CoroutineScope

    private lateinit var v2rayPoint: V2RayPoint
    private lateinit var fdSendHelper: FdSendHelper
    private lateinit var tun2SocksHelper: Tun2SocksHelper

    private val logger by lazy {
        Logger("XRayNgService")
    }

    private val stateCallback by StateDelegate()
    private val binder by lazy {
        object : IVPNService.Stub() {

            override fun startVPN(configuration: VpnConfiguration<*>) {
                // nothing
            }

            override fun stopVPN() {
                // nothing
            }

            override fun getState(): ConnectionState = currentConnectionState

            override fun registerCallback(cb: IConnectionStateListener?) {
                stateCallback.register(cb)
            }

            override fun unregisterCallback(cb: IConnectionStateListener?) {
                stateCallback.unregister(cb)
            }
        }
    }

    private var allowedApplications: Array<String>? = null
    private var notificationHelper: VpnServiceNotification? = null
    private var vpnInterface: ParcelFileDescriptor? = null
    private var currentConnectionState: ConnectionState = ConnectionState.READYFORCONNECT

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requireNotNull(intent)
        return when (intent.actionFromIntent()) {
            Action.START -> {
                initDependencies()
                updateState(ConnectionState.CONNECTING)
                logger.d("onStartCommand() Action.START")
                val configuration = intent.parseConfiguration()
                val domain = intent.parseDomainName()
                val notificationClass = intent.parseNotificationClass()
                allowedApplications = intent.parseAllowedApplications()
                initNotification(notificationClass)
                prepareVpn(configuration, domain)
                startV2Ray()
                START_REDELIVER_INTENT
            }

            Action.STOP -> {
                logger.d("onStartCommand() Action.STOP")
                stopVpn()
                START_NOT_STICKY
            }

            else -> START_STICKY
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onRevoke() {
        logger.d("onRevoke()")
        stopVpn()
        super.onRevoke()
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.d("onDestroy()")
        stopVpn()
    }

    private fun prepareVpn(configuration: String?, domainName: String?) {
        v2rayPoint.configureFileContent = configuration
        v2rayPoint.domainName = domainName

        Seq.setContext(applicationContext)
        Libv2ray.initV2Env(userAssetPath(), getDeviceIdForXUDPBaseKey())
    }

    @SuppressLint("InlinedApi")
    private fun startVpn() {
        val builder = Builder()

        builder.setMtu(MTU)
        // ipv4
        builder.addAddress(PRIVATE_VLAN4_CLIENT, 24)
        resources.getStringArray(R.array.bypass_private_ip_address).forEach {
            val addr = it.split('/')
            builder.addRoute(addr[0], addr[1].toInt())
        }
        // ipv6
        builder.addAddress(PRIVATE_VLAN6_CLIENT, 126)
        builder.addRoute("2000::", 3)
        // name
        builder.setSession(applicationContext.applicationInfo.name)
        // dns
        builder.addDnsServer("1.1.1.1")
        builder.addDnsServer("8.8.8.8")
        builder.addDnsServer("8.8.4.4")
        builder.addDnsServer("77.88.8.8")
        builder.addDnsServer("2a02:6b8::feed:0ff")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        allowedApplications?.forEach { builder.addAllowedApplication(it) }

        runCatching {
            vpnInterface = builder.establish()?.also { interf ->
                scope.launch {
                    tun2SocksHelper.startTun2socks(interf.fileDescriptor)
                }
            }
            updateState(ConnectionState.CONNECTED)
        }.onFailure {
            logger.d("startVpn() builder.establish() error —> $it")
            stopVpn()
        }
    }

    private fun stopVpn() {
        updateState(ConnectionState.DISCONNECTING)
        stopV2Ray()
        notificationHelper?.stop()

        tun2SocksHelper.stop()
        job.cancel()
        runCatching { vpnInterface?.close() }
        vpnInterface = null

        updateState(ConnectionState.DISCONNECTED)
        stopSelf()
    }

    private fun startV2Ray() {
        scope.launch(Dispatchers.IO) {
            runCatching {
                v2rayPoint.runLoop(false)
            }.onFailure {
                logger.d("startV2Ray() v2rayPoint.runLoop() error —> $it")
            }
        }
        notificationHelper?.start()
    }

    private fun stopV2Ray() {
        v2rayPoint.stopLoop()
    }

    private fun initNotification(vpnNotificationClass: String?): VpnServiceNotification {
        val notificationManager = applicationContext.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        val helper = runCatching {
            val cl = Class.forName(vpnNotificationClass)
            val clazz = cl.getConstructor(
                Service::class.java,
                NotificationManager::class.java
            ).newInstance(this, notificationManager) as VpnServiceNotification
            runCustomNotificationUpdater(clazz)
            clazz
        }.onFailure {
            logger.d("initNotification() error —> $it")
        }.getOrDefault(
            defaultValue = DefaultVpnServiceNotification(
                service = this,
                notificationManager = notificationManager
            )
        )
        this.notificationHelper = helper
        return helper
    }

    private fun updateState(newState: ConnectionState) {
        currentConnectionState = newState
        stateCallback.sendCallback { it.stateChanged(newState) }
    }

    private fun initDependencies() {
        job = SupervisorJob()
        scope = CoroutineScope(Dispatchers.IO + job)
        v2rayPoint = Libv2ray.newV2RayPoint(
            XRayNgCallback(
                onEmitStatus = { p1, p2 ->
                    0
                },
                protect = {
                    protect(it.toInt())
                },
                shutdown = {
                    logger.d("Shutdown from go lib!")
                    stopVpn()
                    0
                },
                setup = {
                    startVpn()
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
            coroutineScope = scope
        )
        tun2SocksHelper = Tun2SocksHelper(
            filesDir = FilesDir(applicationContext.filesDir),
            tun2SocksFile = Tun2SocksFile(
                File(
                    applicationContext.applicationInfo.nativeLibraryDir,
                    TUN2SOCKS
                )
            ),
            fdSendHelper = fdSendHelper,
            coroutineScope = scope
        )
    }

    private fun runCustomNotificationUpdater(notificationHelper: VpnServiceNotification) {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                notificationHelper.updateNotification(notificationHelper.createNotification(""))
                delay(1.minutes)
            }
        }
    }

    companion object {
        const val ACTION_KEY = "ACTION_KEY"
        const val ACTION_START_KEY = "ACTION_START_KEY"
        const val ACTION_STOP_KEY = "ACTION_STOP_KEY"

        const val CONFIGURATION_KEY = "CONFIGURATION_KEY"
        const val CONFIGURATION_DOMAIN_KEY = "CONFIGURATION_DOMAIN_KEY"
        const val NOTIFICATION_CLASS_KEY = "NOTIFICATION_CLASS_KEY"
        const val ALLOWED_APPS_KEY = "ALLOWED_APPS_KEY"

        internal const val MTU = 1500
        internal const val PRIVATE_VLAN4_ROUTER = "26.26.26.2"
        private const val PRIVATE_VLAN4_CLIENT = "26.26.26.1"
        private const val PRIVATE_VLAN6_CLIENT = "da26:2626::1"
        internal const val PRIVATE_VLAN6_ROUTER = "da26:2626::2"
        internal const val TUN2SOCKS = "libtun2socks.so"
    }
}
