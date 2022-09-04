package com.tim.openvpn.commandprocessors.needok

import android.net.LocalSocket
import com.tim.openvpn.utils.sendMessage

/**
 * @Author: Тимур Ходжатов
 */
class RouteNeedokCommandProcessor(
    private val socket: LocalSocket
) : NeedokCommandProcessor {

    override val command: String = ROUTE

    override fun process(argument: String?): String? {
        socket.sendMessage("needok $command ok\n")
        return null
    }

    private companion object {
        private const val ROUTE = "ROUTE"
    }
}