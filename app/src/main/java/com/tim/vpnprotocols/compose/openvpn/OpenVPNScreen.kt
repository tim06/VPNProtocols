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
        config = OpenVPNConfig(
            host = "46.229.214.180",
            port = 443,
            type = "tcp-client",
            cipher = "AES-256-CBC",
            auth = "SHA512",
            ca = caMock,
            key = keyMock,
            cert = certMock,
            tlsCrypt = tlsCryptMock
        )
    )
    VpnScreen(
        vpnController = openVPNController
    )
}
