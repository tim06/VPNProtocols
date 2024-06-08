package com.tim.vpnprotocols.xrayNg.parser

import android.content.Intent
import com.tim.vpnprotocols.xrayNg.XRayNgService.Companion.NOTIFICATION_CLASS_KEY

fun Intent?.parseNotificationClass(): String? {
    return this?.getStringExtra(NOTIFICATION_CLASS_KEY)
}