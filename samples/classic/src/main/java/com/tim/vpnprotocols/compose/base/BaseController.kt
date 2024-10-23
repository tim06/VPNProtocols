package com.tim.vpnprotocols.compose.base

import com.tim.basevpn.state.ConnectionState
import kotlinx.coroutines.flow.StateFlow

interface BaseController {
    val connectionState: StateFlow<ConnectionState>
    fun startVpn()
    fun stopVpn()
}
