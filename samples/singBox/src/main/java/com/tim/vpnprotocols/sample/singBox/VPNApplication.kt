package com.tim.vpnprotocols.sample.singBox

import android.app.Application
import go.Seq

class VPNApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Seq.setContext(this)
    }
}
