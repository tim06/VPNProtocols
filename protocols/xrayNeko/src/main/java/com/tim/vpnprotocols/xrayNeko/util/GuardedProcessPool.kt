package com.tim.vpnprotocols.xrayNeko.util

import android.os.Build
import android.os.SystemClock
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.util.Log
import androidx.annotation.MainThread
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import libcore.Libcore
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.concurrent.thread

class GuardedProcessPool(
    private val noBackupFilesDir: File,
    private val onFatal: suspend (IOException) -> Unit
) : CoroutineScope {
    companion object {
        private val pid by lazy {
            Class.forName("java.lang.ProcessManager\$ProcessImpl").getDeclaredField("pid")
                .apply { isAccessible = true }
        }
    }

    private inner class Guard(
        private val cmd: List<String>,
        private val env: Map<String, String> = mapOf()
    ) {
        private lateinit var process: Process

        private fun streamLogger(input: InputStream, logger: (String) -> Unit) = try {
            input.bufferedReader().forEachLine(logger)
        } catch (_: IOException) {
        }    // ignore

        fun start() {
            process = ProcessBuilder(cmd).directory(noBackupFilesDir).apply {
                environment().putAll(env)
            }.start()
        }

        @DelicateCoroutinesApi
        suspend fun looper(onRestartCallback: (suspend () -> Unit)?) {
            var running = true
            val cmdName = File(cmd.first()).nameWithoutExtension
            val exitChannel = Channel<Int>()
            try {
                while (true) {
                    thread(name = "stderr-$cmdName") {
                        streamLogger(process.errorStream) { Libcore.nekoLogPrintln("[$cmdName] $it") }
                    }
                    thread(name = "stdout-$cmdName") {
                        streamLogger(process.inputStream) { Libcore.nekoLogPrintln("[$cmdName] $it") }
                        // this thread also acts as a daemon thread for waitFor
                        runBlocking { exitChannel.send(process.waitFor()) }
                    }
                    val startTime = SystemClock.elapsedRealtime()
                    val exitCode = exitChannel.receive()
                    running = false
                    when {
                        SystemClock.elapsedRealtime() - startTime < 1000 -> throw IOException(
                            "$cmdName exits too fast (exit code: $exitCode)"
                        )

                        exitCode == 128 + OsConstants.SIGKILL -> Log.d("GuardedProcessPool", "$cmdName was killed")
                        else -> Log.e("GuardedProcessPool", IOException("$cmdName unexpectedly exits with code $exitCode").toString())
                    }
                    Log.d("GuardedProcessPool","restart process: ${Commandline.toString(cmd)} (last exit code: $exitCode)")
                    start()
                    running = true
                    onRestartCallback?.invoke()
                }
            } catch (e: IOException) {
                Log.e("GuardedProcessPool","error occurred. stop guard: ${Commandline.toString(cmd)}")
                GlobalScope.launch(Dispatchers.Main) { onFatal(e) }
            } finally {
                if (running) withContext(NonCancellable) {  // clean-up cannot be cancelled
                    if (Build.VERSION.SDK_INT < 24) {
                        try {
                            Os.kill(pid.get(process) as Int, OsConstants.SIGTERM)
                        } catch (e: ErrnoException) {
                            if (e.errno != OsConstants.ESRCH) Log.e("GuardedProcessPool", e.toString())
                        } catch (e: ReflectiveOperationException) {
                            Log.d("GuardedProcessPool", e.toString())
                        }
                        if (withTimeoutOrNull(500) { exitChannel.receive() } != null) return@withContext
                    }
                    process.destroy()                       // kill the process
                    if (Build.VERSION.SDK_INT >= 26) {
                        if (withTimeoutOrNull(1000) { exitChannel.receive() } != null) return@withContext
                        process.destroyForcibly()           // Force to kill the process if it's still alive
                    }
                    exitChannel.receive()
                }                                           // otherwise process already exited, nothing to be done
            }
        }
    }

    override val coroutineContext = Dispatchers.Main.immediate + Job()
    var processCount = 0

    @MainThread
    fun start(
        cmd: List<String>,
        env: MutableMap<String, String> = mutableMapOf(),
        onRestartCallback: (suspend () -> Unit)? = null
    ) {
        Log.d("GuardedProcessPool","start process: ${Commandline.toString(cmd)}")
        Guard(cmd, env).apply {
            start() // if start fails, IOException will be thrown directly
            launch { looper(onRestartCallback) }
        }
        processCount += 1
    }

    @MainThread
    fun close(scope: CoroutineScope) {
        cancel()
        coroutineContext[Job]!!.also { job -> scope.launch { job.cancelAndJoin() } }
    }
}
