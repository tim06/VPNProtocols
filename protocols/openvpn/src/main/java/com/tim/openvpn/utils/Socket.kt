package com.tim.openvpn.utils

import android.net.LocalSocket
import com.tim.openvpn.VpnStatus

/**
 * @Author: Тимур Ходжатов
 */
fun LocalSocket.sendMessage(message: String) {
    VpnStatus.log("OpenVPN Send message: $message")
    with(outputStream) {
        write(message.toByteArray())
        flush()
    }
}