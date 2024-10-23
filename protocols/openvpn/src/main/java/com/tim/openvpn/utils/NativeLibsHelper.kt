package com.tim.openvpn.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.tim.openvpn.BuildConfig
import com.tim.openvpn.VpnStatus
import com.tim.openvpn.core.NativeUtils.nativeAPI
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Write native libs to cache folders
 *
 * @param context Application context
 *
 */
internal class NativeLibsHelper(
    private val context: Context
) {

    fun buildOpenvpnArgv(): Array<String> = listOf(
        writeMiniVPN(),
        "--config",
        "stdin"
    ).toTypedArray()

    private fun writeMiniVPN(): String {
        /* Q does not allow executing binaries written in temp directory anymore */
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            File(context.applicationContext.applicationInfo.nativeLibraryDir, LIBOVPNEXEC).path
        } else {
            var abis = Build.SUPPORTED_ABIS
            if (nativeAPI != abis[0]) {
                abis = arrayOf(nativeAPI)
            }
            // Emulator
            if (abis.all { it == null }) {
                abis = arrayOf("x86")
            }
            for (abi in abis) {
                val vpnExecutable = File(context.applicationContext.cacheDir, "c_$MINIPIEVPN.$abi")
                val isMiniVPNAvailable = writeMiniVPNBinary(
                    abi,
                    vpnExecutable
                )
                if (vpnExecutable.exists() && vpnExecutable.canExecute() || isMiniVPNAvailable) {
                    return vpnExecutable.path
                }
            }
            if (BuildConfig.DEBUG) {
                Log.e(
                    "NativeLibsHelper",
                    "Cannot find any executable for this device's ABIs $abis"
                )
            }
            ""
        }
    }

    private fun writeMiniVPNBinary(abi: String, mvpnout: File): Boolean {
        return runCatching {
            val mvpn: InputStream = context.applicationContext.assets.open("$MINIPIEVPN.$abi")
            FileOutputStream(mvpnout).use { outputStream ->
                val buf = ByteArray(OUTPUTSTREAM_SIZE)
                var lenread = mvpn.read(buf)
                while (lenread > 0) {
                    outputStream.write(buf, 0, lenread)
                    lenread = mvpn.read(buf)
                }
            }
            if (!mvpnout.setExecutable(true)) {
                VpnStatus.log("Failed to make OpenVPN executable")
                return false
            }
            true
        }.onFailure { error ->
            VpnStatus.log(error.message)
        }.getOrDefault(false)
    }

    companion object {
        private const val MINIPIEVPN = "pie_openvpn"
        private const val LIBOVPNEXEC = "libovpnexec.so"

        private const val OUTPUTSTREAM_SIZE = 4096
    }
}
