@file:Suppress("WildcardImport")

package com.tim.vpnprotocols.compose.openvpn

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tim.openvpn.OpenVPNConfig
import com.tim.vpnprotocols.compose.CONFIG_EDIT_SCREEN
import com.tim.vpnprotocols.compose.base.AppTopBar
import com.tim.vpnprotocols.compose.base.VpnScreen
import com.tim.vpnprotocols.compose.edit.ConfigEditViewModel
import com.tim.vpnprotocols.compose.navigation.VpnProtocol
import com.tim.vpnprotocols.compose.openvpn.controller.rememberOpenVPNController
import org.koin.androidx.compose.getViewModel
import org.koin.core.qualifier.named

/**
 *
 * @Author: Timur Hojatov
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenVPNScreen(
    navController: NavController,
) {
    val viewModel = getViewModel<ConfigEditViewModel<OpenVPNConfig>>(
        qualifier = named(OpenVPNConfig::class.java.name)
    )
    val configFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.saveConfigWithPath(it) }
    }

    var config by remember { mutableStateOf<OpenVPNConfig?>(null) }
    LaunchedEffect(Unit) {
        config = viewModel.getConfig()
    }
    Scaffold(
        topBar = {
            AppTopBar(
                title = "OpenVPN",
                actions = {
                    IconButton(
                        onClick = {
                            configFilePicker.launch(ovpnMimeTypes)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Import config"
                        )
                    }
                }
            ) {
                navController.popBackStack()
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        route = "$CONFIG_EDIT_SCREEN/${VpnProtocol.OPENVPN}"
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
                OpenVPNEmptyScreen(configFilePicker)
            } else {
                val openVPNController = rememberOpenVPNController(config = config!!)
                VpnScreen(vpnController = openVPNController)
            }
        }
    }
}

@Composable
fun OpenVPNEmptyScreen(
    configFilePicker: ManagedActivityResultLauncher<Array<String>, Uri?>
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Config not found, configure params in settings")
            Button(
                onClick = {
                    configFilePicker.launch(ovpnMimeTypes)
                }
            ) {
                Text(text = "Import config")
            }
        }
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
