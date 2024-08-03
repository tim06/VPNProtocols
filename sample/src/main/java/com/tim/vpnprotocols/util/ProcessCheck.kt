package com.tim.vpnprotocols.util

import android.app.ActivityManager
import android.content.Context

fun isProcessRunning(context: Context, process: String): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return activityManager.runningAppProcesses.any {
        it.processName.equals(
            other = process,
            ignoreCase = true
        )
    }
}