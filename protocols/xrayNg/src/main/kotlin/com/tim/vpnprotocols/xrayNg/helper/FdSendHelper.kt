package com.tim.vpnprotocols.xrayNg.helper

import android.net.LocalSocket
import android.net.LocalSocketAddress
import com.tim.basevpn.logger.Logger
import com.tim.vpnprotocols.xrayNg.file.SockFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.io.FileDescriptor

internal class FdSendHelper(
    private val sockFile: SockFile,
    coroutineScope: CoroutineScope
) {

    private var scope: CoroutineScope? = coroutineScope + Dispatchers.IO
    private var logger: Logger? = null

    fun initDependencies() {
        logger = Logger("FdSendHelper")
    }

    fun clearDependencies() {
        scope = null
        logger = null
    }

    fun stop() {
        scope?.cancel()
    }

    internal fun sendFd(fd: FileDescriptor) = scope?.launch {
        val path = sockFile.absolutePath

        var tries = 0
        while (true) {
            if (isActive) {
                try {
                    delay(50L shl tries)
                    logger?.d("sendFd tries: $tries")
                    LocalSocket().use { localSocket ->
                        localSocket.connect(
                            LocalSocketAddress(
                                path,
                                LocalSocketAddress.Namespace.FILESYSTEM
                            )
                        )
                        localSocket.setFileDescriptorsForSend(arrayOf(fd))
                        localSocket.outputStream.write(42)
                    }
                    break
                } catch (e: Exception) {
                    logger?.d("sendFd exception: $e")
                    if (tries > 5) break
                    tries += 1
                }
            }
        }
    }
}