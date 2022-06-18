package com.tim.shadowsocksr

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShadowsocksRVpnConfig(
    val name: String = "ShadowsocksR Config",
    var host: String = "",
    val localPort: Int = 1080,
    val remotePort: Int = 443,
    val password: String = "",
    val protocol: String = "origin",
    val protocolParam: String = "",
    val obfs: String = "http_simple",
    val obfsParam: String = "",
    val method: String = "chacha20",
    val dnsAddress: String = "8.8.8.8",
    val dnsPort: String = "53",
) : Parcelable
