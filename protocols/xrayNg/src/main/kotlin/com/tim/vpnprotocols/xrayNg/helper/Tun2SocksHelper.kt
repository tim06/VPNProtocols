package com.tim.vpnprotocols.xrayNg.helper

import com.tim.vpnprotocols.xrayNg.XRayNgService.Companion.MTU
import com.tim.vpnprotocols.xrayNg.XRayNgService.Companion.PRIVATE_VLAN4_ROUTER
import com.tim.vpnprotocols.xrayNg.XRayNgService.Companion.PRIVATE_VLAN6_ROUTER
import com.tim.vpnprotocols.xrayNg.file.FilesDir
import com.tim.vpnprotocols.xrayNg.file.Tun2SocksFile
import com.tim.vpnprotocols.xrayNg.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileDescriptor

internal class Tun2SocksHelper(
    private val filesDir: FilesDir,
    private val tun2SocksFile: Tun2SocksFile,
    private val fdSendHelper: FdSendHelper,
    private val coroutineScope: CoroutineScope
) {

    private val logger: Logger by lazy {
        Logger("Tun2SocksHelper")
    }
    private var process: Process? = null

    internal fun startTun2socks(fd: FileDescriptor) {
        val socksPort = 10808
        val cmd = arrayListOf(
            tun2SocksFile.absolutePath,
            "--netif-ipaddr",
            PRIVATE_VLAN4_ROUTER,
            "--netif-netmask",
            "255.255.255.252",
            "--socks-server-addr",
            "127.0.0.1:${socksPort}",
            "--tunmtu",
            MTU.toString(),
            "--sock-path",
            "sock_path",//File(applicationContext.filesDir, "sock_path").absolutePath,
            "--enable-udprelay",
            "--loglevel",
            "notice"
        )

        cmd.add("--netif-ip6addr")
        cmd.add(PRIVATE_VLAN6_ROUTER)

        val proBuilder = ProcessBuilder(cmd)
        proBuilder.redirectErrorStream(true)
        process = proBuilder
            .directory(filesDir.file)
            .start()
        coroutineScope.launch(Dispatchers.IO) {
            try {
                logger.d("check")
                val waitedValue = process?.waitFor()
                if (waitedValue != 0) {
                    logger.d(
                        "process error -> ${
                            process?.inputStream?.bufferedReader()?.readText()
                        }"
                    )
                }
                logger.d("exited")
                if (isActive) {
                    logger.d("restart")
                    startTun2socks(fd)
                }
            } catch (e: Exception) {
                logger.d("process execution exception -> $e")
            }
        }

        fdSendHelper.sendFd(fd)
    }

    fun stop() {
        try {
            logger.d("destroy")
            process?.destroy()
            process = null
        } catch (e: Exception) {
            logger.d("destroy error -> $e")
        }
    }
}