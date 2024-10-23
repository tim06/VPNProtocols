package com.tim.vpnprotocols.compose.edit.row

import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.vpnprotocols.compose.edit.base.ConfigItem

/**
 * @Author: Тимур Ходжатов
 */
class ShadowsocksrRowsImpl : ConfigRows<ShadowsocksRVpnConfig> {
    override fun getRowsForConfig(config: ShadowsocksRVpnConfig?): List<ConfigItem> {
        val conf = config ?: ShadowsocksRVpnConfig()
        return listOf(
            ConfigItem(KEY_NAME, "Name", conf.name.orEmpty()),
            ConfigItem(KEY_HOST, "Host", conf.host.orEmpty()),
            ConfigItem(KEY_LOCAL_PORT, "Local port", conf.localPort?.toString().orEmpty()),
            ConfigItem(KEY_REMOTE_PORT, "Remote port", conf.remotePort?.toString().orEmpty()),
            ConfigItem(KEY_PASSWORD, "Password", conf.password.orEmpty()),
            ConfigItem(KEY_PROTOCOL, "Protocol", conf.protocol.orEmpty()),
            ConfigItem(KEY_PROTOCOL_PARAM, "Protocol param", conf.protocolParam.orEmpty()),
            ConfigItem(KEY_OBFS, "Obfs", conf.obfs.orEmpty()),
            ConfigItem(KEY_OBFS_PARAM, "Obfs param", conf.obfsParam.orEmpty()),
            ConfigItem(KEY_METHOD, "Method", conf.method.orEmpty()),
            ConfigItem(KEY_DNS_ADDRESS, "DNS address", conf.dnsAddress.orEmpty()),
            ConfigItem(KEY_DNS_PORT, "DNS port", conf.dnsPort.orEmpty())
        )
    }

    @Suppress("ComplexMethod")
    override fun getConfigFromRows(rows: List<ConfigItem>): ShadowsocksRVpnConfig {
        var resultConfig = ShadowsocksRVpnConfig()
        rows.forEach { configItem ->
            resultConfig = when (configItem.key) {
                KEY_NAME -> resultConfig.copy(name = configItem.value)
                KEY_HOST -> resultConfig.copy(host = configItem.value)
                KEY_LOCAL_PORT -> resultConfig.copy(
                    localPort = configItem.value.toIntOrNull() ?: -1
                )
                KEY_REMOTE_PORT -> resultConfig.copy(
                    remotePort = configItem.value.toIntOrNull() ?: -1
                )
                KEY_PASSWORD -> resultConfig.copy(password = configItem.value)
                KEY_PROTOCOL -> resultConfig.copy(protocol = configItem.value)
                KEY_PROTOCOL_PARAM -> resultConfig.copy(protocolParam = configItem.value)
                KEY_OBFS -> resultConfig.copy(obfs = configItem.value)
                KEY_OBFS_PARAM -> resultConfig.copy(obfsParam = configItem.value)
                KEY_METHOD -> resultConfig.copy(method = configItem.value)
                KEY_DNS_ADDRESS -> resultConfig.copy(dnsAddress = configItem.value)
                KEY_DNS_PORT -> resultConfig.copy(dnsPort = configItem.value)
                else -> resultConfig
            }
        }
        return resultConfig
    }

    private companion object {
        private const val KEY_NAME = "KEY_NAME"
        private const val KEY_HOST = "KEY_HOST"
        private const val KEY_LOCAL_PORT = "KEY_LOCAL_PORT"
        private const val KEY_REMOTE_PORT = "KEY_REMOTE_PORT"
        private const val KEY_PASSWORD = "KEY_PASSWORD"
        private const val KEY_PROTOCOL = "KEY_PROTOCOL"
        private const val KEY_PROTOCOL_PARAM = "KEY_PROTOCOL_PARAM"
        private const val KEY_OBFS = "KEY_OBFS"
        private const val KEY_OBFS_PARAM = "KEY_OBFS_PARAM"
        private const val KEY_METHOD = "KEY_METHOD"
        private const val KEY_DNS_ADDRESS = "KEY_DNS_ADDRESS"
        private const val KEY_DNS_PORT = "KEY_DNS_PORT"
    }
}
