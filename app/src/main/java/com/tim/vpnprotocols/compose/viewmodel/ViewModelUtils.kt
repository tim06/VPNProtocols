package com.tim.vpnprotocols.compose.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tim.vpnprotocols.compose.edit.ConfigViewModel
import com.tim.vpnprotocols.compose.edit.store.OpenVPNStoreImpl
import com.tim.vpnprotocols.compose.edit.store.ShadowsocksrStoreImpl
import com.tim.vpnprotocols.storage.dataStore

/**
 * @Author: Тимур Ходжатов
 */
val DATA_STORE_KEY = object : CreationExtras.Key<DataStore<Preferences>> {}

val configFetcherViewModelFactory = viewModelFactory {
    initializer {
        val dataStore = checkNotNull(get(DATA_STORE_KEY))
        ConfigFetcherViewModel(dataStore)
    }
}

val openvpnViewModelFactory = viewModelFactory {
    initializer {
        val dataStore = checkNotNull(get(DATA_STORE_KEY))
        val config = OpenVPNStoreImpl(dataStore)
        ConfigViewModel(config)
    }
}

val shadowsocksrViewModelFactory = viewModelFactory {
    initializer {
        val dataStore = checkNotNull(get(DATA_STORE_KEY))
        val config = ShadowsocksrStoreImpl(dataStore)
        ConfigViewModel(config)
    }
}

fun getCreationExtras(context: Context) = MutableCreationExtras().apply {
    set(DATA_STORE_KEY, context.dataStore)
}
