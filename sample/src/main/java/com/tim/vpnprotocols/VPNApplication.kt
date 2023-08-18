package com.tim.vpnprotocols

import android.app.Application
import com.tim.vpnprotocols.di.initKoin
import io.nekohasekai.sagernet.SagerNet

/**
 * @Author: Timur Hojatov
 */
class VPNApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()

        // TODO xtls-r wait refactor
        SagerNet().onCreate(this)
    }
}
