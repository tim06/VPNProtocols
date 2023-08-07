package com.tim.openvpn.service

import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.*
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.configuration.VpnConfiguration
import com.tim.basevpn.delegate.StateDelegate
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.sendCallback
import com.tim.notification.DefaultVpnServiceNotification
import com.tim.openvpn.OpenVPNThreadv3
import com.tim.openvpn.OpenVPNThreadv3.VPNSERVICE_TUN
import com.tim.openvpn.VpnStatus
import com.tim.openvpn.configuration.OpenVPNConfig
import com.tim.openvpn.log.OpenVPNLogger
import com.tim.openvpn.model.CIDRIP
import com.tim.openvpn.utils.NetworkSpace
import com.tim.openvpn.utils.NetworkSpace.IpAddress
import com.tim.openvpn.utils.NetworkUtils
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException

class OpenVPNService : VpnService(), Handler.Callback, IOpenVPNService {

    // Notification
    private val notificationHelper by lazy {
        DefaultVpnServiceNotification(
            service = this,
            notificationManager = applicationContext.getSystemService(
                NOTIFICATION_SERVICE
            ) as NotificationManager
        )
    }

    private var management: OpenVPNThreadv3? = null
    private val stateCallback by StateDelegate()
    private var stateCached: ConnectionState = ConnectionState.DISCONNECTED


    private lateinit var config: OpenVPNConfig

    private val binder = object : IVPNService.Stub() {

        override fun startVPN(configuration: VpnConfiguration<*>) {
            config = configuration.data as OpenVPNConfig
            Thread {
                startOpenVPN()
            }.start()
        }

        override fun stopVPN() {
            stopOpenVPN()
        }

        override fun getState(): ConnectionState = stateCached

        override fun registerCallback(cb: IConnectionStateListener?) {
            stateCallback.register(cb)
        }

        override fun unregisterCallback(cb: IConnectionStateListener?) {
            stateCallback.unregister(cb)
        }
    }

    @Suppress("DEPRECATION")
    override fun onBind(intent: Intent?): IBinder? {
        /*config = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            intent
                ?.extras
                ?.getParcelable(CONFIG_EXTRA)
                ?: return null
        } else {
            intent
                ?.extras
                ?.getParcelable(CONFIG_EXTRA, OpenVPNConfig::class.java)
                ?: return null
        }*/
        return binder
    }

    override fun handleMessage(msg: Message): Boolean {
        val r = msg.callback
        return if (r != null) {
            r.run()
            true
        } else {
            false
        }
    }

    override fun onRevoke() {
        endVpnService()
        super.onRevoke()
    }

    private fun startOpenVPN() {
        VpnStatus.log("startOpenVPN")
        notificationHelper.start()

        management = OpenVPNThreadv3(
            this,
            config.configuration ?: config.buildConfig()
        )
        VpnStatus.log("OpenVpnManagementThread init")

        synchronized(mProcessLock) {
            mProcessThread = Thread(management, "OpenVPNProcessThread")
            mProcessThread?.start()
        }
        VpnStatus.log("processThread init")
    }

    private fun stopOpenVPN() {
        VpnStatus.log("stopOpenVPN")

        notificationHelper.stop()
        management?.stopVPN()
        forceStopOpenVpnProcess()
        management = null
    }

