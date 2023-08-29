package com.tim.basevpn.configuration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VpnConfiguration<T : IVpnConfiguration>(
    val data: T,
    val allowedApps: Set<String>,
    val notificationClassName: String? = null,
    val timeToDisconnect: Long? = null
) : Parcelable