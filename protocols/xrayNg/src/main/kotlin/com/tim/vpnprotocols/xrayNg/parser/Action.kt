package com.tim.vpnprotocols.xrayNg.parser

import android.content.Intent
import com.tim.vpnprotocols.xrayNg.XRayNgService.Companion.ACTION_KEY
import com.tim.vpnprotocols.xrayNg.XRayNgService.Companion.ACTION_START_KEY
import com.tim.vpnprotocols.xrayNg.XRayNgService.Companion.ACTION_STOP_KEY

internal enum class Action {
    START,
    STOP
}

internal fun Intent.actionFromIntent(): Action {
    return when (getStringExtra(ACTION_KEY).orEmpty()) {
        ACTION_START_KEY -> {
            Action.START
        }

        ACTION_STOP_KEY -> {
            Action.STOP
        }

        else -> Action.STOP
    }
}