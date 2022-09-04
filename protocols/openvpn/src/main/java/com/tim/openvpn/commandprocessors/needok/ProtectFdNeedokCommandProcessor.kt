package com.tim.openvpn.commandprocessors.needok

import android.annotation.SuppressLint
import android.net.LocalSocket
import android.system.Os
import com.tim.openvpn.VpnStatus
import com.tim.openvpn.utils.sendMessage
import com.tim.openvpn.service.FileDescriptorProtector
import java.io.FileDescriptor

/**
 * @Author: Тимур Ходжатов
 */
class ProtectFdNeedokCommandProcessor(
    private val socket: LocalSocket,
    private val fileDescriptorProtector: FileDescriptorProtector
) : NeedokCommandProcessor, FileDescriptorsReceiver {

    override val command: String = PROTECTFD

    private var fileDescriptors: Array<FileDescriptor>? = null

    override fun process(argument: String?): String? {
        fileDescriptors?.firstOrNull()?.let { fileDescriptor ->
            runCatching {
                fileDescriptorProtector.protectFileDescriptor(getInt.invoke(fileDescriptor) as Int)
                Os.close(fileDescriptor)
                socket.sendMessage("needok $command ok\n")
            }.onFailure { error ->
                VpnStatus.log("Failed to retrieve fd from socket ($fileDescriptor)", error.message)
            }
        }
        return null
    }

    override fun setFileDescriptors(fileDescriptors: Array<FileDescriptor>) {
        this.fileDescriptors = fileDescriptors
    }

    @SuppressLint("DiscouragedPrivateApi")
    private val getInt = FileDescriptor::class.java.getDeclaredMethod("getInt$")

    private companion object {
        private const val PROTECTFD = "PROTECTFD"
    }
}