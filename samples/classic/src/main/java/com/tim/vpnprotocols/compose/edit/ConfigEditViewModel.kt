package com.tim.vpnprotocols.compose.edit

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tim.vpnprotocols.compose.edit.base.ConfigItem
import com.tim.vpnprotocols.compose.edit.base.ConfigManager
import com.tim.vpnprotocols.compose.edit.row.ConfigRows
import com.tim.vpnprotocols.compose.edit.store.ConfigStorage
import com.tim.vpnprotocols.parser.ConfigParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @Author: Тимур Ходжатов
 */
class ConfigEditViewModel<T : Parcelable>(
    private val configStorage: ConfigStorage<T>,
    private val configRows: ConfigRows<T>,
    private val configParser: ConfigParser<T>
) : ViewModel(), ConfigManager<T> {

    override val rows: SnapshotStateList<ConfigItem> = mutableStateListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val config = configStorage.getConfig()
            val configRows = configRows.getRowsForConfig(config)
            withContext(Dispatchers.Main) {
                rows.addAll(configRows)
            }
        }
    }

    // TODO do this better
    override fun updateRow(key: String, newValue: String) {
        val initialList = rows
        val index = initialList.indexOfFirst { it.key == key }
        val resultList = initialList.toMutableList()
        val item = resultList.removeAt(index)
        resultList.add(index, item.copy(value = newValue))
        rows.clear()
        rows.addAll(resultList)
    }

    override suspend fun getConfig(): T? = configStorage.getConfig()

    override fun saveConfig() {
        configRows.getConfigFromRows(rows)?.let { config ->
            viewModelScope.launch(Dispatchers.IO) {
                configStorage.saveConfig(config)
            }
        }
    }

    override fun saveConfigWithPath(uri: Uri) {
        configParser.parseConfig(uri)?.let { config ->
            viewModelScope.launch(Dispatchers.IO) {
                configStorage.saveConfig(config)
            }
        }
    }
}

