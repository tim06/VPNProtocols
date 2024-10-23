package com.tim.vpnprotocols.view.xrayNg

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.connection.ConnectionListener
import com.tim.basevpn.state.ConnectionState
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.ShadowsocksFragmentLayoutBinding
import com.tim.vpnprotocols.view.base.BaseVpnFragment
import io.github.tim06.xrayConfiguration.XrayConfiguration
import io.github.tim06.xrayConfiguration.XrayConfiguration.Companion.addMinimalSettings
import io.github.tim06.xrayConfiguration.parser.parse
import io.github.tim06.xrayConfiguration.parser.toConfiguration
import kotlinx.coroutines.launch

class XrayNgServiceFragment : BaseVpnFragment(R.layout.shadowsocks_fragment_layout) {

    private val layoutBinding: ShadowsocksFragmentLayoutBinding by viewBinding()

    val configUri = ""
    //val configUri = ""
    private val configuration by lazy { buildConfiguration() }

    private var iVpnService: IVPNService? = null
    private val connectionStateListener: IConnectionStateListener = object : ConnectionListener() {
        override fun stateChanged(status: ConnectionState?) {
            lifecycleScope.launch {
                status?.let { updateState(it) }
            }
        }

        override fun trafficUpdate(txRate: Long, rxRate: Long, txTotal: Long, rxTotal: Long) = Unit
    }
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            iVpnService = IVPNService.Stub.asInterface(service)
            iVpnService?.registerCallback(connectionStateListener)
            iVpnService?.let(lastAction)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            iVpnService = null
        }
    }

    private var lastAction: (vpnService: IVPNService) -> Unit = { }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutBinding.startButton.setOnClickListener { start() }
        layoutBinding.stopButton.setOnClickListener {
            /*if (XRayNgService.isActive(requireContext()) || isProcessRunning(requireContext(), "com.tim.vpnprotocols.debug:xrayNg")) {
                serviceAction { stopVPN() }
            } else {
                showSnackbar("Not found xray process!")
            }*/
        }
    }

    override fun launch() {
        serviceAction { startVPN() }
    }

    private fun updateState(connectionState: ConnectionState) {
        layoutBinding.stateTextView.text = connectionState.name
    }

    private fun serviceAction(action: IVPNService.() -> Unit) {
        lastAction = action
        runCatching { requireContext().unbindService(serviceConnection) }

        /*requireContext().bindService(
            XRayNgService.buildIntent(
                context = requireContext(),
                config = configuration?.raw().orEmpty(),
                domain = configuration?.domain().orEmpty()
            ),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )*/
    }

    private fun buildConfiguration(): XrayConfiguration? {
        val parsedConfiguration = parse(configUri)
        return parsedConfiguration?.toConfiguration()?.addMinimalSettings()
    }
}