package com.tim.vpnprotocols.view.shadowsocksr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.state.ConnectionState
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.shadowsocksr.connection.ShadowsocksRVpnConnection
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.ShadowsocksFragmentLayoutBinding
import kotlinx.coroutines.launch

/**
 * Shadowsocks implementation
 *
 * @Author: Timur Hojatov
 */
class ShadowsocksrFragment : Fragment(R.layout.shadowsocks_fragment_layout) {

    private var connection: ShadowsocksRVpnConnection? = null

    private val layoutBinding: ShadowsocksFragmentLayoutBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        connection = ShadowsocksRVpnConnection(
            context = requireContext(),
            stateListener = { connectionStatus ->
                updateState(connectionStatus)
            }
        )
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutBinding.apply {
            startButton.setOnClickListener {
                startVpn()
            }
            stopButton.setOnClickListener {
                //stopVpn()
                onDestroy()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connection?.stopServiceIfNeed(false)
    }

    private fun startVpn() {
        lifecycleScope.launch {
            connection?.start(
                ShadowsocksRVpnConfig(
                    name="ShadowsockR configuration",
                    host="162.19.204.76",
                    localPort=1080,
                    remotePort=443,
                    password="asdKkaskJKfnsa",
                    protocol="origin",
                    protocolParam="",
                    obfs="http_simple",
                    obfsParam="",
                    method="aes-256-cfb",
                    dnsAddress="8.8.8.8",
                    dnsPort="53"
                )
            )
        }
    }

    private fun stopVpn() {
        lifecycleScope.launch {
            connection?.stop()
        }
    }

    private fun updateState(state: ConnectionState) {
        when (state) {
            ConnectionState.DISCONNECTED -> {
                layoutBinding.stateTextView.text = "STOPPED"
            }
            ConnectionState.DISCONNECTING -> {
                layoutBinding.stateTextView.text = "STOPPING"
            }
            ConnectionState.CONNECTING -> {
                layoutBinding.stateTextView.text = "CONNECTING"
            }
            ConnectionState.CONNECTED -> {
                layoutBinding.stateTextView.text = "CONNECTED"
            }
            else -> {
                layoutBinding.stateTextView.text = "STOPPED"
            }
        }
    }
}
