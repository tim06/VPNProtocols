@file:Suppress("WildcardImport")
package com.tim.vpnprotocols.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tim.vpnprotocols.compose.edit.ConfigEditScreen
import com.tim.vpnprotocols.compose.navigation.VpnProtocol
import com.tim.vpnprotocols.compose.openvpn.OpenVPNScreen
import com.tim.vpnprotocols.compose.shadowsocksr.ShadowsocksRScreen

/**
 * @Author: Timur Hojatov
 */
const val PROTOCOLS_SCREEN = "PROTOCOLS_SCREEN"
const val OPENVPN_SCREEN = "OPENVPN_SCREEN"
const val CONFIG_EDIT_SCREEN = "CONFIG_EDIT_SCREEN"
const val CONFIG_EDIT_SCREEN_ROUTE = "CONFIG_EDIT_SCREEN/{type}"
const val SHADOWSOCKSR_SCREEN = "SHADOWSOCKSR_SCREEN"

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtocolsScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val title = when (navBackStackEntry?.destination?.route) {
                        PROTOCOLS_SCREEN -> "Choose Protocol"
                        OPENVPN_SCREEN -> "OpenVPN"
                        SHADOWSOCKSR_SCREEN -> "ShadowsocksR"
                        CONFIG_EDIT_SCREEN_ROUTE -> "Config Edit"
                        else -> "Protocols"
                    }
                    Text(title)
                },
                navigationIcon = {
                    if (navBackStackEntry?.destination?.route != PROTOCOLS_SCREEN) {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            when (val screen = navBackStackEntry?.destination?.route) {
                OPENVPN_SCREEN,
                SHADOWSOCKSR_SCREEN -> {
                    val protocolKey = when (screen) {
                        OPENVPN_SCREEN -> VpnProtocol.OPENVPN
                        else -> VpnProtocol.SHADOWSOCKSR
                    }
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(
                                route = "$CONFIG_EDIT_SCREEN/$protocolKey"
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            }
        }
    ) { paddings ->
        NavHost(
            modifier = Modifier.padding(paddings),
            navController = navController,
            startDestination = PROTOCOLS_SCREEN
        ) {
            composable(PROTOCOLS_SCREEN) { Protocols(navController) }
            composable(OPENVPN_SCREEN) { OpenVPNScreen() }
            composable(SHADOWSOCKSR_SCREEN) { ShadowsocksRScreen() }
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
                ConfigEditScreen(protocol)
            }
        }
    }
}

@Composable
fun Protocols(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
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
