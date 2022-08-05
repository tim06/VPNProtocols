package com.tim.vpnprotocols.compose.viewmodel

import android.os.Parcelable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import com.tim.vpnprotocols.extension.convertToObject
import kotlinx.coroutines.flow.map

/**
 * @Author: Тимур Ходжатов
 */
class ConfigFetcherViewModel(
    val dataStore: DataStore<Preferences>
) : ViewModel() {

    inline fun <reified T : Parcelable> getConfig() = dataStore.data.map {
        it[stringPreferencesKey(T::class.java.name)]?.convertToObject<T>()
    }
}
