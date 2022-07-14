package com.tim.basevpn.delegate

import android.net.VpnService
import android.os.Parcelable
import androidx.activity.ComponentActivity
import com.tim.basevpn.state.ConnectionState

/**
 * @Author: Timur Hojatov
 */
inline fun <T : Parcelable, reified V: VpnService> ComponentActivity.vpnDelegate(
    config: T,
    noinline stateListener: (ConnectionState) -> Unit
) = VpnConnectionServiceDelegate(
    activityResultRegistryOwner = this,
    config = config,
    clazz = V::class.java,
    stateListener = stateListener
)
