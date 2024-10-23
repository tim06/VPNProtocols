package com.tim.openvpn.model

import java.util.*

data class CIDRIP(
    var ip: String,
    var len: Int
) {

    internal constructor(ip: String, mask: String) : this(ip, calculateLenFromMask(mask))

    internal constructor(ip: String, mask: String, mode: String) : this(ip, calculateLenFromMask(mask)) {
        val netMaskAsInt = getInt(mask)
        if (len == MASK_LENGTH_MAX && mask != "255.255.255.255") {
            // get the netmask as IP
            val masklen: Int
            val maskResult: Long
            if ("net30" == mode) {
                masklen = MASK_LENGTH_LOW
                maskResult = -0x4
            } else {
                masklen = MASK_LENGTH_MEDIUM
                maskResult = -0x2
            }

            // Netmask is Ip address +/-1, assume net30/p2p with small net
            len = if (netMaskAsInt and maskResult == getInt() and maskResult) {
                masklen
            } else {
                MASK_LENGTH_MAX
            }
        }
    }

    fun getInt(): Long = getInt(ip)

    fun normalise(): Boolean {
        val ip1 = getInt(ip)
        val newip = ip1 and (0xffffffffL shl 32 - len)
        return if (newip != ip1) {
            ip = String.format(
                Locale.US,
                "%d.%d.%d.%d",
                newip and 0xff000000L shr 24,
                newip and 0xff0000L shr 16,
                newip and 0xff00L shr 8,
                newip and 0xffL
            )
            true
        } else {
            false
        }
    }

    override fun toString(): String {
        return String.format(Locale.ENGLISH, "%s/%d", ip, len)
    }


    companion object {
        private const val MASK_LENGTH_MAX = 32
        private const val MASK_LENGTH_MEDIUM = 31
        private const val MASK_LENGTH_LOW = 30

        private const val SHIFT_MAX = 24
        private const val SHIFT_MEDIUM = 16
        private const val SHIFT_LOW = 8

        private const val IP_LAST_BLOCK_INDEX = 3

        private const val MASK_FOR_CHECK = 0x1ffffffffL

        internal fun calculateLenFromMask(mask: String): Int {
            var netmask = getInt(mask)

            // Add 33. bit to ensure the loop terminates
            netmask += 1L shl MASK_LENGTH_MAX
            var lenZeros = 0
            while (netmask and 0x1 == 0L) {
                lenZeros++
                netmask = netmask shr 1
            }
            // Check if rest of netmask is only 1s
            val len: Int = if (netmask != MASK_FOR_CHECK shr lenZeros) {
                // Asume no CIDR, set /32
                MASK_LENGTH_MAX
            } else {
                MASK_LENGTH_MAX - lenZeros
            }
            return len
        }

        internal fun getInt(ipaddr: String): Long {
            val ipt = ipaddr.split(".")
            var ip: Long = 0
            ip += ipt[0].toLong() shl SHIFT_MAX
            ip += (ipt[1].toInt() shl SHIFT_MEDIUM).toLong()
            ip += (ipt[2].toInt() shl SHIFT_LOW).toLong()
            ip += ipt[IP_LAST_BLOCK_INDEX].toInt()
            return ip
        }
    }
}

