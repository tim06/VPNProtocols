package com.tim.openvpn.commandprocessors.needok

import android.net.LocalSocket
import com.tim.openvpn.utils.sendMessage

/**
 * @Author: Тимур Ходжатов
 */
class DnsServerNeedokCommandProcessor(
    private val socket: LocalSocket
) : NeedokCommandProcessor {

    override val command: String = DNSSERVER

    override fun process(argument: String?): String? {
        socket.sendMessage("needok $command ok\n")
        return null
    }

    private companion object {
        private const val DNSSERVER = "DNSSERVER"
    }
}