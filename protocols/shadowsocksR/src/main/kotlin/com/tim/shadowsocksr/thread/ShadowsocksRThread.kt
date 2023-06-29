package com.tim.shadowsocksr.thread

import android.annotation.SuppressLint
import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.util.Log
import com.tim.shadowsocksr.Native
import kotlinx.coroutines.*
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import kotlin.coroutines.CoroutineContext

/**
 * [CoroutineScope] for keeping ShadowsocksR connection
 *
 * @param protectPath cache path for accept socket
 * @param protectFileDescriptor call [android.net.VpnService.protect]
 *
 * @Author: Timur Hojatov
 */
internal class ShadowsocksRThread(
    private val protectPath: String,
    private val protectFileDescriptor: (Int) -> Boolean
) : CoroutineScope {

    private var isRunning = true
    private var serverSocket: LocalServerSocket? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    fun start() {
        launch {
            try {
                // clear
                File(protectPath).delete()

                // bind socket
                if (!initServerSocket()) {
                    return@launch
                }

                while (isRunning) {
                    try {
                        val servSocket = serverSocket?.accept()

                        try {
                            val input = servSocket?.inputStream
                            val output = servSocket?.outputStream

                            input?.read()

                            servSocket?.ancillaryFileDescriptors
                                ?.firstOrNull()
                                ?.let { descriptor ->
                                    val fd = getInt.invoke(descriptor) as Int
                                    val isSuccess = protectFileDescriptor.invoke(fd)
                                    Native.jniclose(fd)

                                    output?.write(if (isSuccess) 0 else 1)
                                }

                            input?.close()
                            output?.close()
                        } catch (error: Throwable) {
                            Log.e("ShadowsocksRThread", "Error during I/O operations: $error")
                        } finally {
                            servSocket?.close()
                        }
                    } catch (e: IOException) {
                        Log.e("ShadowsocksRThread", "Error when accept socket")
                        initServerSocket()
                    }
                }
            } catch (e: IOException) {
                Log.e("ShadowsocksRThread", "Error during server socket initialization: $e")
            }
        }
    }

    /**
     * Initialize server socket
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
            Log.e("ShadowsocksRThread", "$e")
            false
        }
    }

    fun stopThread() {
        isRunning = false
        serverSocket?.close()
        cancel()
    }

    internal companion object {
        @SuppressLint("DiscouragedPrivateApi")
        private val getInt = FileDescriptor::class.java.getDeclaredMethod("getInt$")
    }
}
