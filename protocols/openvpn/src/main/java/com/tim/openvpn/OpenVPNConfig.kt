package com.tim.openvpn

import android.os.Parcelable
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
    val tlsCrypt: String? = null
) : Parcelable {

    fun buildConfig(socketCacheDir: String): String {
        return StringBuilder().apply {
            append("# Start config\n")
            append("management $socketCacheDir/mgmtsocket unix\n")
            append("management-client\n")
            append("management-query-passwords\n")
            append("management-hold\n\n")

            append("machine-readable-output\n")
            append("allow-recursive-routing\n")
            append("ifconfig-nowarn\n")

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
            // # Use system proxy setting
            append("management-query-proxy\n")

            append("${ca}\n")
            append("${key}\n")
            append("${cert}\n")
            append("${tlsCrypt}\n")
        }.toString()
    }
}
