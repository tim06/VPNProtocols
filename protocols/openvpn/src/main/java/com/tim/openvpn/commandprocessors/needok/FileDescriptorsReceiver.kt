package com.tim.openvpn.commandprocessors.needok

import java.io.FileDescriptor

/**
 * @Author: Тимур Ходжатов
 */
interface FileDescriptorsReceiver {
    fun setFileDescriptors(fileDescriptors: Array<FileDescriptor>)
}