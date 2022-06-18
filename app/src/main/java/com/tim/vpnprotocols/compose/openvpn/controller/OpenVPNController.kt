package com.tim.vpnprotocols.compose.openvpn.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.extension.getActivity
import com.tim.openvpn.OpenVPNConfig
import com.tim.openvpn.delegate.openVPN
import com.tim.vpnprotocols.compose.base.BaseController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @Author: Timur Hojatov
 */

@Composable
fun rememberOpenVPNController(config: OpenVPNConfig): OpenVPNController {
    val context = LocalContext.current
    return remember(context) {
        OpenVPNController(
            context = context,
            config = config
        )
    }
}

class OpenVPNController(
    context: Context,
    config: OpenVPNConfig
) : BaseController {

    private val mutableStateFlow = MutableStateFlow(ConnectionState.IDLE)
    override val connectionState: StateFlow<ConnectionState> = mutableStateFlow.asStateFlow()

    private val vpnService by context.getActivity()!!.openVPN(
        config = config
    ) { connectionStatus ->
        mutableStateFlow.value = connectionStatus
    }

    override fun startVpn() {
        mutableStateFlow.value = ConnectionState.CONNECTING
        vpnService.start()
    }

    override fun stopVpn() {
        mutableStateFlow.value = ConnectionState.DISCONNECTING
        vpnService.stop()
    }
}
