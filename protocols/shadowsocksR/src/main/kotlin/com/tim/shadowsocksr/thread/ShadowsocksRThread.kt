package com.tim.shadowsocksr.thread

import android.annotation.SuppressLint
import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.LocalSocketAddress
import com.tim.shadowsocksr.Native
import timber.log.Timber
import java.io.File
import java.io.FileDescriptor
import java.io.IOException

/**
 * [Thread] for keep ShadowsocksR connection
 *
 * @param protectPath cache path for accept socket
 * @param protectFileDescriptor call [android.net.VpnService.protect]
 *
 * @Author: Timur Hojatov
 */
internal class ShadowsocksRThread(
    private val protectPath: String,
    private val protectFileDescriptor: (Int) -> Boolean
) : Thread() {

    private var isRunning = true
    private var serverSocket: LocalServerSocket? = null

    override fun run() {
        // clear
        File(protectPath).delete()

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

                    input?.read()

                    servSocket?.ancillaryFileDescriptors
                        ?.firstOrNull()
                        ?.let { descriptor ->
                            val fd = getInt.invoke(descriptor) as Int
                            val isSuccess = protectFileDescriptor.invoke(
                                fd
                            )
                            Native.jniclose(fd)

                            output?.write(if (isSuccess) 0 else 1)
                        }

                    input?.close()
                    output?.close()
                }.onFailure { error ->
                    Timber.e("Error when protect socket: $error")
                }
                runCatching {
                    servSocket?.close()
                }
            }.onFailure {
                Timber.e("Error when accept socket")
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
                    protectPath,
                    LocalSocketAddress.Namespace.FILESYSTEM
                )
            )
            serverSocket = LocalServerSocket(localSocket.fileDescriptor)
            true
        } catch (e: IOException) {
            Timber.e(e)
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
        @SuppressLint("DiscouragedPrivateApi")
        private val getInt = FileDescriptor::class.java.getDeclaredMethod("getInt$")
    }
}
