package com.tim.shadowsocksr.parser

import android.content.Intent
import android.os.Build
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.shadowsocksr.service.ShadowsocksRService.Companion.CONFIGURATION_KEY

internal fun Intent.parseConfiguration(): ShadowsocksRVpnConfig? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(CONFIGURATION_KEY, ShadowsocksRVpnConfig::class.java)
    } else {
        getParcelableExtra(CONFIGURATION_KEY)
    }
}