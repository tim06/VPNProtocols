package com.tim.openvpn.command.needOk

import com.tim.openvpn.thread.OpenVpnManagementThread
import com.tim.openvpn.VpnStatus
import com.tim.openvpn.thread.OpenVpnManagementThread.Companion.DNSDOMAIN
import com.tim.openvpn.thread.OpenVpnManagementThread.Companion.DNSSERVER
import com.tim.openvpn.thread.OpenVpnManagementThread.Companion.ROUTE
import com.tim.openvpn.command.PROTECTFD
import com.tim.openvpn.command.processProtectFdMessage
import com.tim.openvpn.command.sender.managementCommand

/**
 * @Author: Timur Hojatov
 */

internal fun OpenVpnManagementThread.processNeedOkMessage(argument: String) {
    val p1 = argument.indexOf('\'')
    val p2 = argument.indexOf('\'', p1 + 1)

    val needed = argument.substring(p1 + 1, p2)
    val extra = argument.split(":", limit = 2).toTypedArray()[1]

    var status = "ok"

    when (needed) {
        DNSSERVER,
        DNSDOMAIN,
        ROUTE -> {
            /*no action*/
        }
        PROTECTFD -> processProtectFdMessage()
        IFCONFIG -> processIfConfigMessage(extra)
        PERSIST_TUN_ACTION -> {
            status = OPEN_BEFORE_CLOSE
        }
        OPENTUN -> {
            status = processOpenTunMessage(needed) ?: return
        }
        else -> {
            VpnStatus.log("Unknown needok command $argument")
            return
        }
    }
    managementCommand("needok $needed $status\n")
}
