package com.tim.vpnprotocols.compose.edit.row

import com.tim.openvpn.configuration.OpenVPNConfig
import com.tim.vpnprotocols.compose.edit.base.ConfigItem

/**
 * @Author: Тимур Ходжатов
 */
class OpenVPNRowsImpl : ConfigRows<OpenVPNConfig> {

    override fun getRowsForConfig(config: OpenVPNConfig?): List<ConfigItem> = listOf(
        ConfigItem(KEY_NAME, "Name", config?.name.orEmpty()),
        ConfigItem(KEY_HOST, "Host", config?.host.orEmpty()),
        ConfigItem(KEY_PORT, "Port", config?.port?.toString().orEmpty()),
        ConfigItem(KEY_TYPE, "Type", config?.type.orEmpty()),
        ConfigItem(KEY_CIPHER, "Cipher", config?.cipher.orEmpty()),
        ConfigItem(KEY_AUTH, "Auth", config?.auth.orEmpty()),
        ConfigItem(KEY_CA, "Ca", config?.ca.orEmpty()),
        ConfigItem(KEY_KEY, "Key", config?.key.orEmpty()),
        ConfigItem(KEY_CERT, "Cert", config?.cert.orEmpty()),
        ConfigItem(KEY_TLS, "Tls", config?.tlsCrypt.orEmpty())
    )

    override fun getConfigFromRows(rows: List<ConfigItem>): OpenVPNConfig {
        var resultConfig = OpenVPNConfig()
        rows.forEach { configItem ->
            resultConfig = when (configItem.key) {
                KEY_NAME -> resultConfig.copy(name = configItem.value)
                KEY_HOST -> resultConfig.copy(host = configItem.value)
                KEY_PORT -> resultConfig.copy(port = configItem.value.toIntOrNull())
                KEY_TYPE -> resultConfig.copy(type = configItem.value)
                KEY_CIPHER -> resultConfig.copy(cipher = configItem.value)
                KEY_AUTH -> resultConfig.copy(auth = configItem.value)
                KEY_CA -> resultConfig.copy(ca = configItem.value)
                KEY_KEY -> resultConfig.copy(key = configItem.value)
                KEY_CERT -> resultConfig.copy(cert = configItem.value)
                KEY_TLS -> resultConfig.copy(tlsCrypt = configItem.value)
                else -> resultConfig
            }
        }
        return resultConfig
    }

    private companion object {
        private const val KEY_NAME = "KEY_NAME"
        private const val KEY_HOST = "KEY_HOST"
        private const val KEY_PORT = "KEY_PORT"
        private const val KEY_TYPE = "KEY_TYPE"
        private const val KEY_CIPHER = "KEY_CIPHER"
        private const val KEY_AUTH = "KEY_AUTH"
        private const val KEY_CA = "KEY_CA"
        private const val KEY_KEY = "KEY_KEY"
        private const val KEY_CERT = "KEY_CERT"
        private const val KEY_TLS = "KEY_TLS"
    }
}
