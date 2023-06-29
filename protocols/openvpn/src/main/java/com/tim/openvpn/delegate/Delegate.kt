package com.tim.openvpn.delegate

import android.content.Context
import com.tim.basevpn.delegate.VPNRunner
import com.tim.basevpn.delegate.VpnConnectionServiceDelegate
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.OpenVPNConfig
import com.tim.openvpn.service.OpenVPNService
import kotlin.properties.ReadOnlyProperty

fun openVPN(
    config: OpenVPNConfig,
    stateListener: (ConnectionState) -> Unit
): ReadOnlyProperty<Context, VPNRunner> = VpnConnectionServiceDelegate(
    //config = config,
    clazz = OpenVPNService::class.java,
    stateListener = stateListener
)
