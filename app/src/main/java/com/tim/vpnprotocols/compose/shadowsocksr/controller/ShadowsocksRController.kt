package com.tim.vpnprotocols.compose.shadowsocksr.controller

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.tim.basevpn.state.ConnectionState
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.shadowsocksr.delegate.shadowsocksR
import com.tim.basevpn.extension.getActivity
import com.tim.vpnprotocols.compose.base.BaseController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @Author: Timur Hojatov
 */

@Composable
fun rememberShadowsocksRController(config: ShadowsocksRVpnConfig): ShadowsocksRController {
    val context = LocalContext.current
    return remember(context) {
        ShadowsocksRController(
            context = context,
            config = config
        )
    }
}

class ShadowsocksRController(
    context: Context,
    config: ShadowsocksRVpnConfig
) : BaseController {

    private val mutableStateFlow = MutableStateFlow(ConnectionState.IDLE)
    override val connectionState: StateFlow<ConnectionState> = mutableStateFlow.asStateFlow()

    private val vpnService by context.getActivity().shadowsocksR(
        config = config
    ) { connectionStatus ->
        mutableStateFlow.value = connectionStatus
    }

    override fun startVpn() {
        vpnService.start()
    }

    override fun stopVpn() {
        vpnService.stop()
    }
}
