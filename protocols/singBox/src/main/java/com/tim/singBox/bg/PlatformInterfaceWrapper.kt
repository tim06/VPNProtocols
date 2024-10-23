package com.tim.singBox.bg

import android.os.Build
import com.tim.libbox.InterfaceUpdateListener
import com.tim.libbox.NetworkInterfaceIterator
import com.tim.libbox.PlatformInterface
import com.tim.libbox.StringIterator
import com.tim.libbox.TunOptions
import com.tim.libbox.WIFIState
import java.net.Inet6Address
import java.net.InterfaceAddress
import java.net.NetworkInterface
import java.util.Enumeration
import com.tim.libbox.NetworkInterface as LibboxNetworkInterface

interface PlatformInterfaceWrapper : PlatformInterface {

    override fun usePlatformAutoDetectInterfaceControl(): Boolean {
        return true
    }

    override fun autoDetectInterfaceControl(fd: Int) {
    }

    override fun openTun(options: TunOptions): Int {
        error("invalid argument")
    }

    override fun useProcFS(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    }

    override fun usePlatformDefaultInterfaceMonitor(): Boolean {
        return true
    }

    override fun startDefaultInterfaceMonitor(listener: InterfaceUpdateListener) {

    }

    override fun closeDefaultInterfaceMonitor(listener: InterfaceUpdateListener) {

    }

    override fun usePlatformInterfaceGetter(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    override fun getInterfaces(): NetworkInterfaceIterator {
        return InterfaceArray(NetworkInterface.getNetworkInterfaces())
    }

    override fun underNetworkExtension(): Boolean {
        return false
    }

    override fun includeAllNetworks(): Boolean {
        return false
    }

    override fun clearDNSCache() {
    }

    override fun readWIFIState(): WIFIState? {
        return null
    }

    private class InterfaceArray(private val iterator: Enumeration<NetworkInterface>) :
        NetworkInterfaceIterator {

        override fun hasNext(): Boolean {
            return iterator.hasMoreElements()
        }

        override fun next(): LibboxNetworkInterface {
            val element = iterator.nextElement()
            return LibboxNetworkInterface().apply {
                name = element.name
                index = element.index
                runCatching {
                    mtu = element.mtu
                }
                addresses =
                    StringArray(
                        element.interfaceAddresses.mapTo(mutableListOf()) { it.toPrefix() }
                            .iterator()
                    )
            }
        }

        private fun InterfaceAddress.toPrefix(): String {
            return if (address is Inet6Address) {
                "${Inet6Address.getByAddress(address.address).hostAddress}/${networkPrefixLength}"
            } else {
                "${address.hostAddress}/${networkPrefixLength}"
            }
        }
    }

    private class StringArray(private val iterator: Iterator<String>) : StringIterator {

        override fun len(): Int {
            // not used by core
            return 0
        }

        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }

        override fun next(): String {
            return iterator.next()
        }
    }

}