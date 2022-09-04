package com.tim.openvpn.commandprocessors

import android.net.LocalSocket
import com.tim.openvpn.utils.sendMessage

/**
 * @Author: Тимур Ходжатов
 */
class HoldMessageProcessor(
    private val socket: LocalSocket
) : CommandProcessor {

    override val command: String = HOLD

    override fun process(argument: String?) {
        socket.sendMessage("hold release\n")
        socket.sendMessage("bytecount 2\n")
        socket.sendMessage("state on\n")
    }

    private companion object {
        private const val HOLD = "HOLD"
    }
}