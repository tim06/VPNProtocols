package com.tim.openvpn.thread

import com.tim.openvpn.configuration.OpenVPNConfig
import com.tim.openvpn.VpnStatus
import java.io.OutputStreamWriter

/**
 * OpenVPN thread
 *
 * @param config OpenVPN Profile config
 *
 * @Author: Timur Hojatov
 */
internal class OpenVPNThread(
    private val config: OpenVPNConfig,
    private val threadArgs: Array<String>,
    private val nativeDir: String,
    private val tmpDir: String,
    private val socketCacheDir: String
) : Runnable {

    private var process: Process? = null

    override fun run() {
        runCatching {
            VpnStatus.log(TAG, "Starting openvpn")
            startOpenVPNThreadArgs()
            VpnStatus.log(TAG, "OpenVPN process exited")
        }.onFailure { e ->
            VpnStatus.log("Starting OpenVPN Thread", e.message)
        }.also {
            runCatching {
                process?.waitFor()
            }.onFailure { error ->
                VpnStatus.log("Illegal Thread state: " + error.localizedMessage)
            }
            VpnStatus.log(TAG, "Exiting")
        }
    }

    private fun startOpenVPNThreadArgs() {
        val pb = ProcessBuilder(threadArgs.toList()).run {
            environment()[LIBRARY_PATH] = genLibraryPath(threadArgs, this)
            environment()[TMP_DIR] = tmpDir
            redirectErrorStream(true)
        }

        runCatching {
            process = pb.start().apply {
                OutputStreamWriter(outputStream).use { outputStream ->
                    outputStream.write(config.buildConfig())
                    outputStream.flush()
                }
            }
            while (true) {
                if (Thread.interrupted()) {
                    throw InterruptedException("OpenVpn process was killed form java code")
                }
            }
        }.onFailure { error ->
            VpnStatus.log("Error reading from output of OpenVPN process", error.message)
            process?.destroy()
        }
    }

    private fun genLibraryPath(argv: Array<String>, pb: ProcessBuilder): String {
        // Hack until I find a good way to get the real library path
        val applibpath = argv[0].replaceFirst("/cache/.*$".toRegex(), "/lib")
        var lbpath = pb.environment()[LIBRARY_PATH]
        lbpath = if (lbpath == null) applibpath else "$applibpath:$lbpath"
        if (applibpath != nativeDir) {
            lbpath = "$nativeDir:$lbpath"
        }
        return lbpath
    }

    companion object {
        private const val TAG = "OpenVPN"

        private const val LIBRARY_PATH = "LD_LIBRARY_PATH"
        private const val TMP_DIR = "TMPDIR"
    }
}
