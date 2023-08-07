package com.tim.openvpn.log

import android.util.Log
import com.tim.openvpn.BuildConfig

object OpenVPNLogger {
    @JvmStatic
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
    }

    @JvmStatic
    fun e(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg)
        }
    }
}