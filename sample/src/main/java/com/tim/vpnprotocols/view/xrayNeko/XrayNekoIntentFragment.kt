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
import com.tim.vpnprotocols.xrayNeko.XRayNekoService
import com.tim.vpnprotocols.xrayNeko.fmt.ProxyEntity
import com.tim.vpnprotocols.xrayNeko.fmt.buildConfig
import com.tim.vpnprotocols.xrayNeko.fmt.naive.NaiveBean
import com.tim.vpnprotocols.xrayNeko.fmt.naive.buildNaiveConfig
import com.tim.vpnprotocols.xrayNeko.parser.RawUpdater
import com.tim.vpnprotocols.xrayNeko.util.getNekoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class XrayNekoIntentFragment : BaseVpnFragment(R.layout.shadowsocks_fragment_layout) {

    private val layoutBinding: ShadowsocksFragmentLayoutBinding by viewBinding()

    val configUri = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initStateListener(XRayNekoService::class.java.simpleName, ::updateState)
        layoutBinding.backButton.setOnClickListener {
            val log = String(getNekoLog(requireContext(), 50 * 1024))
            log.lines().forEach { println("NekoLog: $it") }
        }
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
}