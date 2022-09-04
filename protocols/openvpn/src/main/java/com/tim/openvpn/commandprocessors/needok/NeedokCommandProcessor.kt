package com.tim.openvpn.commandprocessors.needok

/**
 * Обработка команд, которые требуют реакции
 */
interface NeedokCommandProcessor {

    /**
     * Команда, которую может обработать реализация
     */
    val command: String

    /**
     * Обработка команды
     *
     * @param argument входной параметр для обработки
     *
     * @return результат обработки для отправки в сокет
     */
    fun process(argument: String?): String?
}