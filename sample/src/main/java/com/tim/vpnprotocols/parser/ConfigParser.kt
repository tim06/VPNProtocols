package com.tim.vpnprotocols.parser

import android.net.Uri
import android.os.Parcelable

/**
 * @Author: Тимур Ходжатов
 */
interface ConfigParser<T: Parcelable> {
    fun parseConfig(uri: Uri): T?
}
