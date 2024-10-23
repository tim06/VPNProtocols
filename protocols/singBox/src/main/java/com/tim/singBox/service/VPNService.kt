package com.tim.singBox.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.Process
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.tim.basevpn.singleProcess.ProtocolsVpnService
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.currentProcess
import com.tim.libbox.BoxService
import com.tim.libbox.CommandServer
import com.tim.libbox.CommandServerHandler
import com.tim.libbox.InterfaceUpdateListener
import com.tim.libbox.Libbox
import com.tim.libbox.SystemProxyStatus
import com.tim.libbox.TunOptions
import com.tim.libbox.WIFIState
import com.tim.singBox.bg.DefaultNetworkListener
import com.tim.singBox.bg.DefaultNetworkMonitor
import com.tim.singBox.bg.LocalResolver
import com.tim.singBox.bg.PlatformInterfaceWrapper
import com.tim.singBox.ktx.hasPermission
import com.tim.singBox.ktx.toIpPrefix
import com.tim.singBox.parser.parseConfiguration
import go.Seq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress
import kotlin.system.exitProcess

class VPNService : ProtocolsVpnService(), PlatformInterfaceWrapper, CommandServerHandler {

    private var wifiManager: WifiManager? = null
    private var connectivityManager: ConnectivityManager? = null

    private var defaultNetworkMonitor: DefaultNetworkMonitor? = null
    private var defaultNetworkListener: DefaultNetworkListener? = null
    private var localResolver: LocalResolver? = null

    private var boxService: BoxService? = null
    private var commandServer: CommandServer? = null

    private var configuration: String? = null

    private var fileDescriptor: ParcelFileDescriptor? = null

    override fun initDependencies(intent: Intent) {
        super.initDependencies(intent)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        defaultNetworkListener = DefaultNetworkListener(
            connectivityManager = requireNotNull(connectivityManager)
        )
        defaultNetworkMonitor = DefaultNetworkMonitor(
            connectivityManager = requireNotNull(connectivityManager),
            defaultNetworkListener = requireNotNull(defaultNetworkListener)
        )
        localResolver = LocalResolver(defaultNetworkMonitor = requireNotNull(defaultNetworkMonitor))
    }

    override fun prepare(intent: Intent) {
        super.prepare(intent)
        configuration = intent.parseConfiguration()
    }

    override fun start() {
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                initialize()
                try {
                    startCommandServer()
                } catch (e: Exception) {
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    showNotification()
                }

                defaultNetworkMonitor?.start()
                Libbox.registerLocalDNSTransport(requireNotNull(localResolver))
                Libbox.setMemoryLimit(false)

                val newService = try {
                    Libbox.newService(configuration, this@VPNService)
                } catch (e: Exception) {
                    return@launch
                }

                newService.start()

                if (newService.needWIFIState()) {
                    val wifiPermission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    } else {
                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    }
                    if (!hasPermission(wifiPermission)) {
                        newService.close()
                        return@launch
                    }
                }

