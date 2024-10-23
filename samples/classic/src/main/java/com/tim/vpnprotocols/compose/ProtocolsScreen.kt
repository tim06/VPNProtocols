@file:Suppress("WildcardImport")

package com.tim.vpnprotocols.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tim.vpnprotocols.compose.base.AppTopBar
import com.tim.vpnprotocols.compose.edit.ConfigEditScreen
import com.tim.vpnprotocols.compose.navigation.VpnProtocol
import com.tim.vpnprotocols.compose.openvpn.OpenVPNScreen
import com.tim.vpnprotocols.compose.shadowsocksr.ShadowsocksRScreen

const val PROTOCOLS_SCREEN = "PROTOCOLS_SCREEN"
const val OPENVPN_SCREEN = "OPENVPN_SCREEN"
const val CONFIG_EDIT_SCREEN = "CONFIG_EDIT_SCREEN"
const val CONFIG_EDIT_SCREEN_ROUTE = "CONFIG_EDIT_SCREEN/{type}"
const val SHADOWSOCKSR_SCREEN = "SHADOWSOCKSR_SCREEN"

@Suppress("LongMethod")
@Composable
fun ProtocolsScreen() {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = PROTOCOLS_SCREEN
    ) {
        composable(PROTOCOLS_SCREEN) { Protocols(navController) }
        composable(OPENVPN_SCREEN) { OpenVPNScreen(navController) }
        composable(SHADOWSOCKSR_SCREEN) { ShadowsocksRScreen(navController) }
        composable(
            route = CONFIG_EDIT_SCREEN_ROUTE,
            arguments = listOf(
                navArgument(
                    name = "VPNProtocol"
                ) {
                    nullable = false
                    type = NavType.EnumType(VpnProtocol::class.java)
                    defaultValue = VpnProtocol.OPENVPN
                }
            )
        ) { backStackEntry ->
            val protocol = backStackEntry.arguments
                ?.getString("type", null)
                ?.let { VpnProtocol.valueOf(it) }
                ?: VpnProtocol.OPENVPN
            ConfigEditScreen(protocol, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Protocols(navController: NavController) {
    Scaffold(
        topBar = {
            AppTopBar(title = "Choose Protocol", showBack = false) {
                // TODO implement
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        navController.navigate(OPENVPN_SCREEN)
                    }
                ) {
                    Text(text = "OpenVPN")
                }
                Button(
                    onClick = {
                        navController.navigate(SHADOWSOCKSR_SCREEN)
                    }
                ) {
                    Text(text = "ShadowsocksR")
                }
            }
        }
    }
}
