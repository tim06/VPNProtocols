package com.tim.openvpn.commandprocessors

import com.tim.openvpn.VpnStatus

/**
 * @Author: Тимур Ходжатов
 */
class LogMessageProcessor : CommandProcessor {

    override val command: String = LOG

    override fun process(argument: String?) {
        val args = argument?.split(",", limit = 4)
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
        val msg = args?.get(LOG_INDEX)
        VpnStatus.log(msg)
    }

    private companion object {
        private const val LOG_INDEX = 3
        private const val LOG = "LOG"
    }
}