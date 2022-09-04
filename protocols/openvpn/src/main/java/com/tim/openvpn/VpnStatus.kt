package com.tim.openvpn

import android.util.Log

internal object VpnStatus {

    internal fun log(message: String?, additionalInfo: String? = null) {
        if (additionalInfo == null) {
            Log.d("VpnStatusLog", "$message")
        } else {
            Log.d("VpnStatusLog", "$message / $additionalInfo")
        }
    }
}
