package com.tim.openvpn.commandprocessors.needok

import android.net.LocalSocket
import com.tim.openvpn.utils.sendMessage

/**
 * @Author: Тимур Ходжатов
 */
class PersistTunNeedokCommandProcessor(
    private val socket: LocalSocket
) : NeedokCommandProcessor {

    override val command: String = PERSIST_TUN_ACTION

    override fun process(argument: String?): String? {
        socket.sendMessage("needok $command $OPEN_BEFORE_CLOSE\n")
        return null
    }

    private companion object {
        private const val PERSIST_TUN_ACTION = "PERSIST_TUN_ACTION"
        private const val OPEN_BEFORE_CLOSE = "OPEN_BEFORE_CLOSE"
    }
}