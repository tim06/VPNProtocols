package com.tim.xtlsr

import android.content.Context
import com.tim.basevpn.connection.VpnConnection
import com.tim.basevpn.state.ConnectionState
import io.nekohasekai.sagernet.bg.VpnService

class XTLSRConnection(
    context: Context,
    stateListener: ((ConnectionState) -> Unit)? = null
) : VpnConnection<VpnService>(
    context = context,
    clazz = VpnService::class.java,
    stateListener = stateListener
)