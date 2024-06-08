package com.tim.vpnprotocols.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.SelectProtocolFragmentBinding

class SelectProtocolFragment : Fragment(R.layout.select_protocol_fragment) {

    private val selectProtocolFragmentBinding: SelectProtocolFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectProtocolFragmentBinding.shadowsocksrButton.setOnClickListener {
            findNavController().navigate(R.id.action_selectProtocolFragment_to_shadowsocksrFragment)
        }
        selectProtocolFragmentBinding.openvpnButton.setOnClickListener {
            findNavController().navigate(R.id.action_selectProtocolFragment_to_openvpnFragment)
        }
        selectProtocolFragmentBinding.ikev2Button.setOnClickListener {
            findNavController().navigate(R.id.action_selectProtocolFragment_to_ikev2Fragment)
        }
        selectProtocolFragmentBinding.xtlsButton.setOnClickListener {
            findNavController().navigate(R.id.action_selectProtocolFragment_to_XTLSServiceFragment)
        }
        selectProtocolFragmentBinding.xrayNgButton.setOnClickListener {
            findNavController().navigate(R.id.action_selectProtocolFragment_to_xrayNgFragment)
        }
    }
}
