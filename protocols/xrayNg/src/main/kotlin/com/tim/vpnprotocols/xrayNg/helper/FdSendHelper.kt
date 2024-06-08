package com.tim.vpnprotocols.xrayNg.helper

import android.net.LocalSocket
import android.net.LocalSocketAddress
import com.tim.vpnprotocols.xrayNg.file.SockFile
import com.tim.vpnprotocols.xrayNg.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileDescriptor

internal class FdSendHelper(
    private val sockFile: SockFile,
    private val coroutineScope: CoroutineScope
) {

    private val logger: Logger by lazy {
        Logger("FdSendHelper")
    }

    internal fun sendFd(fd: FileDescriptor) = coroutineScope.launch(Dispatchers.IO) {
        val path = sockFile.absolutePath

        var tries = 0
        while (true) try {
            delay(50L shl tries)
            logger.d("sendFd tries: $tries")
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
            logger.d("sendFd exception: $e")
            if (tries > 5) break
            tries += 1
        }
    }
}