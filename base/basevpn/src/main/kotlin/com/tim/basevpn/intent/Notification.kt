package com.tim.basevpn.intent

import android.content.Intent
import com.tim.basevpn.singleProcess.ProtocolsVpnService.Companion.NOTIFICATION_CLASS_KEY

fun Intent?.parseNotificationClass(): String? {
    return this?.getStringExtra(NOTIFICATION_CLASS_KEY)
}