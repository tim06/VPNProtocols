package com.tim.openvpn.commandprocessors.needok

import android.net.LocalSocket
import com.tim.openvpn.utils.sendMessage

/**
 * @Author: Тимур Ходжатов
 */
class DnsDomainNeedokCommandProcessor(
    private val socket: LocalSocket
) : NeedokCommandProcessor {

    override val command: String = DNSDOMAIN

    override fun process(argument: String?): String? {
        socket.sendMessage("needok $command ok\n")
        return null
    }

    private companion object {
        private const val DNSDOMAIN = "DNSDOMAIN"
    }
}