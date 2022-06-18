package com.tim.basevpn.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * VPN connection states
 *
 * @Author: Timur Hojatov
 */
@Parcelize
enum class ConnectionState : Parcelable {
    READYFORCONNECT,
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    DISCONNECTING,
    IDLE
}
