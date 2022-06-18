package com.tim.openvpn.command

import android.annotation.SuppressLint
import android.system.Os
import com.tim.openvpn.thread.OpenVpnManagementThread
import com.tim.openvpn.VpnStatus
import java.io.FileDescriptor

/**
 * @Author: Timur Hojatov
 */
internal const val PROTECTFD = "PROTECTFD"
internal const val PROTECTFD_START = "PROTECTFD: "

@SuppressLint("DiscouragedPrivateApi")
private val getInt = FileDescriptor::class.java.getDeclaredMethod("getInt$")

internal fun OpenVpnManagementThread.processProtectFdMessage() {
    fdList.removeFirstOrNull()?.let { fileDescriptor ->
        protectFileDescriptor(fileDescriptor)
    }
}

internal fun OpenVpnManagementThread.protectFileDescriptor(fd: FileDescriptor) = runCatching {
    vpnServiceManager.protectFd(getInt.invoke(fd) as Int)
    Os.close(fd)
}.onFailure { error ->
    VpnStatus.log("Failed to retrieve fd from socket ($fd)", error.message)
}
