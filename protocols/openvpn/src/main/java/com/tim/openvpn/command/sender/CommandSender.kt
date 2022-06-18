package com.tim.openvpn.command.sender

import com.tim.openvpn.thread.OpenVpnManagementThread

/**
 * Send [command] to lib
 *
 * @Author: Timur Hojatov
 */
internal fun OpenVpnManagementThread.managementCommand(command: String): Boolean = runCatching {
    socket?.outputStream?.apply {
        write(command.toByteArray())
        flush()
    }
    true
}.getOrDefault(false)
