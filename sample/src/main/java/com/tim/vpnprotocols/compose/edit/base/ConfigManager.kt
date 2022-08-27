package com.tim.vpnprotocols.compose.edit.base

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * @Author: Тимур Ходжатов
 */
interface ConfigManager<T: Parcelable> {

    val rows: SnapshotStateList<ConfigItem>

    fun updateRow(key: String, newValue: String)

    suspend fun getConfig(): T?

    fun saveConfig()

    fun saveConfigWithPath(uri: Uri)
}

