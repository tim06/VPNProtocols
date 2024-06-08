package com.tim.vpnprotocols.xrayNg

import android.content.Context
import com.tim.basevpn.connection.VpnServiceConnection
import com.tim.basevpn.state.ConnectionState

class XrayNgConnection(
    context: Context,
    stateListener: ((ConnectionState) -> Unit)? = null
) : VpnServiceConnection(
    context = context,
    clazz = XRayNgService::class.java,
    stateListener = stateListener
)