package com.tim.openvpn.command

import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.thread.OpenVpnManagementThread
import com.tim.openvpn.VpnStatus

/**
 * @Author: Timur Hojatov
 */


internal fun OpenVpnManagementThread.processStateMessage(argument: String) {
    val args = argument.split(",", limit = 3)
    val currentstate = args[1]
    if (args[2] == ",,") {
        VpnStatus.log(currentstate, "")
    } else {
        VpnStatus.log(currentstate, args[2])
    }
    stateListener.invoke(getLevel(currentstate))
}

internal fun getLevel(state: String): ConnectionState {
    val noreplyet = arrayOf("CONNECTING", "WAIT", "RECONNECTING", "RESOLVE", "TCP_CONNECT")
    val reply = arrayOf("AUTH", "GET_CONFIG", "ASSIGN_IP", "ADD_ROUTES", "AUTH_PENDING")
    val connected = arrayOf("CONNECTED")
    val notconnected = arrayOf("DISCONNECTED", "EXITING")

    return if (noreplyet.contains(state)) {
        ConnectionState.CONNECTING
    } else if (reply.contains(state)) {
        ConnectionState.CONNECTING
    } else if (connected.contains(state)) {
        ConnectionState.CONNECTED
    } else if (notconnected.contains(state)) {
        ConnectionState.DISCONNECTED
    } else {
        ConnectionState.READYFORCONNECT
    }
}
