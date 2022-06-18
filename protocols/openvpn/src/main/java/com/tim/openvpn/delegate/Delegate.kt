package com.tim.openvpn.delegate

import androidx.lifecycle.LifecycleOwner
import com.tim.basevpn.delegate.VpnConnectionServiceDelegate
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.OpenVPNConfig
import com.tim.openvpn.service.OpenVPNService

fun LifecycleOwner.openVPN(
    config: OpenVPNConfig,
    stateListener: ((ConnectionState) -> Unit)
) = VpnConnectionServiceDelegate(
        lifecycleOwner = this,
        config = config,
        clazz = OpenVPNService::class.java,
        stateListener = stateListener
    )