    /**
     * Establish connection with [tunOptions]
     */
    private fun startTun(): ParcelFileDescriptor? {
        val builder = Builder().apply {

            if (localIp != null) {
                addLocalNetworksToRoutes()
                try {
                    localIp?.let { addAddress(it.ip, it.len) }
                } catch (iae: IllegalArgumentException) {
                    OpenVPNLogger.e("OpenVPNService", "Error: $localIp ${iae.localizedMessage}")
                    return null
                }
            }

            if (mLocalIPv6 != null) {
                val ipv6parts = mLocalIPv6!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                try {
                    addAddress(ipv6parts[0], ipv6parts[1].toInt())
                } catch (iae: java.lang.IllegalArgumentException) {
                    OpenVPNLogger.e("OpenVPNService", "Error: $mLocalIPv6 ${iae.localizedMessage}")
                    return null
                }
            }

            dnsList.forEach {
                addDnsServer(it)
            }

            mtu?.let { setMtu(it) }

            val positiveIPv4Routes = mRoutes.getPositiveIPList()
            val positiveIPv6Routes = mRoutesv6.getPositiveIPList()

            if ("samsung" == Build.BRAND && dnsList.size >= 1) {
                // Check if the first DNS Server is in the VPN range
                try {
                    val dnsServer = IpAddress(CIDRIP(dnsList.get(0), 32), true)
                    var dnsIncluded = false
                    for (net in positiveIPv4Routes) {
                        if (net.containsNet(dnsServer)) {
                            dnsIncluded = true
                        }
                    }
                    if (!dnsIncluded) {
                        val samsungwarning = java.lang.String.format(
                            "Warning Samsung Android 5.0+ devices ignore DNS servers outside the VPN range. To enable DNS resolution a route to your DNS Server (%s) has been added.",
                            dnsList.get(0)
                        )
                        OpenVPNLogger.e("OpenVPNService", samsungwarning)
                        positiveIPv4Routes.add(dnsServer)
                    }
                } catch (e: Exception) {
                    // If it looks like IPv6 ignore error
                    if (!dnsList.get(0)
                            .contains(":")
                    ) OpenVPNLogger.e("OpenVPNService", "Error parsing DNS Server IP: ${dnsList.get(0)}")
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                installRoutesExcluded(this, mRoutes)
                installRoutesExcluded(this, mRoutesv6)
            } else {
                installRoutesPostiveOnly(this, positiveIPv4Routes, positiveIPv6Routes)
            }

            if (domain != null) {
                addSearchDomain(domain!!)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                // VPN always uses the default network
                setUnderlyingNetworks(null);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Setting this false, will cause the VPN to inherit the underlying network metered
                // value
                setMetered(false)
            }

            setSession("VPN Session")
        }

        return runCatching {
            val tun = builder.establish()
                ?: throw NullPointerException(
                    "Android establish() method returned null " +
                            "(Really broken network configuration?)"
                )
            tun
        }.getOrNull()
    }

    // TODO refactor zone start
    private fun addLocalNetworksToRoutes() {
        for (net in NetworkUtils.getLocalNetworks(connectivityManager, false)) {
            val netparts = net.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val ipAddr = netparts[0]
            val netMask = netparts[1].toInt()
            if (ipAddr == localIp?.ip) continue
            //mRoutes.addIP(CIDRIP(ipAddr, netMask), false) // todo check
        }
        //if (mProfile.mAllowLocalLAN) { // todo check
            for (net in NetworkUtils.getLocalNetworks(connectivityManager, true)) {
                addRoutev6(net, false)
            }
        //}
    }

    private fun installRoutesExcluded(builder: Builder, routes: NetworkSpace) {
        for (ipIncl in routes.getNetworks(true)) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    builder.addRoute(ipIncl.prefix)
                }
            } catch (ia: UnknownHostException) {
                OpenVPNLogger.e("OpenVPNService", "Error: $ipIncl + ${ia.localizedMessage}")
            } catch (ia: java.lang.IllegalArgumentException) {
                OpenVPNLogger.e("OpenVPNService", "Error: $ipIncl + ${ia.localizedMessage}")
            }
        }
        for (ipExcl in routes.getNetworks(false)) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    builder.excludeRoute(ipExcl.prefix)
                }
            } catch (ia: UnknownHostException) {
                OpenVPNLogger.e("OpenVPNService", "Error: $ipExcl + ${ia.localizedMessage}")
            } catch (ia: java.lang.IllegalArgumentException) {
                OpenVPNLogger.e("OpenVPNService", "Error: $ipExcl + ${ia.localizedMessage}")
            }
        }
    }

    private fun installRoutesPostiveOnly(
        builder: Builder,
        positiveIPv4Routes: Collection<IpAddress>,
        positiveIPv6Routes: Collection<IpAddress>
    ) {
        val multicastRange = IpAddress(CIDRIP("224.0.0.0", 3), true)
        for (route in positiveIPv4Routes) {
            try {
                if (multicastRange.containsNet(route)) {
                    OpenVPNLogger.d(
                        "OpenVPNService",
                        "installRoutesPostiveOnly(): $route"
                    )
                } else {
                    builder.addRoute(route.getIPv4Address(), route.networkMask)
                }
            } catch (ia: java.lang.IllegalArgumentException) {
                OpenVPNLogger.e("OpenVPNService","Error: $route ${ia.localizedMessage}")
            }
        }
        for (route6 in positiveIPv6Routes) {
            try {
                builder.addRoute(route6.getIPv6Address(), route6.networkMask)
            } catch (ia: java.lang.IllegalArgumentException) {
                OpenVPNLogger.e("OpenVPNService","Error: $route6 ${ia.localizedMessage}")
            }
        }
    }

    // TODO refactor zone end

    private fun endVpnService() {
        stopOpenVPN()
        //notificationHelper.stopNotification()
    }

    private fun forceStopOpenVpnProcess() {
        synchronized(mProcessLock) {
            if (mProcessThread != null) {
                mProcessThread?.interrupt()
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    //ignore
                }
            }
        }
    }


    private val mProcessLock = Any()
    private var mProcessThread: Thread? = null

    private var mtu: Int? = null
    private var domain: String? = null
    private var localIp: CIDRIP? = null
    private var mLocalIPv6: String? = null
    private val dnsList: MutableList<String> = mutableListOf()
    private val mRoutes = NetworkSpace()
    private val mRoutesv6 = NetworkSpace()

    override fun setMtu(mtu: Int) {
        this.mtu = mtu
    }

    override fun addDNS(dns: String?) {
        dns?.let { this.dnsList.add(it) }
    }

    override fun addRoute(route: CIDRIP?, include: Boolean) {
        mRoutes.addIP(route, include)
    }

    override fun addRoute(dest: String?, mask: String?, gateway: String?, device: String?) {
        val route = CIDRIP(dest!!, mask!!)
        var include = isAndroidTunDevice(device)

        val gatewayIP = IpAddress(CIDRIP(gateway!!, 32), false)

        if (localIp == null) {
            OpenVPNLogger.e("OpenVPNService", "Local IP address unset and received. Neither pushed server config nor local config specifies an IP addresses. Opening tun device is most likely going to fail.")
            return
        }
        val localNet = IpAddress(localIp, true)
        if (localNet.containsNet(gatewayIP)) include = true

        if (gateway != null && (gateway.equals("255.255.255.255"))) {
            include = true
        }


        if (route.len == 32 && !mask.equals("255.255.255.255")) {
            OpenVPNLogger.e("OpenVPNService", dest)
        }

        if (route.normalise()) {
            OpenVPNLogger.e("OpenVPNService", "normalise(): dest, route.len, route.mIp")
        }

        mRoutes.addIP(route, include)
    }

    override fun addRoutev6(network: String?, device: String?) {
        val included = isAndroidTunDevice(device)
        network?.let { addRoutev6(network, included) }
    }

    fun addRoutev6(network: String, included: Boolean) {
        val v6parts = network.split("/".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        try {
            val ip = InetAddress.getAllByName(v6parts[0])[0] as Inet6Address
            val mask = v6parts[1].toInt()
            mRoutesv6.addIPv6(ip, mask, included)
        } catch (e: UnknownHostException) {
            OpenVPNLogger.e("OpenVPNService", "Error: $e")
        }
    }

    override fun setDomain(domain: String?) {
        domain?.let { this.domain = it }
    }

    override fun addHttpProxy(proxy: String?, port: Int): Boolean {
        return false
    }

    override fun protectFd(fd: Int): Boolean {
        return this.protect(fd)
    }

    override fun openTun(): ParcelFileDescriptor? {
        return startTun()
    }

    override fun setLocalIP(cidrip: CIDRIP?) {
        this.localIp = cidrip
    }

    override fun setLocalIPv6(ipv6addr: String?) {
        this.mLocalIPv6 = ipv6addr
    }

    override fun trigger_sso(info: String?) {

    }

    override val ctResolver: ContentResolver
        get() = contentResolver
    override val connectivityManager: ConnectivityManager
        get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun openvpnStopped() {
        stopOpenVPN()
    }

    override fun updateState(state: ConnectionState) {
        stateCached = state
        stateCallback.sendCallback { it.stateChanged(state) }
        OpenVPNLogger.d("OpenVPNService", "State: $state")
    }

    private fun isAndroidTunDevice(device: String?): Boolean {
        return device != null &&
                (device.startsWith("tun") || "(null)" == device || VPNSERVICE_TUN == device)
    }

    companion object {
        const val MANAGEMENT_THREAD_NAME = "OpenVPNManagementThread"
    }
}
