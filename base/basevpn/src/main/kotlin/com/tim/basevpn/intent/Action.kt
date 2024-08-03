package com.tim.basevpn.intent

import android.content.Intent
import com.tim.basevpn.singleProcess.ProtocolsVpnService.Companion.ACTION_KEY
import com.tim.basevpn.singleProcess.ProtocolsVpnService.Companion.ACTION_MEASURE_KEY
import com.tim.basevpn.singleProcess.ProtocolsVpnService.Companion.ACTION_START_KEY
import com.tim.basevpn.singleProcess.ProtocolsVpnService.Companion.ACTION_STOP_KEY

internal enum class Action {
    START,
    STOP,
    MEASURE
}

internal fun Intent.actionFromIntent(): Action {
    return when (getStringExtra(ACTION_KEY).orEmpty()) {
        ACTION_START_KEY -> Action.START
        ACTION_STOP_KEY -> Action.STOP
        ACTION_MEASURE_KEY -> Action.MEASURE
        else -> Action.STOP
    }
}