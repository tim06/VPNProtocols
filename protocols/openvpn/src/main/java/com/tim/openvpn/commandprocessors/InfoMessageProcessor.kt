package com.tim.openvpn.commandprocessors

/**
 * @Author: Тимур Ходжатов
 */
class InfoMessageProcessor : CommandProcessor {
    override val command: String = INFO

    override fun process(argument: String?) {
        // TODO
    }

    private companion object {
        private const val INFO = "INFO"
    }
}