package com.tim.basevpn.utils

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build

fun Context.currentProcess(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Application.getProcessName()
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val manager = getSystemService(ActivityManager::class.java) as ActivityManager
        val pid = android.os.Process.myPid()
        val process = manager.runningAppProcesses.firstOrNull { it.pid == pid }
        process?.processName.orEmpty()
    } else {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pid = android.os.Process.myPid()
        val process = manager.runningAppProcesses.firstOrNull { it.pid == pid }
        process?.processName.orEmpty()
    }
}