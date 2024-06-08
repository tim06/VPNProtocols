package com.tim.vpnprotocols.xrayNg.file

import java.io.File

@JvmInline
internal value class Tun2SocksFile(val file: File) {

    internal val absolutePath: String
        get() = file.absolutePath
}