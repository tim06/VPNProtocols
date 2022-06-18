package com.tim.openvpn.command

import com.tim.openvpn.thread.OpenVpnManagementThread
import com.tim.openvpn.command.sender.managementCommand

/**
 * @Author: Timur Hojatov
 */

internal fun OpenVpnManagementThread.processProxyMessage() {
    managementCommand("proxy NONE\n")
}
