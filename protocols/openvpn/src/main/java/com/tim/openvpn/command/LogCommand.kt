package com.tim.openvpn.command

import com.tim.openvpn.VpnStatus

/**
 * @Author: Timur Hojatov
 */
private const val LOG_INDEX = 3

internal fun processLogMessage(argument: String) {
    val args = argument.split(",", limit = 4)
    // 0 unix time stamp
    // 1 log level N,I,E etc.
    /*
      (b) zero or more message flags in a single string:
      I -- informational
      F -- fatal error
      N -- non-fatal error
      W -- warning
      D -- debug, and
    */
    // 2 log message
    val msg = args[LOG_INDEX]
    VpnStatus.log(msg)
}
