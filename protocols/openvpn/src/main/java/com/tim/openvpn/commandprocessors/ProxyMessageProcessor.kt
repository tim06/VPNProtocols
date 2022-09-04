package com.tim.openvpn.commandprocessors

import android.net.LocalSocket
import com.tim.openvpn.utils.sendMessage

/**
 * @Author: Тимур Ходжатов
 */
class ProxyMessageProcessor(
    private val socket: LocalSocket
) : CommandProcessor {

    override val command: String = PROXY

    override fun process(argument: String?) {
        socket.sendMessage("proxy NONE\n")
    }

    private companion object {
        private const val PROXY = "PROXY"
    }
}