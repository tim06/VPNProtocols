package com.tim.vpnprotocols.compose.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tim.vpnprotocols.compose.navigation.VpnProtocol
import com.tim.vpnprotocols.compose.viewmodel.getCreationExtras
import com.tim.vpnprotocols.compose.viewmodel.openvpnViewModelFactory
import com.tim.vpnprotocols.compose.viewmodel.shadowsocksrViewModelFactory

/**
 * @Author: Тимур Ходжатов
 */
@Composable
fun ConfigEditScreen(
    type: VpnProtocol,
    viewModel: ConfigViewModel = when (type) {
        VpnProtocol.OPENVPN -> {
            openvpnViewModelFactory.create(
                modelClass = ConfigViewModel::class.java,
                extras = getCreationExtras(LocalContext.current)
            )
        }
        VpnProtocol.SHADOWSOCKSR -> {
            shadowsocksrViewModelFactory.create(
                modelClass = ConfigViewModel::class.java,
                extras = getCreationExtras(LocalContext.current)
            )
        }
    }
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        viewModel.getRows().forEach { configItem ->
            item(key = configItem.key) {
                ConfigItem(
                    configItem.hint,
                    viewModel.getValueWithKey(configItem.key)
                        .collectAsState(initial = null).value
                ) { newEnteredValue ->
                    viewModel.updateConfigWithNewValue(configItem.key, newEnteredValue)
                }
            }
        }
    }
}

@Composable
fun ConfigItem(
    hint: String,
    value: String?,
    onValueChange: (String) -> Unit
) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = hint) },
        value = value.orEmpty(),
        onValueChange = onValueChange
    )
}
