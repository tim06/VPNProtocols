package com.tim.openvpn.service

import android.os.ParcelFileDescriptor
import com.tim.openvpn.model.TunOptions

/**
 * @Author: Timur Hojatov
 */
internal interface VpnServiceManager {
    /**
     * Call [android.net.VpnService.protect]
     */
    fun protectFd(fileDescriptor: Int)
    /**
     * Create [android.net.VpnService.Builder] with [android.net.VpnService.Builder.establish]
     */
    fun openTun(tunOptions: TunOptions): ParcelFileDescriptor?
}
