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
import io.github.tim06.xrayConfiguration.XrayConfiguration.Companion.addMinimalSettings
import io.github.tim06.xrayConfiguration.parser.toConfiguration

class XrayNgIntentFragment : BaseVpnFragment(R.layout.shadowsocks_fragment_layout) {

    private val layoutBinding: ShadowsocksFragmentLayoutBinding by viewBinding()

    val configUri = ""
    val configUri2 = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initStateListener(XRayNgService::class.java.simpleName, ::updateState)
        layoutBinding.startButton.setOnClickListener { start() }
        layoutBinding.stopButton.setOnClickListener { XRayNgService.stopService(requireContext()) }
    }

    override fun launch() {
        val configuration = io.github.tim06.xrayConfiguration.parser.parse(configUri/*listOf(configUri,configUri2)*/)
            ?.toConfiguration()
            ?.addMinimalSettings()
        val domain = configuration?.domain().orEmpty()
        XRayNgService.startService(
            context = requireContext(),
            config = configuration?.raw().orEmpty(),
            domain = domain
        )
    }

    private fun updateState(connectionState: ConnectionState) {
        layoutBinding.stateTextView.text = connectionState.name
    }
}