package com.tim.vpnprotocols.compose.openvpn

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tim.openvpn.OpenVPNConfig
import com.tim.vpnprotocols.compose.base.VpnScreen
import com.tim.vpnprotocols.compose.openvpn.controller.rememberOpenVPNController
import com.tim.vpnprotocols.compose.viewmodel.ConfigFetcherViewModel
import com.tim.vpnprotocols.compose.viewmodel.configFetcherViewModelFactory
import com.tim.vpnprotocols.compose.viewmodel.getCreationExtras

/**
 *
 * @Author: Timur Hojatov
 */
@Composable
fun OpenVPNScreen(
    viewModel: ConfigFetcherViewModel = configFetcherViewModelFactory.create(
        modelClass = ConfigFetcherViewModel::class.java,
        extras = getCreationExtras(LocalContext.current)
    )
) {
    val config by viewModel.getConfig<OpenVPNConfig>().collectAsState(initial = null)
    if (config == null) {
        OpenVPNEmptyScreen()
    } else {
        val openVPNController = rememberOpenVPNController(config = config!!)
        VpnScreen(vpnController = openVPNController)
    }
}

@Composable
fun OpenVPNEmptyScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Config not found, configure params in settings"
        )
    }
}
/*val openVPNController = rememberOpenVPNController(
    config = OpenVPNConfig(
        name = "Config",
        host = "1.1.1.1",
        port = 443,
        type = "tcp-client",
        cipher = "AES-256-CBC",
        auth = "SHA512",
        ca = caMock,
        key = keyMock,
        cert = certMock,
        tlsCrypt = tlsCryptMock
    )
)*/
