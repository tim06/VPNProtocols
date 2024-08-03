package com.tim.vpnprotocols.util

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.storage.ProtocolsStorage
import com.tim.state.getStateStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

typealias StateListener = (ConnectionState) -> Unit

fun Fragment.initStateListener(stateKey: String, listener: StateListener) {
    ProtocolsStorage.init(getStateStorage(requireContext()))
    lifecycleScope.launch {
        repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
            ProtocolsStorage.getCurrentStateFlow(stringPreferencesKey(stateKey))
                .flowOn(Dispatchers.IO)
                .collect(listener::invoke)
        }
    }
}