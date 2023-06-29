package com.tim.shadowsocksr.thread

import kotlinx.coroutines.*
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.util.Log
import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.LocalSocketAddress
import com.tim.shadowsocksr.utils.TrafficMonitor
import kotlin.coroutines.CoroutineContext

internal class TrafficMonitorThread(
    private val statPath: String,
    private val update: (Long, Long) -> Unit
): CoroutineScope {
    private var isRunning = true
    private var serverSocket: LocalServerSocket? = null

    // Устанавливаем контекст корутин с SupervisorJob и Dispatchers.IO
    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO

    // Запускает мониторинг трафика в отдельной корутине
    fun startThread() {
        launch {
            clearStatFile() // Очищает файл статистики
            if (initServerSocket()) {
                monitorTraffic() // Мониторит трафик
            }
        }
    }

    // Останавливает мониторинг трафика
    fun stopThread() {
        isRunning = false
        cancel() // Отменяет все запущенные корутины в CoroutineScope
        runCatching {
            serverSocket?.close() // Закрывает серверный сокет для освобождения ресурсов
        }
        serverSocket = null
    }

    // Очищает файл статистики
    private suspend fun clearStatFile() {
        withContext(Dispatchers.IO) {
            File(statPath).delete()
        }
    }

    // Инициализирует серверный сокет
    private suspend fun initServerSocket(): Boolean {
        return withContext(Dispatchers.IO) {
            if (!isRunning) {
                return@withContext false
            }

            return@withContext runCatching {
                val localSocket = LocalSocket()
                localSocket.bind(
                    LocalSocketAddress(
                        statPath,
                        LocalSocketAddress.Namespace.FILESYSTEM
                    )
                )
                serverSocket = LocalServerSocket(localSocket.fileDescriptor)
                true
            }.getOrElse { error ->
                Log.e("TrafficMonitor", "$error")
                false
            }
        }
    }

    // Мониторит трафик
    private suspend fun monitorTraffic() {
        while (isRunning) {
            runCatching {
                val servSocket = serverSocket?.accept()

                runCatching {
                    val input = withContext(Dispatchers.IO) {
                        servSocket?.inputStream // Получает входной поток сокета
                    }
                    val output = withContext(Dispatchers.IO) {
                        servSocket?.outputStream // Получает выходной поток сокета
                    }

                    if (input != null) {
                        val buffer = readSocket(input) // Читает данные из входного потока
                        val stat = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                        TrafficMonitor.update(stat.getLong(0), stat.getLong(8))
                        val send = stat.getLong(0)
                        val received = stat.getLong(LOG_INDEX)
                        update.invoke(send, received) // Обновляет статистику
                        Log.d("TrafficMonitor", "$send, $received")
                        if (output != null) {
                            writeSocket(output) // Записывает данные в выходной поток
                        }
                    }

                    input?.close() // Закрывает входной поток
                    output?.close() // Закрывает выходной поток
                }.onFailure { error ->
                    Log.e("TrafficMonitor", "Error when recv traffic stat: $error")
                }

                runCatching {
                    servSocket?.close() // Закрывает сокет
                }
            }.onFailure {
                Log.e("TrafficMonitor", "Error when accept socket")
                initServerSocket() // Повторно инициализирует серверный сокет в случае ошибки
            }
        }
    }

    // Читает данные из входного потока сокета
    private suspend fun readSocket(input: InputStream): ByteArray {
        return withContext(Dispatchers.IO) {
            val buffer = ByteArray(BYTE_ARRAY_SIZE)
            input.read(buffer)
            buffer
        }
    }

    // Записывает данные в выходной поток сокета
    private suspend fun writeSocket(output: OutputStream?) {
        withContext(Dispatchers.IO) {
            output?.write(0)
        }
    }

    companion object {
        private const val BYTE_ARRAY_SIZE = 16
        private const val LOG_INDEX = 8
    }
}
