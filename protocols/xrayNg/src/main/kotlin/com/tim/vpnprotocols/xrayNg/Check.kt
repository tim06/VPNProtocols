package com.tim.vpnprotocols.xrayNg

import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException

fun hasConnection(url: String, port: Int): Boolean {
    val result = socketConnectTime(url, port)
    return result != -1L
}

fun socketConnectTime(url: String, port: Int): Long {
    try {
        val socket = Socket()
        val start = System.currentTimeMillis()
        socket.connect(InetSocketAddress(url, port), 2000)
        val time = System.currentTimeMillis() - start
        socket.close()
        return time
    } catch (e: UnknownHostException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return -1
}