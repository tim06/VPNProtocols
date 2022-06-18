package com.tim.shadowsocksr

internal object Native {
    init {
        System.loadLibrary("system")
    }

    external fun sendfd(fd: Int, path: String?): Int
    external fun jniclose(fd: Int)
}