                boxService = newService
                commandServer?.setService(boxService)
                updateState(ConnectionState.CONNECTED)
            }
        } catch (e: Exception) {
            return
        }
    }

    override fun stop() {
        stopNotification()
        lifecycleScope.launch(Dispatchers.IO) {
            updateState(ConnectionState.DISCONNECTING)
            val pfd = fileDescriptor
            if (pfd != null) {
                pfd.close()
                fileDescriptor = null
            }
            boxService?.apply {
                runCatching {
                    close()
                }.onFailure {
                    writeLog("service: error when closing: $it")
                }
                Seq.destroyRef(refnum)
            }
            commandServer?.setService(null)
            boxService = null
            Libbox.registerLocalDNSTransport(null)
            defaultNetworkMonitor?.stop()

            commandServer?.apply {
                close()
                Seq.destroyRef(refnum)
            }
            commandServer = null
            updateState(ConnectionState.DISCONNECTED)
            withContext(Dispatchers.Main) {
                runCatching { stopSelf() }
            }
        }
    }

    override fun autoDetectInterfaceControl(fd: Int) {
        protect(fd)
    }

    override fun openTun(options: TunOptions): Int {
        if (prepare(this) != null) error("android: missing vpn permission")

        val builder = Builder()
            .setSession("sing-box")
            .setMtu(options.mtu)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        val inet4Address = options.inet4Address
        while (inet4Address.hasNext()) {
            val address = inet4Address.next()
            builder.addAddress(address.address(), address.prefix())
        }

        val inet6Address = options.inet6Address
        while (inet6Address.hasNext()) {
            val address = inet6Address.next()
            builder.addAddress(address.address(), address.prefix())
        }

        if (options.autoRoute) {
            builder.addDnsServer(options.dnsServerAddress)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val inet4RouteAddress = options.inet4RouteAddress
                if (inet4RouteAddress.hasNext()) {
                    while (inet4RouteAddress.hasNext()) {
                        builder.addRoute(inet4RouteAddress.next().toIpPrefix())
                    }
                } else if (options.inet4Address.hasNext()) {
                    builder.addRoute("0.0.0.0", 0)
                }

                val inet6RouteAddress = options.inet6RouteAddress
                if (inet6RouteAddress.hasNext()) {
                    while (inet6RouteAddress.hasNext()) {
                        builder.addRoute(inet6RouteAddress.next().toIpPrefix())
                    }
                } else if (options.inet6Address.hasNext()) {
                    builder.addRoute("::", 0)
                }

                val inet4RouteExcludeAddress = options.inet4RouteExcludeAddress
                while (inet4RouteExcludeAddress.hasNext()) {
                    builder.excludeRoute(inet4RouteExcludeAddress.next().toIpPrefix())
                }

                val inet6RouteExcludeAddress = options.inet6RouteExcludeAddress
                while (inet6RouteExcludeAddress.hasNext()) {
                    builder.excludeRoute(inet6RouteExcludeAddress.next().toIpPrefix())
                }
            } else {
                val inet4RouteAddress = options.inet4RouteRange
                if (inet4RouteAddress.hasNext()) {
                    while (inet4RouteAddress.hasNext()) {
                        val address = inet4RouteAddress.next()
                        builder.addRoute(address.address(), address.prefix())
                    }
                }

                val inet6RouteAddress = options.inet6RouteRange
                if (inet6RouteAddress.hasNext()) {
                    while (inet6RouteAddress.hasNext()) {
                        val address = inet6RouteAddress.next()
                        builder.addRoute(address.address(), address.prefix())
                    }
                }
            }

            allowedApplications?.forEach { builder.addAllowedApplication(it) }

        }

        val pfd =
            builder.establish() ?: error("android: the application is not prepared or is revoked")
        fileDescriptor = pfd
        return pfd.fd
    }

    override fun readWIFIState(): WIFIState? {
        @Suppress("DEPRECATION")
        val wifiInfo = wifiManager?.connectionInfo ?: return null
        var ssid = wifiInfo.ssid
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length - 1)
        }
        return WIFIState(ssid, wifiInfo.bssid)
    }

    override fun startDefaultInterfaceMonitor(listener: InterfaceUpdateListener) {
        defaultNetworkMonitor?.setListener(listener)
    }

    override fun closeDefaultInterfaceMonitor(listener: InterfaceUpdateListener) {
        defaultNetworkMonitor?.setListener(null)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun findConnectionOwner(
        ipProtocol: Int,
        sourceAddress: String,
        sourcePort: Int,
        destinationAddress: String,
        destinationPort: Int
    ): Int {
        val uid = connectivityManager?.getConnectionOwnerUid(
            ipProtocol,
            InetSocketAddress(sourceAddress, sourcePort),
            InetSocketAddress(destinationAddress, destinationPort)
        )
        if (uid == Process.INVALID_UID) error("android: connection owner not found")
        return uid ?: 0
    }

    override fun packageNameByUid(uid: Int): String {
        val packages = packageManager.getPackagesForUid(uid)
        if (packages.isNullOrEmpty()) error("android: package not found")
        return packages[0]
    }

    @Suppress("DEPRECATION")
    override fun uidByPackageName(packageName: String): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageUid(
                    packageName, PackageManager.PackageInfoFlags.of(0)
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                packageManager.getPackageUid(packageName, 0)
            } else {
                packageManager.getApplicationInfo(packageName, 0).uid
            }
        } catch (e: PackageManager.NameNotFoundException) {
            error("android: package not found")
        }
    }

    override fun writeLog(message: String) {
        commandServer?.writeMessage(message)
    }

    override fun getSystemProxyStatus(): SystemProxyStatus {
        val status = SystemProxyStatus()
        status.available = false
        status.enabled = false
        return status
    }

    override fun postServiceClose() {
        // Not used on Android
    }

    override fun serviceReload() {
        val pfd = fileDescriptor
        if (pfd != null) {
            pfd.close()
            fileDescriptor = null
        }
        boxService?.apply {
            runCatching {
                close()
            }.onFailure {
                writeLog("service: error when closing: $it")
            }
            Seq.destroyRef(refnum)
        }
        commandServer?.setService(null)
        commandServer?.resetLog()
        boxService = null
        runBlocking {
            start()
        }
    }

    override fun onRevoke() {
        stop()
    }

    override fun clearDependencies() {
        super.clearDependencies()
        wifiManager = null
        connectivityManager = null

        defaultNetworkMonitor = null
        defaultNetworkListener = null
        localResolver = null

        boxService = null
        commandServer = null

        configuration = null

        fileDescriptor = null
    }

    override fun setSystemProxyEnabled(isEnabled: Boolean) {
        serviceReload()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (currentProcess().contains("singBox", true)) { // from manifest
            exitProcess(0)
        }
    }

    private fun initialize() {
        val baseDir = applicationContext.filesDir
        baseDir.mkdirs()
        val workingDir = applicationContext.getExternalFilesDir(null) ?: return
        workingDir.mkdirs()
        val tempDir = applicationContext.cacheDir
        tempDir.mkdirs()
        Libbox.setup(baseDir.path, workingDir.path, tempDir.path, false)
        Libbox.redirectStderr(File(workingDir, "stderr.log").path)
        return
    }

    private fun startCommandServer() {
        val commandServer =
            CommandServer(this, 300)
        commandServer.start()
        this.commandServer = commandServer
    }

    companion object {
        const val CONFIGURATION_KEY = "CONFIGURATION_KEY"

        fun startService(
            context: Context,
            config: String,
            notificationClass: String? = null,
            allowedApplications: Array<String> = emptyArray(),
        ) {
            val intent = buildIntent(
                context = context,
                config = config,
                notificationClass = notificationClass,
                allowedApplications = allowedApplications,
            ).apply {
                putExtra(ACTION_KEY, ACTION_START_KEY)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, VPNService::class.java).apply {
                setPackage(context.applicationContext.packageName)
                putExtra(ACTION_KEY, ACTION_STOP_KEY)
            }
            context.startService(intent)
        }

        fun buildIntent(
            context: Context,
            config: String,
            notificationClass: String? = null,
            allowedApplications: Array<String> = emptyArray()
        ): Intent {
            return Intent(context, VPNService::class.java).apply {
                setPackage(context.applicationContext.packageName)
                putExtra(CONFIGURATION_KEY, config)
                putExtra(NOTIFICATION_CLASS_KEY, notificationClass)
                putExtra(ALLOWED_APPS_KEY, allowedApplications)
            }
        }
    }
}