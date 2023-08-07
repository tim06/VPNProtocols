package com.tim.vpnprotocols.di

import com.tim.openvpn.configuration.OpenVPNConfig
import com.tim.vpnprotocols.compose.edit.ConfigEditViewModel
import com.tim.vpnprotocols.compose.edit.row.ConfigRows
import com.tim.vpnprotocols.compose.edit.row.OpenVPNRowsImpl
import com.tim.vpnprotocols.compose.edit.store.ConfigStorage
import com.tim.vpnprotocols.compose.edit.store.ConfigStorageImpl
import com.tim.vpnprotocols.parser.ConfigParser
import com.tim.vpnprotocols.parser.OpenVPNConfigParser
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * @Author: Тимур Ходжатов
 */
val openVPNModule = module {
    val openvpn = OpenVPNConfig::class.java.name
    single<ConfigStorage<OpenVPNConfig>>(named(openvpn)) {
        ConfigStorageImpl(
            dataStore = get(),
            clazz = OpenVPNConfig::class.java
        )
    }
    single<ConfigRows<OpenVPNConfig>>(named(openvpn)) {
        OpenVPNRowsImpl()
    }
    single<ConfigParser<OpenVPNConfig>>(named(openvpn)) {
        OpenVPNConfigParser(contentResolver = get())
    }
    viewModel(named(openvpn)) {
        ConfigEditViewModel<OpenVPNConfig>(
            configStorage = get(named(openvpn)),
            configRows = get(named(openvpn)),
            configParser = get(named(openvpn))
        )
    }
}
