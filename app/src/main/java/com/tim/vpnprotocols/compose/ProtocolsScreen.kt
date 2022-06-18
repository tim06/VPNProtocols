package com.tim.vpnprotocols.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tim.vpnprotocols.compose.openvpn.OpenVPNScreen
import com.tim.vpnprotocols.compose.shadowsocksr.ShadowsocksRScreen

/**
 * @Author: Timur Hojatov
 */
const val PROTOCOLS_SCREEN = "PROTOCOLS_SCREEN"
const val OPENVPN_SCREEN = "OPENVPN_SCREEN"
const val SHADOWSOCKSR_SCREEN = "SHADOWSOCKSR_SCREEN"

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
