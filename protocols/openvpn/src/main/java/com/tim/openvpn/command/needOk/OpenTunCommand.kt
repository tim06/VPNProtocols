package com.tim.openvpn.command.needOk

import android.annotation.SuppressLint
import android.os.ParcelFileDescriptor
import com.tim.openvpn.thread.OpenVpnManagementThread
import com.tim.openvpn.VpnStatus
import com.tim.openvpn.command.sender.managementCommand
import java.io.FileDescriptor

/**
 * @Author: Timur Hojatov
 */
internal const val OPENTUN = "OPENTUN"

@SuppressLint("DiscouragedPrivateApi")
private val setInt = FileDescriptor::class.java.getDeclaredMethod(
    "setInt$",
    Int::class.javaPrimitiveType
)

internal fun OpenVpnManagementThread.processOpenTunMessage(needed: String): String? =
    if (sendTunFD(needed)) {
        null
    } else {
        "cancel"
    }

internal fun OpenVpnManagementThread.sendTunFD(needed: String): Boolean = runCatching {
    val pfd: ParcelFileDescriptor = tunOptions?.let { vpnServiceManager.openTun(it) } ?: return false
    val fdtosend = FileDescriptor()

    setInt.invoke(fdtosend, pfd.fd)

    socket?.setFileDescriptorsForSend(arrayOf(fdtosend))

    // Trigger a send so we can close the fd on our side of the channel
    // The API documentation fails to mention that it will not reset the file descriptor to
    // be send and will happily send the file descriptor on every write ...
    managementCommand("needok $needed ok\n")

    // Set the FileDescriptor to null to stop this mad behavior
    socket?.setFileDescriptorsForSend(null)


    pfd.close()
    true
}.onFailure { error ->
    VpnStatus.log("Could not send fd over socket", error.message)
}.getOrDefault(false)
