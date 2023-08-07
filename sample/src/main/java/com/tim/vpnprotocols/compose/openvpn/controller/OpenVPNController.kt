package com.tim.vpnprotocols.compose.openvpn.controller

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.tim.basevpn.configuration.VpnConfiguration
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.configuration.OpenVPNConfig
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
            config = config,
            context = context
        )
    }
    // TODO find way to retrieve registry from compose context
}

class OpenVPNController(
    config: OpenVPNConfig,
    private val context: Context
) : BaseController {

    private val mutableStateFlow = MutableStateFlow(ConnectionState.IDLE)
    override val connectionState: StateFlow<ConnectionState> = mutableStateFlow.asStateFlow()

    private val Context.vpnService by openVPN { connectionStatus ->
        mutableStateFlow.value = connectionStatus
    }

    override fun startVpn() {
        mutableStateFlow.value = ConnectionState.CONNECTING
        context.vpnService.start(
            config = VpnConfiguration(OpenVPNConfig(), emptySet())
        )
    }

    override fun stopVpn() {
        mutableStateFlow.value = ConnectionState.DISCONNECTING
        context.vpnService.stop()
    }
}
