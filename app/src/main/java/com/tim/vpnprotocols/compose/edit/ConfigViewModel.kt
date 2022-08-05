package com.tim.vpnprotocols.compose.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tim.vpnprotocols.compose.edit.base.ConfigStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @Author: Тимур Ходжатов
 */
class ConfigViewModel(
    private val configStore: ConfigStore
) : ViewModel(), ConfigStore by configStore {

    /**
     * Job for canceling previous work
     */
    var updateConfigJob: Job? = null

    fun updateConfigWithNewValue(key: String, newValue: String) {
        updateConfigJob?.cancel()
        updateConfigJob = viewModelScope.launch(Dispatchers.IO) {
            configStore.updateConfig(key, newValue)
        }
    }
}
