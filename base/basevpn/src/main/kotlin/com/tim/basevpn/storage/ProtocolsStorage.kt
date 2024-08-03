package com.tim.basevpn.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.tim.basevpn.state.ConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

object ProtocolsStorage {

    private var storage: DataStore<Preferences>? = null

    fun init(dataStore: DataStore<Preferences>) {
        if (storage == null) {
            storage = dataStore
        }
    }

    suspend fun setCurrentState(
        newState: ConnectionState,
        preferencesKey: Preferences.Key<String>
    ) = withContext(Dispatchers.IO) {
        requireNotNull(storage) { "Must call StateHolder.init() first!" }
        storage?.edit { preferences ->
            preferences.set(preferencesKey, newState.name)
        }
    }

    suspend fun getCurrentState(preferencesKey: Preferences.Key<String>): ConnectionState =
        withContext(Dispatchers.IO) {
            requireNotNull(storage) { "Must call StateHolder.init() first!" }
            storage?.data?.firstOrNull()?.get(preferencesKey).toConnectionState()
        }

    fun getCurrentStateFlow(preferencesKey: Preferences.Key<String>): Flow<ConnectionState> {
        requireNotNull(storage) { "Must call StateHolder.init() first!" }
        return storage?.data?.map { preferences ->
            preferences.get(preferencesKey).toConnectionState()
        } ?: emptyFlow()
    }

    suspend fun setMeasuredResultPing(
        preferencesKey: Preferences.Key<Long>,
        ping: Long
    ) = withContext(Dispatchers.IO) {
        requireNotNull(storage) { "Must call StateHolder.init() first!" }
        storage?.edit { preferences ->
            preferences.set(preferencesKey, ping)
        }
    }

    suspend fun getMeasuredResultPing(preferencesKey: Preferences.Key<Long>): Long =
        withContext(Dispatchers.IO) {
            requireNotNull(storage) { "Must call StateHolder.init() first!" }
            val ping = storage?.data?.firstOrNull()?.get(preferencesKey) ?: -1
            setMeasuredResultPing(preferencesKey, -1)
            ping
        }

    private fun String?.toConnectionState(): ConnectionState {
        return ConnectionState.valueOf(this ?: ConnectionState.DISCONNECTED.name)
    }
}

