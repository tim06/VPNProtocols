package com.tim.shadowsocksr.delegate

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistryOwner
import com.tim.basevpn.delegate.vpnDelegate
import com.tim.basevpn.state.ConnectionState
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.shadowsocksr.service.ShadowsocksService

/**
 * Delegate for establish shadowsocksR
 * VPN connection
 *
 * @param config user configuration
 * @param stateListener listener for receive state update
 *
 * @Author: Timur Hojatov
 */
fun ActivityResultRegistryOwner.shadowsocksR(
    config: ShadowsocksRVpnConfig,
    stateListener: ((ConnectionState) -> Unit)
) = (this as ComponentActivity).vpnDelegate<ShadowsocksRVpnConfig, ShadowsocksService>(
    config = config,
    stateListener = stateListener
)
