package com.tim.openvpn

import android.util.Log

internal object VpnStatus {

    internal fun log(message: String?, additionalInfo: String? = null) {
        if (BuildConfig.DEBUG) {
            Log.d("VpnStatusLog", "$message / $additionalInfo")
        }
    }
}
