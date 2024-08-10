package com.tim.vpnprotocols.root

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.SelectProtocolFragmentBinding

class SelectProtocolViewFragment : Fragment(R.layout.select_protocol_fragment) {

    private val selectProtocolFragmentBinding: SelectProtocolFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectProtocolFragmentBinding.shadowsocksrButton.setOnClickListener {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_typeUiFragment_to_shadowsocksrFragment)
        }
        selectProtocolFragmentBinding.openvpnButton.setOnClickListener {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_typeUiFragment_to_openvpnFragment)
        }
        selectProtocolFragmentBinding.ikev2Button.setOnClickListener {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_typeUiFragment_to_ikev2Fragment)
        }
        selectProtocolFragmentBinding.xtlsButton.setOnClickListener {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_typeUiFragment_to_XTLSServiceFragment)
        }
        selectProtocolFragmentBinding.xrayNgButton.setOnClickListener {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_typeUiFragment_to_xrayNgFragment)
        }
        selectProtocolFragmentBinding.xrayNekoButton.setOnClickListener {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_typeUiFragment_to_xrayNekoIntentFragment)
        }
    }
}
