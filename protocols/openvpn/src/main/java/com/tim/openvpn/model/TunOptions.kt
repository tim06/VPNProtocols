package com.tim.openvpn.model

data class TunOptions(
    val localIp: CIDRIP,
    val mtu: Int = 0
)
