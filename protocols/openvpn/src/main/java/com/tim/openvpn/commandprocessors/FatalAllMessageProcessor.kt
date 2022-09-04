package com.tim.openvpn.commandprocessors

/**
 * @Author: Тимур Ходжатов
 */
class FatalAllMessageProcessor(
    private val onVpnStop: () -> Unit
) : CommandProcessor {

    override val command: String = FATAL

    override fun process(argument: String?) {
        onVpnStop.invoke()
    }

    private companion object {
        private const val FATAL = "FATAL"
    }
}