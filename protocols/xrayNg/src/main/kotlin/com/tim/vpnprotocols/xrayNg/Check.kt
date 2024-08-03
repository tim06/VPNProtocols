package com.tim.vpnprotocols.xrayNg

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException

suspend fun hasConnection(url: String, port: Int): Boolean {
    val result = socketConnectTime(url, port)
    return result != -1L
}

suspend fun socketConnectTime(url: String, port: Int): Long = withContext(Dispatchers.IO) {
    try {
        val socket = Socket()
        val start = System.currentTimeMillis()
        socket.connect(InetSocketAddress(url, port), 2000)
        val time = System.currentTimeMillis() - start
        socket.close()
        time
    } catch (e: UnknownHostException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    -1
}