package com.tim.vpnprotocols.view.xrayNeko

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.state.ConnectionState
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.ShadowsocksFragmentLayoutBinding
import com.tim.vpnprotocols.util.initStateListener
import com.tim.vpnprotocols.view.base.BaseVpnFragment
import com.tim.vpnprotocols.xrayNeko.fmt.ProxyEntity
import com.tim.vpnprotocols.xrayNeko.parser.RawUpdater
import com.tim.vpnprotocols.xrayNeko.XRayNekoService
import com.tim.vpnprotocols.xrayNeko.fmt.buildConfig
import com.tim.vpnprotocols.xrayNeko.fmt.naive.NaiveBean
import com.tim.vpnprotocols.xrayNeko.fmt.naive.buildNaiveConfig
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class XrayNekoIntentFragment : BaseVpnFragment(R.layout.shadowsocks_fragment_layout) {

    private val layoutBinding: ShadowsocksFragmentLayoutBinding by viewBinding()

    //val configUri = "vless://8da3432a-d4da-4f43-801f-342cda5eae9f@95.164.8.185:8080?security=reality&encryption=none&pbk=MKfDEJAAbEk2S49G67c1m49oFc8BLwd-0jgfiygcOFg&headerType=none&fp=chrome&type=tcp&flow=xtls-rprx-vision&sni=www.speedtest.net&sid=7554c6ea#%F0%9F%87%AA%F0%9F%87%AA+EE+1"
    val configUri = "naive+https://DKkd2md:dksddk22@212.102.54.33:49491/?sni=tesss.phooeyunfold.com#naive"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initStateListener(XRayNekoService::class.java.simpleName, ::updateState)
        layoutBinding.startButton.setOnClickListener { start() }
        layoutBinding.stopButton.setOnClickListener { XRayNekoService.stopService(requireContext()) }
    }

    override fun launch() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                RawUpdater.parseRaw(text = configUri)?.firstOrNull()?.let { bean ->
                    val profile = ProxyEntity(groupId = 0).apply {
                        putBean(bean)
                    }
                    val configuration = buildConfig(context = requireContext(), proxy = profile)

                    val external = configuration.externalIndex.firstOrNull()?.chain?.entries?.firstOrNull()
                    val bean = external?.value?.requireBean()
                    val naiveConfig = when (bean) {
                        is NaiveBean -> {
                            bean.buildNaiveConfig(external.key)
                        }
                        else -> null
                    }
                    XRayNekoService.startService(
                        context = requireContext(),
                        config = configuration.config,
                        naiveConfig = naiveConfig
                    )
                }
            }
        }
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