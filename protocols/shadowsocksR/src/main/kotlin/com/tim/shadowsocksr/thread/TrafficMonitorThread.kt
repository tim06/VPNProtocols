package com.tim.shadowsocksr.thread

import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.util.Log
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Thread for monitor traffic
 *
 * @param statPath path to accept socket
 *
 * @Author: Timur Hojatov
 */
internal class TrafficMonitorThread(
    private val statPath: String
) : Thread() {

    private var isRunning = true
    private var serverSocket: LocalServerSocket? = null

    override fun run() {
        // clear
        File(statPath).delete()

        // bind socket
        if (!initServerSocket()) {
            return
        }

        while (isRunning) {
            runCatching {
                val servSocket = serverSocket?.accept()

                runCatching {
                    val input = servSocket?.inputStream
                    val output = servSocket?.outputStream

                    val buffer = ByteArray(BYTE_ARRAY_SIZE)
                    val data = input?.read(buffer)
                    if (data != BYTE_ARRAY_SIZE) throw IOException("Unexpected traffic stat length")
                    val stat = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                    Log.d("TrafficMonitor", "${stat.getLong(0)}, ${stat.getLong(LOG_INDEX)}")
                    output?.write(0)

                    input.close()
                    output?.close()
                }.onFailure { error ->
                    Log.e("TrafficMonitor", "Error when recv traffic stat: $error")
                }

                runCatching {
                    servSocket?.close()
                }
            }.onFailure {
                Log.e("TrafficMonitor", "Error when accept socket")
                initServerSocket()
            }
        }
    }

    /**
     * init server socket
     *
     * @return init failed return false.
     */
    private fun initServerSocket(): Boolean {
        // if not running, do not init
        if (!isRunning) {
            return false
        }

        return try {
            val localSocket = LocalSocket()
            localSocket.bind(
                LocalSocketAddress(
                    statPath,
                    LocalSocketAddress.Namespace.FILESYSTEM
                )
            )
            serverSocket = LocalServerSocket(localSocket.fileDescriptor)
            true
        } catch (e: IOException) {
            Log.e("TrafficMonitor", "$e")
            false
        }
    }

    fun stopThread() {
        isRunning = false
        runCatching {
            serverSocket?.close()
        }
        serverSocket = null
    }

    internal companion object {
        private const val BYTE_ARRAY_SIZE = 16
        private const val LOG_INDEX = 8
    }
}
