@file:Suppress("WildcardImport")
package com.tim.vpnprotocols.compose.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tim.vpnprotocols.compose.base.AppTopBar
import com.tim.vpnprotocols.compose.base.BackPressedHandler
import com.tim.vpnprotocols.compose.edit.base.ConfigItem
import com.tim.vpnprotocols.compose.navigation.VpnProtocol
import com.tim.vpnprotocols.compose.viewmodel.getViewModel

/**
 * @Author: Тимур Ходжатов
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ConfigEditScreen(
    type: VpnProtocol,
    navController: NavController
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewModel = type.getViewModel()

    DisposableEffect(type) {
        onDispose {
            viewModel.saveConfig()
        }
    }
    val onBackAction = {
        viewModel.saveConfig()
        keyboardController?.hide()
        navController.popBackStack()
        Unit
    }
    Scaffold(
        topBar = {
            AppTopBar(title = "Config Edit", onBackPressed = onBackAction)
        }
    ) { paddingValues ->
        BackPressedHandler(onBackPressed = onBackAction)
        Box(modifier = Modifier.padding(paddingValues)) {
            Content(rows = viewModel.rows) { key, newValue ->
                viewModel.updateRow(key, newValue)
            }
        }
    }
}

@Composable
private fun Content(
    rows: Collection<ConfigItem>,
    onValueChange: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        rows.forEach { configItem ->
            item(key = configItem.key) {
                ConfigItem(
                    hint = configItem.hint,
                    value = configItem.value,
                ) { newEnteredValue ->
                    onValueChange.invoke(configItem.key, newEnteredValue)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigItem(
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
