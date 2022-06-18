package com.tim.shadowsocksr.thread

import timber.log.Timber
import java.io.InputStream

/**
 * [Thread] for receive log messages from native libs
 *
 * @param inputStream from processes
 *
 * @Author: Timur Hojatov
 */
internal class StreamLoggerThread(private val inputStream: InputStream) : Thread() {
    override fun run() {
        runCatching {
            inputStream.bufferedReader().use { reader ->
                reader.lineSequence().forEach {
                    Timber.d("Message $it")
                }
            }
        }
    }
}
