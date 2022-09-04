package com.tim.openvpn.commandprocessors

import com.tim.openvpn.commandprocessors.needok.NeedokCommandProcessor

/**
 * @Author: Тимур Ходжатов
 */
interface NeedokProcessorsHolder {
    val commandProcessors: Map<String, NeedokCommandProcessor>
}