package io.nekohasekai.sagernet.ktx

import libcore.Libcore
import java.io.InputStream
import java.io.OutputStream

object Logs {

    private fun mkTag(): String {
        val stackTrace = Thread.currentThread().stackTrace
        return stackTrace[4].className.substringAfterLast(".")
    }

    // level int use logrus.go

    fun d(message: String) {
        print("qweLogs: $message")
        Libcore.nekoLogPrintln("[Debug] [${mkTag()}] $message")
    }

    fun d(message: String, exception: Throwable) {
        print("qweLogs: $message")
        Libcore.nekoLogPrintln("[Debug] [${mkTag()}] $message" + "\n" + exception.stackTraceToString())
    }

    fun i(message: String) {
        print("qweLogs: $message")
        Libcore.nekoLogPrintln("[Info] [${mkTag()}] $message")
    }

    fun i(message: String, exception: Throwable) {
        print("qweLogs: $message")
        Libcore.nekoLogPrintln("[Info] [${mkTag()}] $message" + "\n" + exception.stackTraceToString())
    }

    fun w(message: String) {
        print("qweLogs: $message")
        Libcore.nekoLogPrintln("[Warning] [${mkTag()}] $message")
    }

    fun w(message: String, exception: Throwable) {
        print("qweLogs: $message")
        Libcore.nekoLogPrintln("[Warning] [${mkTag()}] $message" + "\n" + exception.stackTraceToString())
    }

    fun w(exception: Throwable) {
        print("qweLogs: ${exception.message}")
        Libcore.nekoLogPrintln("[Warning] [${mkTag()}] " + exception.stackTraceToString())
    }

    fun e(message: String) {
        print("qweLogs: $message")
        Libcore.nekoLogPrintln("[Error] [${mkTag()}] $message")
    }

    fun e(message: String, exception: Throwable) {
        print("qweLogs: $message")
        Libcore.nekoLogPrintln("[Error] [${mkTag()}] $message" + "\n" + exception.stackTraceToString())
    }

    fun e(exception: Throwable) {
        print("qweLogs: ${exception.message}")
        Libcore.nekoLogPrintln("[Error] [${mkTag()}] " + exception.stackTraceToString())
    }

}

fun InputStream.use(out: OutputStream) {
    use { input ->
        out.use { output ->
            input.copyTo(output)
        }
    }
}