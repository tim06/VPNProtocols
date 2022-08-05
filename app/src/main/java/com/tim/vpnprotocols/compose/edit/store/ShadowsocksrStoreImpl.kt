package com.tim.vpnprotocols.compose.edit.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.vpnprotocols.compose.edit.base.ConfigItem
import com.tim.vpnprotocols.compose.edit.base.ConfigStore
import com.tim.vpnprotocols.extension.convertToObject
import com.tim.vpnprotocols.extension.convertToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * @Author: Тимур Ходжатов
 */
class ShadowsocksrStoreImpl(
    private val dataStore: DataStore<Preferences>
) : ConfigStore {

    override val config: Flow<ShadowsocksRVpnConfig?> = dataStore.data.map {
        it[stringPreferencesKey(ShadowsocksRVpnConfig::class.java.name)]?.convertToObject()
    }

    override fun getRows(): List<ConfigItem> = listOf(
        ConfigItem(KEY_NAME, "Name"),
        ConfigItem(KEY_HOST, "Host"),
        ConfigItem(KEY_LOCAL_PORT, "Local port"),
        ConfigItem(KEY_REMOTE_PORT, "Remote port"),
        ConfigItem(KEY_PASSWORD, "Password"),
        ConfigItem(KEY_PROTOCOL, "Protocol"),
        ConfigItem(KEY_PROTOCOL_PARAM, "Protocol param"),
        ConfigItem(KEY_OBFS, "Obfs"),
        ConfigItem(KEY_OBFS_PARAM, "Obfs param"),
        ConfigItem(KEY_METHOD, "Method"),
        ConfigItem(KEY_DNS_ADDRESS, "DNS address"),
        ConfigItem(KEY_DNS_PORT, "DNS port")
    )

    override fun getValueWithKey(key: String): Flow<String?> {
        return config.map { conf ->
            when (key) {
                KEY_NAME -> conf?.name
                KEY_HOST -> conf?.host
                KEY_LOCAL_PORT -> conf?.localPort?.toString()
                KEY_REMOTE_PORT -> conf?.remotePort?.toString()
                KEY_PASSWORD -> conf?.password
                KEY_PROTOCOL -> conf?.protocol
                KEY_PROTOCOL_PARAM -> conf?.protocolParam
                KEY_OBFS -> conf?.obfs
                KEY_OBFS_PARAM -> conf?.obfsParam
                KEY_METHOD -> conf?.method
                KEY_DNS_ADDRESS -> conf?.dnsAddress
                KEY_DNS_PORT -> conf?.dnsPort
                else -> throw IllegalStateException("Not found row with key $key")
            }
        }
    }

    @Suppress("ComplexMethod")
    override suspend fun updateConfig(
        key: String,
        newValue: String
    ) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            val initialConfig = prefs[stringPreferencesKey(ShadowsocksRVpnConfig::class.java.name)]
                ?.convertToObject<ShadowsocksRVpnConfig>()
            val resultConfig = initialConfig ?: ShadowsocksRVpnConfig()
            val result = when (key) {
                KEY_NAME -> resultConfig.copy(name = newValue)
                KEY_HOST -> resultConfig.copy(host = newValue)
                KEY_LOCAL_PORT -> resultConfig.copy(localPort = newValue.toIntOrNull() ?: -1)
                KEY_REMOTE_PORT -> resultConfig.copy(remotePort = newValue.toIntOrNull() ?: -1)
                KEY_PASSWORD -> resultConfig.copy(password = newValue)
                KEY_PROTOCOL -> resultConfig.copy(protocol = newValue)
                KEY_PROTOCOL_PARAM -> resultConfig.copy(protocolParam = newValue)
                KEY_OBFS -> resultConfig.copy(obfs = newValue)
                KEY_OBFS_PARAM -> resultConfig.copy(obfsParam = newValue)
                KEY_METHOD -> resultConfig.copy(method = newValue)
                KEY_DNS_ADDRESS -> resultConfig.copy(dnsAddress = newValue)
                KEY_DNS_PORT -> resultConfig.copy(dnsPort = newValue)
                else -> resultConfig
            }
            prefs[stringPreferencesKey(ShadowsocksRVpnConfig::class.java.name)] =
                result.convertToString()
        }
        Unit
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
