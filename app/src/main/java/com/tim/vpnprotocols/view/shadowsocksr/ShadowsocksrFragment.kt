package com.tim.vpnprotocols.view.shadowsocksr

import android.os.Bundle
import android.view.View
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.permission.isVpnPermissionGranted
import com.tim.basevpn.permission.vpnPermissionResult
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

    private val layoutBinding: ShadowsocksFragmentLayoutBinding by viewBinding()

    private val vpnPermission = vpnPermissionResult { isSuccess ->
        if (isSuccess) {
            startVpn()
        }
    }

    private val vpnService by requireActivity().shadowsocksR(
        config = ShadowsocksRVpnConfig()
    ) { connectionStatus ->
        updateState(connectionStatus)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutBinding.apply {
            startButton.setOnClickListener {
                if (requireContext().isVpnPermissionGranted()) {
                    startVpn()
                } else {
                    vpnPermission.launch()
                }
            }
            stopButton.setOnClickListener {
                stopVpn()
            }
        }
    }

    private fun startVpn() {
        vpnService.start()
    }

    private fun stopVpn() {
        vpnService.stop()
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
        }
    }
}
