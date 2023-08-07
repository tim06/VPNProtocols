package com.tim.shadowsocksr.delegate

import android.content.Context
import com.tim.basevpn.delegate.VPNRunner
import com.tim.basevpn.delegate.VpnConnectionServiceDelegate
import com.tim.basevpn.state.ConnectionState
import com.tim.shadowsocksr.service.ShadowsocksService
import kotlin.properties.ReadOnlyProperty

/**
 * Delegate for establish shadowsocksR
 * VPN connection
 *
 * @param config user configuration
 * @param stateListener listener for receive state update
 *
 * @Author: Timur Hojatov
 */
fun shadowsocksR(
    stateListener: ((ConnectionState) -> Unit),
    trafficListener: ((Long, Long, Long, Long) -> Unit)? = null
): ReadOnlyProperty<Context, VPNRunner> = VpnConnectionServiceDelegate(
    clazz = ShadowsocksService::class.java,
    stateListener = stateListener,
    trafficListener = trafficListener
)