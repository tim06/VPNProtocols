package com.tim.openvpn

import com.tim.openvpn.configuration.OpenVPNConfig

/**
 * @Author: Тимур Ходжатов
 */
object OpenVPNConfigParser {

    fun parse(config: String): OpenVPNConfig {
        return configFromLines(config.lines())
    }

    fun configFromLines(lines: List<String>): OpenVPNConfig {
        var config = OpenVPNConfig(type = "tcp-client")
        lines.forEach { line ->
            when {
                // host & port
                line.startsWith("remote ") -> {
                    val trimmed = line.trim().filterNot { it.isLetter() }.trim().split(" ")
                    val host = trimmed.first()
                    val port = trimmed.last()
                    config = config.copy(host = host, port = port.toIntOrNull())
                }
                // Cipher
                line.startsWith("cipher") -> {
                    val cipher = line.takeLastWhile { it != Char(EMPTY_SPACE_CHAR_CODE) }
                    config = config.copy(cipher = cipher)
                }
                // Auth
                line.startsWith("auth") -> {
                    val cipher = line.takeLastWhile { it != Char(EMPTY_SPACE_CHAR_CODE) }
                    config = config.copy(auth = cipher)
                }
                else -> {
                    VpnStatus.log("ConfigParser", "Unknown config param: $line")
                }
            }
        }

        // ca
        val ca = lines.linesByKey("<ca>")
        config = config.copy(ca = ca)

        // cert
        val cert = lines.linesByKey("<cert>")
        config = config.copy(cert = cert)

        // key
        val key = lines.linesByKey("<key>")
        config = config.copy(key = key)

        // tls-crypt
        val tlsCrypt = lines.linesByKey("<tls-crypt>")
        config = config.copy(tlsCrypt = tlsCrypt)
        return config
    }

    private fun List<String>.linesByKey(key: String): String {
        val startIndexOfKey = indexOfFirst { line ->
            line.startsWith(key)
        }
        val endIndexOfKey = indexOfFirst { line ->
            line.startsWith(key.replace("<", "</"))
        }
        return toList().slice(startIndexOfKey..endIndexOfKey).joinToString("\n")
    }

    private const val EMPTY_SPACE_CHAR_CODE = 32
}
