package com.tim.openvpn.delegate

import android.content.Context
import com.tim.basevpn.delegate.VPNRunner
import com.tim.basevpn.delegate.VpnConnectionServiceDelegate
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.service.OpenVPNService
import kotlin.properties.ReadOnlyProperty

fun openVPN(
    stateListener: (ConnectionState) -> Unit
): ReadOnlyProperty<Context, VPNRunner> = VpnConnectionServiceDelegate(
    clazz = OpenVPNService::class.java,
    stateListener = stateListener
)
