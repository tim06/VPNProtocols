package com.tim.openvpn.service

import android.content.ContentResolver
import android.net.ConnectivityManager
import android.os.ParcelFileDescriptor
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.model.CIDRIP

interface IOpenVPNService {
    fun setMtu(mtu: Int)
    fun addDNS(dns: String?)
    fun addRoute(route: CIDRIP?, include: Boolean)
    fun addRoute(dest: String?, mask: String?, gateway: String?, device: String?)
    fun addRoutev6(network: String?, device: String?)
    fun setDomain(domain: String?)
    fun addHttpProxy(proxy: String?, port: Int): Boolean
    fun openTun(): ParcelFileDescriptor?
    fun setLocalIP(cidrip: CIDRIP?)
    fun setLocalIPv6(ipv6addr: String?)
    fun protectFd(fd: Int): Boolean
    fun trigger_sso(info: String?)

    val ctResolver: ContentResolver?

    val connectivityManager: ConnectivityManager?
    fun openvpnStopped()
    fun updateStateThread(state: ConnectionState)
}
