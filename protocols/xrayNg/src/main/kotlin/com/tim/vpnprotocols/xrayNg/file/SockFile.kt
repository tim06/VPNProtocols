package com.tim.vpnprotocols.xrayNg.file

import java.io.File

@JvmInline
internal value class SockFile(val file: File) {

    internal val absolutePath: String
        get() = file.absolutePath
}