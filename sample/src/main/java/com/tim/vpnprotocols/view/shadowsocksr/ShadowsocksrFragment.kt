package com.tim.vpnprotocols.view.shadowsocksr

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.state.ConnectionState
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.shadowsocksr.delegate.shadowsocksR
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.ShadowsocksFragmentLayoutBinding

/**
 * Shadowsocks implementation
 *
 * @Author: Timur Hojatov
 */
class ShadowsocksrFragment : Fragment(R.layout.shadowsocks_fragment_layout) {

    private val Context.vpnService by shadowsocksR(
        stateListener = { connectionStatus ->
            updateState(connectionStatus)
        },
        trafficListener = { txTotal, rxTotal, txRate, rxRate ->

        }
    )

    private val layoutBinding: ShadowsocksFragmentLayoutBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutBinding.apply {
            startButton.setOnClickListener {
                startVpn()
            }
            stopButton.setOnClickListener {
                stopVpn()
            }
        }
    }

    private fun startVpn() {
        requireContext().applicationContext.vpnService.start(
            config = ShadowsocksRVpnConfig(
                host = "212.113.122.223",
                password = "12345678"
            )
        )
    }

    private fun stopVpn() {
        requireContext().applicationContext.vpnService.stop()
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
