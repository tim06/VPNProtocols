package com.tim.vpnprotocols.view.xrayNg

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.connection.ConnectionListener
import com.tim.basevpn.state.ConnectionState
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.ShadowsocksFragmentLayoutBinding
import com.tim.vpnprotocols.util.isProcessRunning
import com.tim.vpnprotocols.view.base.BaseVpnFragment
import com.tim.vpnprotocols.xrayNg.XRayNgService
import io.github.tim06.xrayConfiguration.Protocol
import io.github.tim06.xrayConfiguration.XrayConfiguration
import io.github.tim06.xrayConfiguration.dns.Dns
import io.github.tim06.xrayConfiguration.dns.DnsServer
import io.github.tim06.xrayConfiguration.inbounds.Destination
import io.github.tim06.xrayConfiguration.inbounds.Inbound
import io.github.tim06.xrayConfiguration.inbounds.Sniffing
import io.github.tim06.xrayConfiguration.inbounds.settings.HttpInboundConfigurationObject
import io.github.tim06.xrayConfiguration.inbounds.settings.SocksInboundConfigurationObject
import io.github.tim06.xrayConfiguration.log.Log
import io.github.tim06.xrayConfiguration.log.LogLevel
import io.github.tim06.xrayConfiguration.parser.parse
import io.github.tim06.xrayConfiguration.routing.DomainStrategy
import io.github.tim06.xrayConfiguration.routing.Routing
import io.github.tim06.xrayConfiguration.routing.Rule
import kotlinx.coroutines.launch

class XrayNgServiceFragment : BaseVpnFragment(R.layout.shadowsocks_fragment_layout) {

    private val layoutBinding: ShadowsocksFragmentLayoutBinding by viewBinding()

    val configUri = ""
    //val configUri = ""
    private val configuration by lazy { buildConfiguration() }

    private var iVpnService: IVPNService? = null
    private val connectionStateListener: IConnectionStateListener = object : ConnectionListener() {
        override fun stateChanged(status: ConnectionState?) {
            lifecycleScope.launch {
                status?.let { updateState(it) }
            }
        }

        override fun trafficUpdate(txRate: Long, rxRate: Long, txTotal: Long, rxTotal: Long) = Unit
    }
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            iVpnService = IVPNService.Stub.asInterface(service)
            iVpnService?.registerCallback(connectionStateListener)
            iVpnService?.let(lastAction)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            iVpnService = null
        }
    }

    private var lastAction: (vpnService: IVPNService) -> Unit = { }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutBinding.startButton.setOnClickListener { start() }
        layoutBinding.stopButton.setOnClickListener {
            if (XRayNgService.isActive(requireContext()) || isProcessRunning(requireContext(), "com.tim.vpnprotocols.debug:xrayNg")) {
                serviceAction { stopVPN() }
            } else {
                showSnackbar("Not found xray process!")
            }
        }
    }

    override fun launch() {
        serviceAction { startVPN() }
    }

    private fun updateState(connectionState: ConnectionState) {
        layoutBinding.stateTextView.text = connectionState.name
    }

    private fun serviceAction(action: IVPNService.() -> Unit) {
        lastAction = action
        runCatching { requireContext().unbindService(serviceConnection) }

        requireContext().bindService(
            XRayNgService.buildIntent(
                context = requireContext(),
                config = configuration?.raw().orEmpty(),
                domain = configuration?.domain().orEmpty()
            ),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun buildConfiguration(): XrayConfiguration? {
        val parsedConfiguration = parse(configUri)
        return parsedConfiguration?.copy(
            dns = Dns(
                servers = listOf(
                    DnsServer.DirectDnsServer("1.1.1.1"),
                    DnsServer.DirectDnsServer("8.8.8.8"),
                ),
            ),
            inbounds = listOf(
                Inbound(
                    listen = "127.0.0.1",
                    port = 10808,
                    protocol = Protocol.SOCKS,
                    settings = SocksInboundConfigurationObject(
                        auth = SocksInboundConfigurationObject.Auth.NOAUTH,
                        udp = true,
                        userLevel = 8
                    ),
                    sniffing = Sniffing(
                        destOverride = listOf(Destination.HTTP, Destination.TLS),
                        enabled = true,
                        routeOnly = false
                    ),
                    tag = "socks"
                ),
                Inbound(
                    listen = "127.0.0.1",
                    port = 10809,
                    protocol = Protocol.HTTP,
                    settings = HttpInboundConfigurationObject(
                        userLevel = 8
                    ),
                    tag = "http"
                )
            ),
            log = Log(level = LogLevel.Info),
            routing = Routing(
                domainStrategy = DomainStrategy.IPIfNonMatch,
                rules = listOf(
                    Rule(
                        ip = listOf("1.1.1.1"),
                        outboundTag = "proxy",
                        port = "53"
                    ),
                    Rule(
                        ip = listOf("8.8.8.8"),
                        outboundTag = "proxy",
                        port = "53"
                    ),
                    Rule(
                        outboundTag = "proxy",
                        port = "0-65535"
                    )
                )
            )
        )
    }
}