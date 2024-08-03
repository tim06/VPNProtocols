package com.tim.vpnprotocols.xrayNg

import android.content.Context
import android.provider.Settings
import android.util.Base64

private const val DIR_ASSETS = "assets"

internal fun Context?.userAssetPath(): String {
    if (this == null) return ""
    val extDir = getExternalFilesDir(DIR_ASSETS)
        ?: return getDir(DIR_ASSETS, 0).absolutePath
    return extDir.absolutePath
}

internal fun getDeviceIdForXUDPBaseKey(): String {
    val androidId = Settings.Secure.ANDROID_ID.toByteArray(Charsets.UTF_8)
    return Base64.encodeToString(androidId.copyOf(32), Base64.NO_PADDING.or(Base64.URL_SAFE))
}