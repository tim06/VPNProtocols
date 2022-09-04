package com.tim.openvpn.service

/**
 * @Author: Тимур Ходжатов
 */
interface FileDescriptorProtector {
    /**
     * Call [android.net.VpnService.protect]
     */
    fun protectFileDescriptor(fileDescriptor: Int)
}