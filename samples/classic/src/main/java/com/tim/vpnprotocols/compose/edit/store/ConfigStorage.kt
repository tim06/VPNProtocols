package com.tim.vpnprotocols.compose.edit.store

import android.os.Parcelable
import kotlinx.coroutines.flow.Flow

/**
 * @Author: Тимур Ходжатов
 */
interface ConfigStorage<T : Parcelable> {
    fun getConfigFlow(): Flow<T?>

    suspend fun getConfig(): T?

    suspend fun saveConfig(config: T)
}
