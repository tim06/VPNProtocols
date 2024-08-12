package com.tim.vpnprotocols.xrayNeko.util

import android.content.Context
import java.io.File
import java.io.FileInputStream

// Get log bytes from neko.log
fun getNekoLog(context: Context, max: Long): ByteArray {
    return try {
        val file = File(
            context.applicationContext.cacheDir,
            "neko.log"
        )
        val len = file.length()
        val stream = FileInputStream(file)
        if (max in 1 until len) {
            stream.skip(len - max) // TODO string?
        }
        stream.use { it.readBytes() }
    } catch (e: Exception) {
        e.stackTraceToString().toByteArray()
    }
}