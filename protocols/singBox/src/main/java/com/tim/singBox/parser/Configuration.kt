package com.tim.singBox.parser

import android.content.Intent
import com.tim.singBox.service.VPNService.Companion.CONFIGURATION_KEY

internal fun Intent.parseConfiguration(): String? {
    return getStringExtra(CONFIGURATION_KEY)
}