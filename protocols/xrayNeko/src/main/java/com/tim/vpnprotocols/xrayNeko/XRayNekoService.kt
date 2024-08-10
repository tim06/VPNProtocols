package com.tim.vpnprotocols.xrayNeko

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.tim.basevpn.logger.Logger
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.currentProcess
import com.tim.vpnprotocols.xrayNeko.parser.parseConfiguration
import com.tim.vpnprotocols.xrayNeko.parser.parseNaiveConfiguration
import com.tim.vpnprotocols.xrayNeko.util.GuardedProcessPool
import com.tim.vpnprotocols.xrayNeko.util.Logs
import go.Seq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import libcore.BoxInstance
import libcore.BoxPlatformInterface
import libcore.Libcore
import libcore.NB4AInterface
import java.io.File
import java.net.InetSocketAddress
import kotlin.system.exitProcess

class XRayNekoService : NetworkListenerVpnService() {

    private var connectivity: ConnectivityManager? = null
    private var wifiManager: WifiManager? = null
    private var logger: Logger? = null
    private var box: BoxInstance? = null
    private var vpnInterface: ParcelFileDescriptor? = null

    // for naive
    private var cacheFiles: ArrayList<File>? = null
    private var processes: GuardedProcessPool? = null

    private val nB4AInterface: NB4AInterface = object : NB4AInterface {
        override fun selector_OnProxySelected(p0: String?, p1: String?) {
            Libcore.resetAllConnections(true)
        }

        override fun useOfficialAssets(): Boolean {
            return true
        }
    }
    private val boxPlatformInterface: BoxPlatformInterface = object : BoxPlatformInterface {
        override fun autoDetectInterfaceControl(fd: Int) {
            protect(fd)
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun findConnectionOwner(
            ipProto: Int,
            srcIp: String?,
            srcPort: Int,
            destIp: String?,
            destPort: Int
        ): Int {
            return connectivity?.getConnectionOwnerUid(
                ipProto, InetSocketAddress(srcIp, srcPort), InetSocketAddress(destIp, destPort)
            ) ?: 0
        }

        override fun openTun(p0: String?, p1: String?): Long {
            return establishConnection().toLong()
        }

        override fun packageNameByUid(uid: Int): String {
            return "android"
        }

        override fun uidByPackageName(p0: String?): Int {
            return 0
        }

        override fun useProcFS(): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        }

        override fun wifiState(): String {
            val connectionInfo = wifiManager?.connectionInfo
            return "${connectionInfo?.ssid},${connectionInfo?.bssid}"
        }
    }

    override fun initDependencies(intent: Intent) {
        super.initDependencies(intent)
        Seq.setContext(this)
        logger = Logger(this::class.simpleName.orEmpty())
        logger?.d("initDependencies()")
        connectivity = getSystemService<ConnectivityManager>()
        wifiManager = getSystemService<WifiManager>()
        cacheFiles = ArrayList()
        processes = GuardedProcessPool(application.noBackupFilesDir) {
            Logs.w(it)
            stop()
        }

        val process = applicationContext.currentProcess()
        val externalAssets: File = getExternalFilesDir(null) ?: filesDir
        val logBufSize = 50 // kb
        val logLevel = 0 // 0 -> "panic"; 1 -> "warn"; 2 -> "info"; 3 -> "debug"; 4 -> "trace"

        lifecycleScope.launch(Dispatchers.Default) {
            Libcore.initCore(
                process,
                cacheDir.absolutePath + "/",
                filesDir.absolutePath + "/",
                externalAssets.absolutePath + "/",
                logBufSize,
                false,
                nB4AInterface,
                boxPlatformInterface
            )
        }

        lifecycleScope.launch(Dispatchers.Default) {
            registerDns()
            val configuration = intent.parseConfiguration()
            box = Libcore.newSingBoxInstance(configuration)
            box?.setAsMain()
        }
    }

    override fun clearDependencies() {
        super.clearDependencies()
        logger?.d("clearDependencies()")
        logger = null
    }

    override fun prepare(intent: Intent) {
        super.prepare(intent)
        val naiveConfig = intent.parseNaiveConfiguration()
        naiveConfig?.let { initNaive(it) }
    }

