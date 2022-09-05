@file:Suppress("WildcardImport")

package com.tim.vpnprotocols.compose.shadowsocksr

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.vpnprotocols.compose.CONFIG_EDIT_SCREEN
import com.tim.vpnprotocols.compose.base.AppTopBar
import com.tim.vpnprotocols.compose.base.VpnScreen
import com.tim.vpnprotocols.compose.edit.ConfigEditViewModel
import com.tim.vpnprotocols.compose.navigation.VpnProtocol
import com.tim.vpnprotocols.compose.shadowsocksr.controller.rememberShadowsocksRController
import org.koin.androidx.compose.getViewModel
import org.koin.core.qualifier.named

/**
 * @Author: Timur Hojatov
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadowsocksRScreen(
    navController: NavController
) {
    val viewModel = getViewModel<ConfigEditViewModel<ShadowsocksRVpnConfig>>(
        qualifier = named(ShadowsocksRVpnConfig::class.java.name)
    )
    var config by remember { mutableStateOf<ShadowsocksRVpnConfig?>(null) }
    LaunchedEffect(Unit) {
        config = viewModel.getConfig()
    }
    Scaffold(
        topBar = {
            AppTopBar(title = "ShadowsocksR") {
                navController.popBackStack()
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        route = "$CONFIG_EDIT_SCREEN/${VpnProtocol.SHADOWSOCKSR}"
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (config == null) {
                ShadowsocksrEmptyScreen()
            } else {
                val shadowsocksRController = rememberShadowsocksRController(config = config!!)
                VpnScreen(vpnController = shadowsocksRController)
            }
        }
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
