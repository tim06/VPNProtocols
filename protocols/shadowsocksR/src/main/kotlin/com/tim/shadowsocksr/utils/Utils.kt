package com.tim.shadowsocksr.utils

import java.io.File
import java.io.PrintWriter

internal fun File.printToFile(
    content: String,
    isPrintln: Boolean = false
) = PrintWriter(this).use {
        if (isPrintln) {
            it.println(content)
        } else {
            it.print(content)
        }
        it.flush()
    }
