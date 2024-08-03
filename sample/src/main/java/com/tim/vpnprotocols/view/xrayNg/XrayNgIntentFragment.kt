package com.tim.vpnprotocols.view.xrayNg

import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.state.ConnectionState
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.ShadowsocksFragmentLayoutBinding
import com.tim.vpnprotocols.util.initStateListener
import com.tim.vpnprotocols.view.base.BaseVpnFragment
import com.tim.vpnprotocols.xrayNg.XRayNgService
import io.github.tim06.Protocol
import io.github.tim06.XrayConfiguration
import io.github.tim06.dns.Dns
import io.github.tim06.dns.DnsServer
import io.github.tim06.inbounds.Destination
import io.github.tim06.inbounds.Inbound
import io.github.tim06.inbounds.Sniffing
import io.github.tim06.inbounds.settings.HttpInboundConfigurationObject
import io.github.tim06.inbounds.settings.SocksInboundConfigurationObject
import io.github.tim06.log.Log
import io.github.tim06.log.LogLevel
import io.github.tim06.outbounds.settings.ShadowsocksOutboundConfigurationObject
import io.github.tim06.parser.parse
import io.github.tim06.routing.DomainStrategy
import io.github.tim06.routing.Routing
import io.github.tim06.routing.Rule

class XrayNgIntentFragment : BaseVpnFragment(R.layout.shadowsocks_fragment_layout) {

    private val layoutBinding: ShadowsocksFragmentLayoutBinding by viewBinding()

    val configUri = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initStateListener(XRayNgService::class.java.simpleName, ::updateState)
        layoutBinding.startButton.setOnClickListener { start() }
        layoutBinding.stopButton.setOnClickListener { XRayNgService.stopService(requireContext()) }
    }

    override fun launch() {
        val configuration = buildConfiguration()
        val protocolOutboundSettings = (configuration?.outbounds?.firstOrNull()?.settings as? ShadowsocksOutboundConfigurationObject)?.servers?.firstOrNull()
        val domain = "${protocolOutboundSettings?.address}:${protocolOutboundSettings?.port}"
        XRayNgService.startService(
            context = requireContext(),
            config = configuration?.raw().orEmpty(),
            domain = domain
        )
    }

    private fun updateState(connectionState: ConnectionState) {
        layoutBinding.stateTextView.text = connectionState.name
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