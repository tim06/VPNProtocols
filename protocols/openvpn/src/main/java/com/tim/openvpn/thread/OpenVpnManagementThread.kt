package com.tim.openvpn.thread

import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.LocalSocketAddress
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.VpnStatus
import com.tim.openvpn.model.TunOptions
import com.tim.openvpn.utils.sendMessage
import com.tim.openvpn.service.FileDescriptorProtector
import com.tim.openvpn.service.OpenVPNService
import com.tim.openvpn.service.TunOpener
import java.io.IOException
import kotlin.properties.Delegates

/**
 * OpenVPN management thread
 *
 * @Author: Timur Hojatov
 */
internal class OpenVpnManagementThread(
    cacheDir: String,
    tunOpener: TunOpener,
    fileDescriptorProtector: FileDescriptorProtector,
    stateListener: (ConnectionState) -> Unit,
    onEndService: () -> Unit
) : Runnable {

    private val openVPNMessageProcessor = OpenVPNMessageProcessor(
        tunOpener,
        fileDescriptorProtector,
        stateListener
    ) {
        stopVPN()
    }

    private var socket by Delegates.notNull<LocalSocket>()
    private var serverSocket by Delegates.notNull<LocalServerSocket>()

    init {
        if (openManagementInterface(cacheDir)) {
            Thread(this, OpenVPNService.MANAGEMENT_THREAD_NAME).run {
                start()
            }
            VpnStatus.log("started Socket Thread")
        } else {
            onEndService.invoke()
        }
    }

    override fun run() {
        val buffer = ByteArray(BUFFER_SIZE)

        runCatching {
            // Wait for a client to connect
            socket = serverSocket.accept().also {
                openVPNMessageProcessor.setSocket(it)
            }

            val inputStream = socket.inputStream

            // Close the management socket after client connected
            try {
                serverSocket.close()
            } catch (e: IOException) {
                VpnStatus.log(e.message)
            }

            // Closing one of the two sockets also closes the other
            socket.sendMessage("version 3\n")

            while (true) {
                val numberOfReadBytes = inputStream?.read(buffer)
                if (numberOfReadBytes == null || numberOfReadBytes == -1) {
                    return
                }

                runCatching {
                    socket.ancillaryFileDescriptors?.let { fileDescriptors ->
                        openVPNMessageProcessor.setFileDescriptors(fileDescriptors)
                    }
                }.onFailure { error ->
                    VpnStatus.log("Error reading fds from socket", error.message)
                }
                val message = String(buffer, 0, numberOfReadBytes, Charsets.UTF_8)
                openVPNMessageProcessor.process(message)
            }
        }.onFailure { error ->
            VpnStatus.log("OpenVPN Thread error: ${error.message}")
        }
    }

    private fun openManagementInterface(pathToCacheDir: String): Boolean {
        // Could take a while to open connection
        var tries = TRIES_COUNT

        val socketName = "$pathToCacheDir/mgmtsocket"
        // The mServerSocketLocal is transferred to the LocalServerSocket, ignore warning

        val mServerSocketLocal = LocalSocket()
        while (tries > 0 && !mServerSocketLocal.isBound) {
            runCatching {
                mServerSocketLocal.bind(
                    LocalSocketAddress(
                        socketName,
                        LocalSocketAddress.Namespace.FILESYSTEM
                    )
                )
            }.onFailure {
                // wait 300 ms before retrying
                runCatching {
                    Thread.sleep(THREAD_SLEEP_TIME)
                }
            }
            tries--
        }

        runCatching {
            serverSocket = LocalServerSocket(mServerSocketLocal.fileDescriptor)
            return true
        }.onFailure { error ->
            VpnStatus.log(error.message)
        }
        return false
    }

    fun stopVPN(): Boolean = socket.run {
        runCatching {
            sendMessage("signal SIGINT\n")
            close()
        }
        true
    }

    fun setTunOptions(tunOptions: TunOptions? = null) =
        openVPNMessageProcessor.setTunOptions(tunOptions)

    companion object {
        private const val BUFFER_SIZE = 2048

        private const val TRIES_COUNT = 9
        private const val THREAD_SLEEP_TIME = 300L
    }
}
