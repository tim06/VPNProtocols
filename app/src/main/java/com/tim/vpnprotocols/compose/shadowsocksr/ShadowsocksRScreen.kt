package com.tim.vpnprotocols.compose.shadowsocksr

import androidx.compose.runtime.Composable
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.vpnprotocols.compose.base.VpnScreen
import com.tim.vpnprotocols.compose.shadowsocksr.controller.rememberShadowsocksRController

/**
 * @Author: Timur Hojatov
 */
@Composable
fun ShadowsocksRScreen() {
    val shadowsocksRController = rememberShadowsocksRController(
        config = ShadowsocksRVpnConfig(
            host = "89.223.67.70",
            password = "ATwoT@@Pc5"
        )
    )
    VpnScreen(vpnController = shadowsocksRController)
}
