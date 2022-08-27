package com.tim.vpnprotocols.di

import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.vpnprotocols.compose.edit.ConfigEditViewModel
import com.tim.vpnprotocols.compose.edit.row.ConfigRows
import com.tim.vpnprotocols.compose.edit.row.ShadowsocksrRowsImpl
import com.tim.vpnprotocols.compose.edit.store.ConfigStorage
import com.tim.vpnprotocols.compose.edit.store.ConfigStorageImpl
import com.tim.vpnprotocols.parser.ConfigParser
import com.tim.vpnprotocols.parser.ShadowsocksrConfigParser
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * @Author: Тимур Ходжатов
 */
val shadowsocksrModule = module {
    val shadowsocksr = ShadowsocksRVpnConfig::class.java.name
    single<ConfigStorage<ShadowsocksRVpnConfig>>(named(shadowsocksr)) {
        ConfigStorageImpl(
            dataStore = get(),
            clazz = ShadowsocksRVpnConfig::class.java
        )
    }
    single<ConfigRows<ShadowsocksRVpnConfig>>(named(shadowsocksr)) {
        ShadowsocksrRowsImpl()
    }
    single<ConfigParser<ShadowsocksRVpnConfig>>(named(shadowsocksr)) {
        ShadowsocksrConfigParser()
    }
    viewModel(named(shadowsocksr)) {
        ConfigEditViewModel<ShadowsocksRVpnConfig>(
            configStorage = get(named(shadowsocksr)),
            configRows = get(named(shadowsocksr)),
            configParser = get(named(shadowsocksr))
        )
    }
}
