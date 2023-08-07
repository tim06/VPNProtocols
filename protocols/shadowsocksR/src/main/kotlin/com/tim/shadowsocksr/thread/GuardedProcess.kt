package com.tim.shadowsocksr.thread

import com.tim.shadowsocksr.log.ShadowsocksRLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

internal class GuardedProcess(private val cmd: List<String?>) : CoroutineScope {

    private var processJob: Job? = null
    private var streamLoggerJob: Job? = null
    private var process: Process? = null

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO

    /**
     * Запускает процесс и отслеживает его работу.
     *
     * @param onRestartCallback колбэк, вызываемый при перезапуске процесса.
     */
    fun start(onRestartCallback: (() -> Boolean)? = null) {
        processJob = launch {
            try {
                //while (isActive) {
                    ShadowsocksRLogger.d("GuardedProcess", "Start process: $cmd")

                    val executionTime = measureTimeMillis {
                        // Запускаем процесс с помощью ProcessBuilder
                        process = ProcessBuilder(cmd)
                            .redirectErrorStream(true)
                            .start()

                        withContext(SupervisorJob() + Dispatchers.IO) {
                            // Запускаем отдельную корутину для чтения вывода процесса
                            streamLoggerJob = launch {
                                runCatching {
                                    process?.inputStream?.bufferedReader()?.use { reader ->
                                        reader.lineSequence().forEach { line ->
                                            ShadowsocksRLogger.d("StreamLogger", "Message: $line")
                                        }
                                    }
                                }.onFailure { e ->
                                    ShadowsocksRLogger.e("GuardedProcess", "Error reading input stream: ${e.message}")
                                    // Добавьте здесь дополнительную обработку ошибки, если нужно
                                }
                            }
                        }

                        process?.waitFor()
                        onRestartCallback?.invoke()
                    }

                    // Проверяем, завершился ли процесс слишком быстро
                    if (executionTime < START_TIME_TIMER_DELAY) {
                        ShadowsocksRLogger.d("GuardedProcess", "Process exit too fast, stopping guard: $cmd")
                        coroutineContext.cancelChildren() // Отменяем все потоки в контексте
                    }
                //}
            } catch (e: Exception) {
                ShadowsocksRLogger.e("GuardedProcess", "An error occurred: ${e.message}")
                // Обработка ошибок
            }
        }
    }

    /**
     * Останавливает процесс и освобождает ресурсы.
     */
    fun destroy() {
        coroutineContext.cancelChildren() // Отменяем все потоки в контексте
        process?.destroy() // Уничтожаем процесс
        processJob = null
        process = null
        streamLoggerJob = null
    }

    @TestOnly
    fun getProcess(): Process? {
        return process
    }

    internal companion object {
        private const val START_TIME_TIMER_DELAY = 1000L
    }
}
