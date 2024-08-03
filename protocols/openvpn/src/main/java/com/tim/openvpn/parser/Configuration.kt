package com.tim.openvpn.parser

import android.content.Intent
import android.os.Build
import com.tim.openvpn.configuration.OpenVPNConfig
import com.tim.openvpn.service.OpenVPNService.Companion.CONFIGURATION_KEY

internal fun Intent.parseConfiguration(): OpenVPNConfig? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(CONFIGURATION_KEY, OpenVPNConfig::class.java)
    } else {
        getParcelableExtra(CONFIGURATION_KEY)
    }
}