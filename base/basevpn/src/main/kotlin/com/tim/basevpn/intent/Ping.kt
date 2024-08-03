package com.tim.basevpn.intent

import android.content.Intent
import com.tim.basevpn.singleProcess.ProtocolsVpnService.Companion.PING_URL_KEY

fun Intent?.parsePingUrl(): String? {
    return this?.getStringExtra(PING_URL_KEY)
}