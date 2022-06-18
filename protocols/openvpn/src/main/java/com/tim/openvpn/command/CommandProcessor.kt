package com.tim.openvpn.command

import com.tim.openvpn.VpnStatus
import com.tim.openvpn.thread.OpenVpnManagementThread.Companion.HOLD
import com.tim.openvpn.thread.OpenVpnManagementThread.Companion.INFO
import com.tim.openvpn.thread.OpenVpnManagementThread.Companion.LOG
import com.tim.openvpn.thread.OpenVpnManagementThread.Companion.NEED_OK
import com.tim.openvpn.thread.OpenVpnManagementThread.Companion.PROXY
import com.tim.openvpn.thread.OpenVpnManagementThread.Companion.STATE
import com.tim.openvpn.thread.OpenVpnManagementThread.Companion.SUCCESS_START
import com.tim.openvpn.command.needOk.processNeedOkMessage
import com.tim.openvpn.thread.OpenVpnManagementThread

/**
 * @Author: Timur Hojatov
 */
internal fun OpenVpnManagementThread.processMessage(command: String) {
    if (command.startsWith(">") && command.contains(":")) {
        val parts = command.split(":", limit = 2).toTypedArray()
        val cmd = parts[0].substring(1)
        val argument = parts[1]
        when (cmd) {
            INFO -> return
            HOLD -> processHoldMessage()
            NEED_OK -> processNeedOkMessage(argument)
            STATE -> if (!isShuttingDown) processStateMessage(argument)
            PROXY -> processProxyMessage()
            LOG -> processLogMessage(argument)
            else -> {
                VpnStatus.log("MGMT: Got unrecognized command: $command")
            }
        }
    } else if (command.startsWith(SUCCESS_START)) {
        return
    } else if (command.startsWith(PROTECTFD_START)) {
        processProtectFdMessage()
    } else {
        VpnStatus.log("MGMT: Got unrecognized line from management:$command")
    }
}
