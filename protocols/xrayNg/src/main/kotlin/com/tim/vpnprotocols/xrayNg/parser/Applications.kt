package com.tim.vpnprotocols.xrayNg.parser

import android.content.Intent
import com.tim.vpnprotocols.xrayNg.XRayNgService.Companion.ALLOWED_APPS_KEY

fun Intent?.parseAllowedApplications(): Array<String>? {
    return this?.getStringArrayExtra(ALLOWED_APPS_KEY)
}