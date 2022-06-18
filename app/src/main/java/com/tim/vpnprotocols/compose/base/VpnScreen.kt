package com.tim.vpnprotocols.compose.base

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.tim.basevpn.permission.VpnActivityResultContract
import com.tim.basevpn.permission.isVpnPermissionGranted
import com.tim.basevpn.state.ConnectionState

/**
 * @Author: Timur Hojatov
 */
@Composable
fun VpnScreen(
    modifier: Modifier = Modifier,
    vpnController: BaseController
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        VpnActivityResultContract()
    ) { isGranted: Boolean ->
        if (isGranted) {
            vpnController.startVpn()
        }
    }
    val state by vpnController.connectionState.collectAsState()
    val color by animateColorAsState(targetValue = colorByState(state))
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = color)
    ) {
        Button(
            modifier = Modifier.align(Alignment.Center),
            enabled = isButtonEnabledByState(state),
            onClick = {
                if (isStartByState(state)) {
                    if (context.isVpnPermissionGranted()) {
                        vpnController.startVpn()
                    } else {
                        permissionLauncher.launch()
                    }
                } else {
                    vpnController.stopVpn()
                }
            }
        ) {
            Text(text = buttonTextByState(state))
        }
    }
}

fun colorByState(state: ConnectionState): Color = when (state) {
    ConnectionState.IDLE,
    ConnectionState.READYFORCONNECT -> Color.Black
    ConnectionState.CONNECTED -> Color.Green
    ConnectionState.DISCONNECTED -> Color.Red
    else -> Color.Black
}

fun buttonTextByState(state: ConnectionState): String = when (state) {
    ConnectionState.DISCONNECTED,
    ConnectionState.IDLE,
    ConnectionState.READYFORCONNECT -> "Start"
    else -> "Disconnect"
}

fun isButtonEnabledByState(state: ConnectionState): Boolean = when (state) {
    ConnectionState.READYFORCONNECT,
    ConnectionState.CONNECTED,
    ConnectionState.DISCONNECTED,
    ConnectionState.IDLE -> true
    else -> false
}

fun isStartByState(state: ConnectionState): Boolean = when (state) {
    ConnectionState.READYFORCONNECT,
    ConnectionState.DISCONNECTED,
    ConnectionState.IDLE -> true
    else -> false
}