    override fun start() {
        lifecycleScope.launch(Dispatchers.Main) {
            showNotification()
            box?.start()
        }
    }

    override fun stop() {
        processes?.close(GlobalScope + Dispatchers.IO)
        cacheFiles?.all { it.delete() }
        cacheFiles?.clear()
        cacheFiles = null
        Libcore.registerLocalDNSTransport(null)
        box?.close()
        runCatching { vpnInterface?.close() }
        stopNotification()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        logger?.d("onDestroy()")
        if (currentProcess().contains("xrayNeko", true)) { // from manifest
            exitProcess(0)
        }
    }

    private fun establishConnection(): Int {
        val builder = Builder()
        builder.setSession("New VPN")
        builder.setMtu(MTU)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        builder.addAddress(PRIVATE_VLAN4_CLIENT, 30)
        builder.addDnsServer(PRIVATE_VLAN4_ROUTER)
        builder.addRoute("0.0.0.0", 0)
        builder.addRoute("::", 0)

        allowedApplications?.forEach { builder.addAllowedApplication(it) }

        runCatching {
            vpnInterface = builder.establish()
            updateState(ConnectionState.CONNECTED)
        }.onFailure {
            logger?.d("startVpn() builder.establish() error —> $it")
            stop()
        }
        return vpnInterface!!.fd
    }

    private fun initNaive(config: String) {
        val configFile = File(
            cacheDir, "naive_" + SystemClock.elapsedRealtime() + ".json"
        )

        configFile.parentFile?.mkdirs()
        configFile.writeText(config)
        cacheFiles?.add(configFile)

        val envMap = mutableMapOf<String, String>()

        /*if (bean.certificates.isNotBlank()) {
            val certFile = File(
                cacheDir, "naive_" + SystemClock.elapsedRealtime() + ".crt"
            )

            certFile.parentFile?.mkdirs()
            certFile.writeText(bean.certificates)
            cacheFiles.add(certFile)

            envMap["SSL_CERT_FILE"] = certFile.absolutePath
        }*/

        val naiveExecutable = File(applicationInfo.nativeLibraryDir + "/libnaive.so").absolutePath
        val commands = mutableListOf(
            naiveExecutable, configFile.absolutePath
        )

        processes?.start(commands, envMap)
    }

    companion object {
        const val CONFIGURATION_KEY = "CONFIGURATION_KEY"
        const val NAIVE_CONFIGURATION_KEY = "NAIVE_CONFIGURATION_KEY"

        internal const val MTU = 9000
        const val PRIVATE_VLAN4_CLIENT = "172.19.0.1"
        const val PRIVATE_VLAN4_ROUTER = "172.19.0.2"
        const val FAKEDNS_VLAN4_CLIENT = "198.18.0.0"
        const val PRIVATE_VLAN6_CLIENT = "fdfe:dcba:9876::1"

        fun startService(
            context: Context,
            config: String,
            naiveConfig: String?,
            notificationClass: String? = null,
            allowedApplications: Array<String> = emptyArray()
        ) {
            val intent = buildIntent(
                context = context,
                config = config,
                naiveConfig = naiveConfig,
                notificationClass = notificationClass,
                allowedApplications = allowedApplications
            ).apply {
                putExtra(ACTION_KEY, ACTION_START_KEY)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, XRayNekoService::class.java).apply {
                setPackage(context.applicationContext.packageName)
                putExtra(ACTION_KEY, ACTION_STOP_KEY)
            }
            context.startService(intent)
        }

        fun buildIntent(
            context: Context,
            config: String,
            naiveConfig: String?,
            notificationClass: String? = null,
            allowedApplications: Array<String> = emptyArray()
        ): Intent {
            return Intent(context, XRayNekoService::class.java).apply {
                setPackage(context.applicationContext.packageName)
                putExtra(CONFIGURATION_KEY, config)
                putExtra(NAIVE_CONFIGURATION_KEY, naiveConfig)
                putExtra(NOTIFICATION_CLASS_KEY, notificationClass)
                putExtra(ALLOWED_APPS_KEY, allowedApplications)
            }
        }
    }
}