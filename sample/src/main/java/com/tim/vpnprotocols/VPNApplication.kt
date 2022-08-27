package com.tim.vpnprotocols

import android.app.Application
import com.tim.vpnprotocols.di.initKoin

/**
 * @Author: Timur Hojatov
 */
class VPNApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
