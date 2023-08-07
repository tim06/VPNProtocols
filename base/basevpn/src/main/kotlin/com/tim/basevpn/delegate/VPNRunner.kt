package com.tim.basevpn.delegate

import com.tim.basevpn.configuration.VpnConfiguration

/**
 * @Author: Timur Hojatov
 */
interface VPNRunner {
    fun start(config: VpnConfiguration<*>)
    fun stop()
}
