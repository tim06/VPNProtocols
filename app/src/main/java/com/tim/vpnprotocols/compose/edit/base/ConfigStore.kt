package com.tim.vpnprotocols.compose.edit.base

import android.os.Parcelable
import kotlinx.coroutines.flow.Flow

/**
 * @Author: Тимур Ходжатов
 */
interface ConfigStore {

    val config: Flow<Parcelable?>

    fun getRows(): List<ConfigItem>

    fun getValueWithKey(
        key: String
    ): Flow<String?>

    suspend fun updateConfig(
        key: String,
        newValue: String
    )
}
