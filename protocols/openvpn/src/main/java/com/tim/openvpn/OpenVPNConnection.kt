package com.tim.openvpn

import android.content.Context
import com.tim.basevpn.connection.VpnConnection
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.service.OpenVPNService

class OpenVPNConnection(
    context: Context,
    stateListener: ((ConnectionState) -> Unit)? = null
) : VpnConnection<OpenVPNService>(
    context = context,
    clazz = OpenVPNService::class.java,
    stateListener = stateListener
)