package com.tim.openvpn.delegate

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistryOwner
import com.tim.basevpn.delegate.VpnConnectionServiceDelegate
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.OpenVPNConfig
import com.tim.openvpn.service.OpenVPNService

fun ComponentActivity.openVPN(
    config: OpenVPNConfig,
    stateListener: ((ConnectionState) -> Unit)
) = (this as ActivityResultRegistryOwner).openVPN(
    config = config,
    stateListener = stateListener
)

fun ActivityResultRegistryOwner.openVPN(
    config: OpenVPNConfig,
    stateListener: ((ConnectionState) -> Unit)
) = VpnConnectionServiceDelegate(
    activityResultRegistryOwner = this,
    config = config,
    clazz = OpenVPNService::class.java,
    stateListener = stateListener
)
