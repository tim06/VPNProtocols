package com.tim.vpnprotocols.view.ikev2

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.state.ConnectionState
import com.tim.ikev2.Ikev2Configuration
import com.tim.ikev2.Ikev2Connection
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.OpenvpnFragmentLayoutBinding
import com.tim.vpnprotocols.view.shadowsocksr.VpnActivityResultContract

class Ikev2Fragment : Fragment(R.layout.openvpn_fragment_layout) {

    private val pushNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->

    }

    private val vpnConnection by lazy {
        Ikev2Connection(
            context = requireContext(),
            stateListener = ::updateConnectionState
        )
    }

    private val vpnPermission = registerForActivityResult(VpnActivityResultContract()) {
        if (it) {
            vpnConnection.start(
                /*Ikev2Configuration(
                    name = "qwe",
                    host = "",
                    login = "",
                    password = "",
                )*/
                Ikev2Configuration(
                    name = "qwe",
                    host = if (clicked % 2 == 0) "" else "",
                    login = "",
                    password = "",
                )
            )
            clicked += 1
        }
    }

    private var clicked = 0

    private val layoutBinding: OpenvpnFragmentLayoutBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        vpnConnection.bindService(true)
        layoutBinding.apply {
            startButton.setOnClickListener {
                vpnPermission.launch(Unit)
            }
            stopButton.setOnClickListener {
                vpnConnection.stop()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnConnection.apply {
            stopServiceIfNeed()
            clear()
        }
    }

    private fun updateConnectionState(state: ConnectionState) {
        layoutBinding.stateTextView.text = state.name
    }
}