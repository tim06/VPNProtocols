package com.tim.shadowsocksr.connection

import android.content.Context
import com.tim.basevpn.connection.VpnServiceConnection
import com.tim.basevpn.state.ConnectionState
import com.tim.shadowsocksr.service.ShadowsocksService

class ShadowsocksRVpnConnection(
    context: Context,
    stateListener: ((ConnectionState) -> Unit)? = null
) : VpnServiceConnection(
    context = context,
    clazz = ShadowsocksService::class.java,
    stateListener = stateListener
)