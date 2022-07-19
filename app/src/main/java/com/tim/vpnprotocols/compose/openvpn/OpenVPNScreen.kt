package com.tim.vpnprotocols.compose.openvpn

import androidx.compose.runtime.Composable
import com.tim.openvpn.OpenVPNConfig
import com.tim.vpnprotocols.compose.base.VpnScreen
import com.tim.vpnprotocols.compose.openvpn.controller.rememberOpenVPNController

/**
 *
 * @Author: Timur Hojatov
 */
@Composable
fun OpenVPNScreen() {
    val openVPNController = rememberOpenVPNController(
        config = OpenVPNConfig()
    )
    VpnScreen(
        vpnController = openVPNController
    )
}
