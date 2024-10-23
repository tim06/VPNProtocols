package com.tim.vpnprotocols.compose.edit.store

import android.os.Parcelable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tim.vpnprotocols.parcelable.convertToObject
import com.tim.vpnprotocols.parcelable.convertToString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

/**
 * @Author: Тимур Ходжатов
 */
class ConfigStorageImpl<T : Parcelable>(
    private val dataStore: DataStore<Preferences>,
    private val clazz: Class<T>
) : ConfigStorage<T> {

    override fun getConfigFlow(): Flow<T?> = dataStore.data.map {
        it[stringPreferencesKey(clazz::class.java.name)]?.convertToObject(clazz)
    }

    override suspend fun getConfig(): T? = dataStore.data
        .firstOrNull()
        ?.get(stringPreferencesKey(clazz::class.java.name))
        ?.convertToObject(clazz)

    override suspend fun saveConfig(config: T) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(clazz::class.java.name)] =
                config.convertToString()
        }
    }
}
