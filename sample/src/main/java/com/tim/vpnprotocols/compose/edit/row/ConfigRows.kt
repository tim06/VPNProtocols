package com.tim.vpnprotocols.compose.edit.row

import android.os.Parcelable
import com.tim.vpnprotocols.compose.edit.base.ConfigItem

/**
 * @Author: Тимур Ходжатов
 */
interface ConfigRows<T : Parcelable> {
    fun getRowsForConfig(config: T?): List<ConfigItem>
    fun getConfigFromRows(rows: List<ConfigItem>): T?
}
