package com.tim.basevpn.intent

import android.content.Intent
import com.tim.basevpn.singleProcess.ProtocolsVpnService.Companion.ALLOWED_APPS_KEY

fun Intent?.parseAllowedApplications(): Array<String>? {
    return this?.getStringArrayExtra(ALLOWED_APPS_KEY)
}