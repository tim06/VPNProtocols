package com.tim.openvpn.command.parser

import com.tim.openvpn.command.processMessage
import com.tim.openvpn.thread.OpenVpnManagementThread

/**
 * @Author: Timur Hojatov
 */
internal fun OpenVpnManagementThread.processInput(pendingInput: String): String {
    var pendingInput1 = pendingInput
    while (pendingInput1.contains("\n")) {
        val tokens = pendingInput1.split(Regex("\\r?\\n"), limit = 2)
        processMessage(tokens[0])
        pendingInput1 = if (tokens.size == 1) {
            // No second part, newline was at the end
            ""
        } else {
            tokens[1]
        }
    }
    return pendingInput1
}
