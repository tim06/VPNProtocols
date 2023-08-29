package com.tim.openvpn.connection

import android.content.Context
import com.tim.basevpn.connection.VpnServiceConnection
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.service.OpenVPNService

class OpenVPNVpnConnection(
    context: Context,
    stateListener: ((ConnectionState) -> Unit)? = null
) : VpnServiceConnection(
    context = context,
    clazz = OpenVPNService::class.java,
    stateListener = stateListener
)