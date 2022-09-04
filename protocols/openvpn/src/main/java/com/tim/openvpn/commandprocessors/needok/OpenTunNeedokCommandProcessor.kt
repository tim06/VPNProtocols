package com.tim.openvpn.commandprocessors.needok

import android.annotation.SuppressLint
import android.net.LocalSocket
import com.tim.openvpn.VpnStatus
import com.tim.openvpn.model.TunOptions
import com.tim.openvpn.utils.sendMessage
import com.tim.openvpn.service.TunOpener
import java.io.FileDescriptor

/**
 * @Author: Тимур Ходжатов
 */
class OpenTunNeedokCommandProcessor(
    private val socket: LocalSocket,
    private val tunOpener: TunOpener
) : NeedokCommandProcessor, TunOptionsReceiver {

    private var tunOptions: TunOptions? = null

    override val command: String = OPENTUN

    override fun process(argument: String?): String? {
        sendTunFileDescriptor()
        return null
    }

    override fun onNewTunOptions(tunOptions: TunOptions?) {
        this.tunOptions = tunOptions
    }

    private fun sendTunFileDescriptor() {
        runCatching {
            tunOptions?.let { tunOpener.openTun(it) }?.let { parcelFileDescriptor ->
                val fileDescriptorToSend = FileDescriptor()

                setInt.invoke(fileDescriptorToSend, parcelFileDescriptor.fd)

                socket.setFileDescriptorsForSend(arrayOf(fileDescriptorToSend))

                // Trigger a send so we can close the fd on our side of the channel
                // The API documentation fails to mention that it will not reset the file descriptor to
                // be send and will happily send the file descriptor on every write ...
                socket.sendMessage("needok OPENTUN ok\n")

                // Set the FileDescriptor to null to stop this mad behavior
                socket.setFileDescriptorsForSend(null)

                parcelFileDescriptor.close()
            }
        }.onSuccess {
            VpnStatus.log("OpenTunNeedokCommandProcessor sendTunFileDescriptor success")
        }.onFailure { error ->
            VpnStatus.log("Could not send fd over socket", error.message)
            socket.sendMessage("needok OPENTUN cancel\n")
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    private val setInt = FileDescriptor::class.java.getDeclaredMethod(
        "setInt$",
        Int::class.javaPrimitiveType
    )

    private companion object {
        private const val OPENTUN = "OPENTUN"
    }
}