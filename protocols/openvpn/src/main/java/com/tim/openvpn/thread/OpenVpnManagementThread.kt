package com.tim.openvpn.thread

import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.LocalSocketAddress
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.VpnStatus
import com.tim.openvpn.command.parser.processInput
import com.tim.openvpn.command.sender.managementCommand
import com.tim.openvpn.model.TunOptions
import com.tim.openvpn.service.OpenVPNService
import com.tim.openvpn.service.VpnServiceManager
import java.io.FileDescriptor
import java.io.IOException

/**
 * OpenVPN management thread
 *
 * Receive and send messages in [processInput]
 * over [managementCommand]
 *
 * @Author: Timur Hojatov
 */
internal class OpenVpnManagementThread(
    cacheDir: String,
    val vpnServiceManager: VpnServiceManager,
    val stateListener: (ConnectionState) -> Unit,
    onEndService: () -> Unit
) : Runnable {

    private val activeThreads = mutableListOf<OpenVpnManagementThread>()
    var tunOptions: TunOptions? = null

    var socket: LocalSocket? = null
    private var serverSocket: LocalServerSocket? = null

    val fdList = mutableListOf<FileDescriptor>()
    var isShuttingDown = false

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

        synchronized(activeThreads) {
            activeThreads.add(this)
        }

        runCatching {
            // Wait for a client to connect
            socket = serverSocket?.accept()
            val instream = socket?.inputStream

            // Close the management socket after client connected
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                VpnStatus.log(e.message)
            }

            // Closing one of the two sockets also closes the other
            managementCommand("version 3\n")

            while (true) {
                val numbytesread = instream?.read(buffer)
                if (numbytesread == null || numbytesread == -1) {
                    return
                }

                runCatching {
                    socket?.ancillaryFileDescriptors?.let {
                        fdList.addAll(it)
                    }
                }.onFailure { error ->
                    VpnStatus.log("Error reading fds from socket", error.message)
                }
                processInput(String(buffer, 0, numbytesread, Charsets.UTF_8))
            }
        }.onFailure { error ->
            VpnStatus.log(error.message)
        }
        synchronized(activeThreads) {
            activeThreads.remove(this)
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

    fun stopVPN(): Boolean {
        val stopSucceed = stopOpenVPN()
        if (stopSucceed) {
            isShuttingDown = true
        }
        return stopSucceed
    }

    private fun stopOpenVPN(): Boolean {
        synchronized(activeThreads) {
            var sendCMD = false
            for (mt in activeThreads) {
                sendCMD = mt.managementCommand("signal SIGINT\n")
                runCatching {
                    mt.socket?.close()
                }
            }
            return sendCMD
        }
    }

    companion object {
        private const val BUFFER_SIZE = 2048

        private const val TRIES_COUNT = 9
        private const val THREAD_SLEEP_TIME = 300L

        const val INFO = "INFO"
        const val HOLD = "HOLD"
        const val NEED_OK = "NEED-OK"
        const val STATE = "STATE"
        const val PROXY = "PROXY"
        const val LOG = "LOG"

        // Currently no action
        const val DNSSERVER = "DNSSERVER"
        const val DNSDOMAIN = "DNSDOMAIN"
        const val ROUTE = "ROUTE"

        const val SUCCESS_START = "SUCCESS:"
    }
}
