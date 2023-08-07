package com.tim.openvpn

interface OpenVPNManagement {
    fun stopVPN()

    fun networkChange()

    fun sendCRResponse(response: String)

    fun reconnect()
}