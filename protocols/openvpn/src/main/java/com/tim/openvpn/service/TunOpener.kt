package com.tim.openvpn.service

import android.os.ParcelFileDescriptor
import com.tim.openvpn.model.TunOptions

interface TunOpener {
    /**
     * Create [android.net.VpnService.Builder] with [android.net.VpnService.Builder.establish]
     */
    fun openTun(tunOptions: TunOptions): ParcelFileDescriptor?
}
