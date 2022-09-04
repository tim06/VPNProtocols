package com.tim.openvpn.commandprocessors

/**
 * Обработка команд полученных от нативной библиотеки
 */
interface CommandProcessor {

    /**
     * Команда, которую может обработать реализация
     */
    val command: String

    /**
     * Обработка команды
     *
     * @param argument входной параметр для обработки
     */
    fun process(argument: String?)
}