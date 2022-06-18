package com.tim.openvpn.command

import com.tim.openvpn.thread.OpenVpnManagementThread
import com.tim.openvpn.command.sender.managementCommand

/**
 * @Author: Timur Hojatov
 */


internal fun OpenVpnManagementThread.processHoldMessage() {
    managementCommand("hold release\n")
    managementCommand("bytecount 2\n")
    managementCommand("state on\n")
}
