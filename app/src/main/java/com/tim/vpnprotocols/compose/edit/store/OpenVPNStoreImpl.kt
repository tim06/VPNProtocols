package com.tim.vpnprotocols.compose.edit.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tim.openvpn.OpenVPNConfig
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
class OpenVPNStoreImpl(
    private val dataStore: DataStore<Preferences>
) : ConfigStore {

    override val config: Flow<OpenVPNConfig?> = dataStore.data.map {
        it[stringPreferencesKey(OpenVPNConfig::class.java.name)]?.convertToObject()
    }

    override fun getRows(): List<ConfigItem> = listOf(
        ConfigItem(KEY_NAME, "Name"),
        ConfigItem(KEY_HOST, "Host"),
        ConfigItem(KEY_PORT, "Port"),
        ConfigItem(KEY_TYPE, "Type"),
        ConfigItem(KEY_CIPHER, "Cipher"),
        ConfigItem(KEY_AUTH, "Auth"),
        ConfigItem(KEY_CA, "Ca"),
        ConfigItem(KEY_KEY, "Key"),
        ConfigItem(KEY_CERT, "Cert"),
        ConfigItem(KEY_TLS, "Tls"),
    )

    override fun getValueWithKey(key: String): Flow<String?> {
        return config.map { conf ->
            when (key) {
                KEY_NAME -> conf?.name
                KEY_HOST -> conf?.host
                KEY_PORT -> conf?.port?.toString()
                KEY_TYPE -> conf?.type
                KEY_CIPHER -> conf?.cipher
                KEY_AUTH -> conf?.auth
                KEY_CA -> conf?.ca
                KEY_KEY -> conf?.key
                KEY_CERT -> conf?.cert
                KEY_TLS -> conf?.tlsCrypt
                else -> throw IllegalStateException("Not found row with key $key")
            }
        }
    }

    override suspend fun updateConfig(
        key: String,
        newValue: String
    ) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            val initialConfig = prefs[stringPreferencesKey(OpenVPNConfig::class.java.name)]
                ?.convertToObject<OpenVPNConfig>()
            val resultConfig = initialConfig ?: OpenVPNConfig()
            val result = when (key) {
                KEY_NAME -> resultConfig.copy(name = newValue)
                KEY_HOST -> resultConfig.copy(host = newValue)
                KEY_PORT -> resultConfig.copy(port = newValue.toIntOrNull())
                KEY_TYPE -> resultConfig.copy(type = newValue)
                KEY_CIPHER -> resultConfig.copy(cipher = newValue)
                KEY_AUTH -> resultConfig.copy(auth = newValue)
                KEY_CA -> resultConfig.copy(ca = newValue)
                KEY_KEY -> resultConfig.copy(key = newValue)
                KEY_CERT -> resultConfig.copy(cert = newValue)
                KEY_TLS -> resultConfig.copy(tlsCrypt = newValue)
                else -> resultConfig
            }
            prefs[stringPreferencesKey(OpenVPNConfig::class.java.name)] = result.convertToString()
        }
        Unit
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
