package com.tim.vpnprotocols.xrayNg.log

import android.util.Log

class Logger(private val tag: String) {
    fun d(msg: String) {
        Log.d(tag, msg)
    }
}
