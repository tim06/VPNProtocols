package com.tim.openvpn.commandprocessors.needok

import android.net.LocalSocket
import com.tim.openvpn.model.CIDRIP
import com.tim.openvpn.model.TunOptions
import com.tim.openvpn.utils.sendMessage

/**
 * @Author: Тимур Ходжатов
 */
class IfConfigNeedokCommandProcessor(
    private val socket: LocalSocket,
    private val tunOptionsCallback: (TunOptions) -> Unit
) : NeedokCommandProcessor {

    override val command: String = IFCONFIG

    override fun process(argument: String?): String? {
        argument?.let {
            val ifconfigParts = argument.split(" ")
            val localIp = ifconfigParts[0]
            val networkMask = ifconfigParts[1]
            val mtu = ifconfigParts[2].toInt()
            val mode = ifconfigParts[MODE_INDEX]
            tunOptionsCallback.invoke(
                TunOptions(
                    localIp = CIDRIP(ip = localIp, mask = networkMask, mode = mode),
                    mtu = mtu
                )
            )
            socket.sendMessage("needok $command ok\n")
        }
        return null
    }

    private companion object {
        private const val IFCONFIG = "IFCONFIG"
        private const val MODE_INDEX = 3
    }
}