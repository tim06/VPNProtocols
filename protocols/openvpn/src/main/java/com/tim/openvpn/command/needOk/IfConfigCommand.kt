package com.tim.openvpn.command.needOk

import com.tim.openvpn.thread.OpenVpnManagementThread
import com.tim.openvpn.model.CIDRIP
import com.tim.openvpn.model.TunOptions

/**
 * @Author: Timur Hojatov
 */
internal const val IFCONFIG = "IFCONFIG"
private const val MODE_INDEX = 3

internal fun OpenVpnManagementThread.processIfConfigMessage(message: String) {
    val ifconfigParts = message.split(" ")
    val localIp = ifconfigParts[0]
    val networkMask = ifconfigParts[1]
    val mtu = ifconfigParts[2].toInt()
    val mode = ifconfigParts[MODE_INDEX]
    tunOptions = TunOptions(
        localIp = CIDRIP(ip = localIp, mask = networkMask, mode = mode),
        mtu = mtu
    )
}
