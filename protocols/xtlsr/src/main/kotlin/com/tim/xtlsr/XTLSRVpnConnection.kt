package com.tim.xtlsr

import android.content.Context
import com.tim.basevpn.configuration.VpnConfiguration
import com.tim.basevpn.connection.VpnConnection
import com.tim.basevpn.connection.VpnServiceConnection
import com.tim.basevpn.state.ConnectionState
import io.nekohasekai.sagernet.bg.VpnService
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.group.RawUpdater

class XTLSRVpnConnection(
    context: Context,
    stateListener: ((ConnectionState) -> Unit)? = null
) : VpnServiceConnection(
    context = context,
    clazz = VpnService::class.java,
    stateListener = stateListener
)