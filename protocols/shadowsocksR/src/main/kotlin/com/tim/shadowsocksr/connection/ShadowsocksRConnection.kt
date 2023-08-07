package com.tim.shadowsocksr.connection

import android.content.Context
import com.tim.basevpn.connection.VpnConnection
import com.tim.basevpn.state.ConnectionState
import com.tim.shadowsocksr.service.ShadowsocksService

class ShadowsocksRConnection(
    context: Context,
    stateListener: ((ConnectionState) -> Unit)? = null
) : VpnConnection<ShadowsocksService>(
    context = context,
    clazz = ShadowsocksService::class.java,
    stateListener = stateListener
)