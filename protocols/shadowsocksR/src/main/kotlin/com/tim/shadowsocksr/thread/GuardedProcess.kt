package com.tim.shadowsocksr.thread

import android.util.Log
import com.tim.shadowsocksr.BuildConfig

/**
 * [Process] runner
 *
 * @param cmd commands for create [Process]
 *
 * @Author: Timur Hojatov
 */
internal class GuardedProcess(private val cmd: List<String>) {

    private var thread: Thread? = null
    private var process: Process? = null
    private var isDestroyed: Boolean = false
    private var streamLoggerThread: StreamLoggerThread? = null

    fun start(onRestartCallback: (() -> Boolean)? = null): GuardedProcess {
        thread = Thread(
            {
                runCatching {
                    while (isDestroyed.not()) {
                        if (BuildConfig.DEBUG) {
                            Log.d("GuardedProcess","start process: $cmd")
                        }

                        val startTime = System.currentTimeMillis()

                        process = ProcessBuilder(cmd)
                            .redirectErrorStream(true)
                            .start()
                            .also {
                                streamLoggerThread = StreamLoggerThread(it.inputStream)
                                    .also { logger ->
                                        logger.start()
                                    }
                            }

                        onRestartCallback?.invoke()

                        process?.waitFor()

                        synchronized(this) {
                            if (System.currentTimeMillis() - startTime < START_TIME_TIMER_DELAY) {
                                if (BuildConfig.DEBUG) {
                                    Log.w("GuardedProcess", "process exit too fast, stop guard: $cmd")
                                }
                                isDestroyed = true
                            }
                        }
                    }
                }.onFailure {
                    if (BuildConfig.DEBUG) {
                        Log.e("GuardedProcess", "thread interrupt, destroy process: $cmd")
                    }
                    process?.destroy()
                }
            },
            "GuardThread-$cmd"
        )

        thread?.start()
        return this
    }

    fun destroy() {
        isDestroyed = true
        thread?.interrupt()
        streamLoggerThread?.interrupt()
        process?.destroy()
        runCatching {
            thread?.join()
        }
    }

    internal companion object {
        private const val START_TIME_TIMER_DELAY = 1000
    }
}
