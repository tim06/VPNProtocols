package com.tim.basevpn.delegate

import android.net.VpnService
import android.os.Parcelable
import androidx.lifecycle.LifecycleOwner
import com.tim.basevpn.state.ConnectionState

/**
 * @Author: Timur Hojatov
 */
inline fun <T : Parcelable, reified V: VpnService> LifecycleOwner.vpnDelegate(
    config: T,
    noinline stateListener: (ConnectionState) -> Unit
) = VpnConnectionServiceDelegate(
    lifecycleOwner = this,
    config = config,
    clazz = V::class.java,
    stateListener = stateListener
)
