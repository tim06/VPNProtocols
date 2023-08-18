package com.tim.xtlsr

import com.tim.basevpn.configuration.IVpnConfiguration
import io.nekohasekai.sagernet.database.ProxyEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class XTLSRVpnConfig(
    val proxyEntity: ProxyEntity
) : IVpnConfiguration
