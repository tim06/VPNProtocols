package com.tim.openvpn.commandprocessors

import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.VpnStatus

/**
 * @Author: Тимур Ходжатов
 */
class StateMessageProcessor(
    private val stateListener: (ConnectionState) -> Unit
) : CommandProcessor {

    override val command: String = STATE

    override fun process(argument: String?) {
        argument?.split(",", limit = 3)
            ?.firstOrNull { messagePart ->
                messagePart.all { it.isDigit().not() }
            }?.let { state ->
                VpnStatus.log("OpenVPN State: $state")
                stateListener.invoke(getLevel(state))
            }
    }

    private fun getLevel(state: String): ConnectionState {
        val noReplyYet = arrayOf("CONNECTING", "WAIT", "RECONNECTING", "RESOLVE", "TCP_CONNECT")
        val reply = arrayOf("AUTH", "GET_CONFIG", "ASSIGN_IP", "ADD_ROUTES", "AUTH_PENDING")
        val connected = arrayOf("CONNECTED")
        val notConnected = arrayOf("DISCONNECTED", "EXITING")

        return if (noReplyYet.contains(state)) {
            ConnectionState.CONNECTING
        } else if (reply.contains(state)) {
            ConnectionState.CONNECTING
        } else if (connected.contains(state)) {
            ConnectionState.CONNECTED
        } else if (notConnected.contains(state)) {
            ConnectionState.DISCONNECTED
        } else {
            ConnectionState.READYFORCONNECT
        }
    }

    private companion object {
        private const val STATE = "STATE"
    }
}