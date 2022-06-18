package com.tim.openvpn.core

internal object NativeUtils {
    @JvmStatic
    val nativeAPI: String
        get() = jniAPI

    private val jniAPI: String
        external get

    init {
        System.loadLibrary("ovpnutil")
    }
}
