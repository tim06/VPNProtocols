package com.tim.shadowsocksr.log

import android.util.Log
import com.tim.shadowsocksr.BuildConfig

object ShadowsocksRLogger {
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg)
        }
    }
}