package com.tim.vpnprotocols.xrayNg.parser

import android.content.Intent
import com.tim.vpnprotocols.xrayNg.XRayNgService.Companion.CONFIGURATION_DOMAIN_KEY
import com.tim.vpnprotocols.xrayNg.XRayNgService.Companion.CONFIGURATION_KEY

internal fun Intent.parseConfiguration(): String? {
    return getStringExtra(CONFIGURATION_KEY)
}

internal fun Intent.parseDomainName(): String? {
    return getStringExtra(CONFIGURATION_DOMAIN_KEY)
}