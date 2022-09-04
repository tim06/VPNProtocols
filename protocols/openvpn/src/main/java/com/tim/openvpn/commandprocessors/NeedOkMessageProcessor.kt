package com.tim.openvpn.commandprocessors

import com.tim.openvpn.VpnStatus
import com.tim.openvpn.commandprocessors.needok.NeedokCommandProcessor

/**
 * @Author: Тимур Ходжатов
 */
class NeedOkMessageProcessor(
    override val commandProcessors: Map<String, NeedokCommandProcessor>
) : CommandProcessor, NeedokProcessorsHolder {

    override val command: String = NEED_OK

    override fun process(argument: String?) {
        argument?.let {
            val p1 = argument.indexOf('\'')
            val p2 = argument.indexOf('\'', p1 + 1)

            val needed = argument.substring(p1 + 1, p2)
            if (needed == IFCONFIG) {
                commandProcessors.values
                    .find { it.command == IFCONFIG }
                    ?.process(argument.split(":", limit = 2).toTypedArray()[1])
            } else {
                commandProcessors.getOrElse(needed) {
                    VpnStatus.log("NeedOkMessageProcessor: Got unrecognized needok command: $needed")
                    null
                }?.process(needed)
            }
        }
    }

    private companion object {
        private const val NEED_OK = "NEED-OK"
        private const val IFCONFIG = "IFCONFIG"
    }
}