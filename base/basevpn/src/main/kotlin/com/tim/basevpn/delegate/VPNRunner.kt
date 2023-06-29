package com.tim.basevpn.delegate

import android.os.Parcelable

/**
 * @Author: Timur Hojatov
 */
interface VPNRunner {
    fun <T: Parcelable> start(
        config: T,
        notificationClassName: String? = null,
        allowedApps: Set<String> = emptySet()
    )
    fun stop()
}
