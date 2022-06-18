package com.tim.openvpn.model

/**
 * @Author: Timur Hojatov
 */
data class TunOptions(
    val localIp: CIDRIP,
    val mtu: Int = 0
)
