package com.tim.ikev2

import android.content.Context
import com.tim.basevpn.configuration.VpnConfiguration
import com.tim.basevpn.connection.VpnConnection
import com.tim.basevpn.state.ConnectionState
import org.strongswan.android.data.VpnProfile
import org.strongswan.android.data.VpnProfileDataSource
import org.strongswan.android.data.VpnType
import org.strongswan.android.logic.VpnStateService
import java.util.UUID

class Ikev2Connection(
    context: Context,
    stateListener: ((ConnectionState) -> Unit)? = null
) : VpnConnection<VpnStateService>(
    context = context,
    clazz = VpnStateService::class.java,
    stateListener = stateListener
) {

    private val storage = VpnProfileDataSource(context)

    override fun start(config: VpnConfiguration<*>) {
        val profile = VpnProfile().apply {
            uuid = UUID.fromString("f000aa01-0451-4000-b000-000000000000")
            name = "VPN Server"//(config.data as Ikev2Configuration).name
            gateway = (config.data as Ikev2Configuration).host
            username = (config.data as Ikev2Configuration).login
            password = (config.data as Ikev2Configuration).password
            vpnType = VpnType.IKEV2_EAP
        }

        with(storage) {
            open()
            if (getVpnProfile(profile.uuid) == null) {
                insertProfile(profile)
            } else {
                deleteVpnProfile(getVpnProfile(profile.uuid))
                insertProfile(profile)
            }
            close()
        }

        super.start(config)
    }
}