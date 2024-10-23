package com.tim.vpnprotocols.view.shadowsocksr

import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.state.ConnectionState
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.shadowsocksr.service.ShadowsocksRService
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.ShadowsocksFragmentLayoutBinding
import com.tim.vpnprotocols.util.initStateListener
import com.tim.vpnprotocols.view.base.BaseVpnFragment

/**
 * Shadowsocks implementation
 */
class ShadowsocksrFragment : BaseVpnFragment(R.layout.shadowsocks_fragment_layout) {

    private val layoutBinding: ShadowsocksFragmentLayoutBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initStateListener(ShadowsocksRService::class.java.simpleName, ::updateState)
        layoutBinding.apply {
            startButton.setOnClickListener {
                start()
            }
            stopButton.setOnClickListener {
                stopVpn()
            }
        }
    }

    override fun launch() {
        ShadowsocksRService.startService(
            context = requireContext(),
            config = ShadowsocksRVpnConfig(
                name="ShadowsockR configuration",
                host="1.1.1.1", // add ip here
                localPort=1080,
                remotePort=443,
                password="ATwoT@@Pc5",
                protocol="origin",
                protocolParam="",
                obfs="http_simple_compatible",
                obfsParam="",
                method="chacha20",
                dnsAddress="8.8.8.8",
                dnsPort="53"
            )
        )
    }

    private fun updateState(connectionState: ConnectionState) {
        layoutBinding.stateTextView.text = connectionState.name
    }

    private fun stopVpn() {
        ShadowsocksRService.stopService(requireContext())
    }
}
