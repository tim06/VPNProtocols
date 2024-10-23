package com.tim.basevpn.delegate

import com.tim.basevpn.configuration.VpnConfiguration

interface VPNRunner {
    fun start(config: VpnConfiguration<*>)
    fun stop()
}
