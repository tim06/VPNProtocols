package com.tim.vpnprotocols.view.openvpn

import android.os.Bundle
import android.view.View
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.permission.isVpnPermissionGranted
import com.tim.basevpn.permission.vpnPermissionResult
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.OpenVPNConfig
import com.tim.openvpn.delegate.openVPN
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.OpenvpnFragmentLayoutBinding

/**
 * OpenVPN implementation
 *
 * @Author: Timur Hojatov
 */
class OpenvpnFragment : Fragment(R.layout.openvpn_fragment_layout) {

    private val layoutBinding: OpenvpnFragmentLayoutBinding by viewBinding()

    private val vpnService by requireActivity().openVPN(
        config = OpenVPNConfig()
    ) { connectionStatus ->
        updateConnectionState(connectionStatus)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutBinding.apply {
            startButton.setOnClickListener {
                vpnService.start()
            }
            stopButton.setOnClickListener {
                vpnService.stop()
            }
        }
    }

    private fun updateConnectionState(state: ConnectionState) {
        layoutBinding.stateTextView.text = state.name
    }
}
