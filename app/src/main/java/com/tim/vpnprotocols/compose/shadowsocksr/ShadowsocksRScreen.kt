package com.tim.vpnprotocols.compose.shadowsocksr

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.vpnprotocols.compose.base.VpnScreen
import com.tim.vpnprotocols.compose.shadowsocksr.controller.rememberShadowsocksRController
import com.tim.vpnprotocols.compose.viewmodel.ConfigFetcherViewModel
import com.tim.vpnprotocols.compose.viewmodel.configFetcherViewModelFactory
import com.tim.vpnprotocols.compose.viewmodel.getCreationExtras

/**
 * @Author: Timur Hojatov
 */
@Composable
fun ShadowsocksRScreen(
    viewModel: ConfigFetcherViewModel = configFetcherViewModelFactory.create(
        modelClass = ConfigFetcherViewModel::class.java,
        extras = getCreationExtras(LocalContext.current)
    )
) {
    val config by viewModel.getConfig<ShadowsocksRVpnConfig>().collectAsState(initial = null)
    if (config == null) {
        ShadowsocksrEmptyScreen()
    } else {
        val shadowsocksRController = rememberShadowsocksRController(config = config!!)
        VpnScreen(vpnController = shadowsocksRController)
    }
}

@Composable
fun ShadowsocksrEmptyScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Config not found, configure params in settings"
        )
    }
}
