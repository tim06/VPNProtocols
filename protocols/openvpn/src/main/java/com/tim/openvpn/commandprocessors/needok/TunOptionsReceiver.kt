package com.tim.openvpn.commandprocessors.needok

import com.tim.openvpn.model.TunOptions

/**
 * @Author: Тимур Ходжатов
 */
interface TunOptionsReceiver {
    fun onNewTunOptions(tunOptions: TunOptions?)
}