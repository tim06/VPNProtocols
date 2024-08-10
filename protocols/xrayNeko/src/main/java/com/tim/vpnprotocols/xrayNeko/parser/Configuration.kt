package com.tim.vpnprotocols.xrayNeko.parser

import android.content.Intent
import com.tim.vpnprotocols.xrayNeko.XRayNekoService.Companion.CONFIGURATION_KEY
import com.tim.vpnprotocols.xrayNeko.XRayNekoService.Companion.NAIVE_CONFIGURATION_KEY

internal fun Intent.parseConfiguration(): String? {
    return getStringExtra(CONFIGURATION_KEY)
}

internal fun Intent.parseNaiveConfiguration(): String? {
    return getStringExtra(NAIVE_CONFIGURATION_KEY)
}