package com.tim.basevpn.state

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tim.basevpn.storage.ProtocolsStorage

class ConnectionStateHolder(dataStore: DataStore<Preferences>, private val key: Preferences.Key<String>) {

    init {
        ProtocolsStorage.init(dataStore)
    }

    suspend fun setCurrentState(connectionState: ConnectionState) {
        ProtocolsStorage.setCurrentState(connectionState, key)
    }

    suspend fun getCurrentState(): ConnectionState {
        return ProtocolsStorage.getCurrentState(key)
    }
}