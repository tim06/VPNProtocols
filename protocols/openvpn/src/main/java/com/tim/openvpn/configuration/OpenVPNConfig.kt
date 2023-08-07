package com.tim.openvpn.configuration

import com.tim.basevpn.configuration.IVpnConfiguration
import kotlinx.parcelize.Parcelize

@Parcelize
data class OpenVPNConfig(
    val name: String? = null,
    val host: String? = null,
    val port: Int? = null,
    val type: String? = null,
    val cipher: String? = null,
    val auth: String? = null,
    val ca: String? = null,
    val key: String? = null,
    val cert: String? = null,
    val tlsCrypt: String? = null,
    val configuration: String? = null
) : IVpnConfiguration {

    fun buildConfig(): String {
        return StringBuilder().apply {
            append("# Config for OpenVPN 3 C++\n")

            append("client\n")
            append("verb 4\n")
            append("connect-retry 2 300\n")
            append("connect-retry-max 3\n")
            append("resolv-retry 60\n")
            append("dev tun\n")

            append("remote $host $port $type\n")
            append("nobind\n")
            append("remote-cert-tls server\n")
            append("cipher $cipher\n")
            append("auth $auth\n")

            append("persist-tun\n")
            append("preresolve\n")

            append("${ca}\n")
            append("${key}\n")
            append("${cert}\n")
            append("${tlsCrypt}\n")
        }.toString()
    }
}
