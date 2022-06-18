package com.tim.vpnprotocols

import android.app.Application
import timber.log.Timber

/**
 * @Author: Timur Hojatov
 */
class VPNApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
