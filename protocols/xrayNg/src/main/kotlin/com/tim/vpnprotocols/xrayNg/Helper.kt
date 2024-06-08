package com.tim.vpnprotocols.xrayNg

import android.content.Context
import android.content.Intent
import android.os.Build

fun startService(context: Context, config: String, domain: String, notificationClass: String, allowedApplications: Array<String>) {
    val intent = Intent(context, XRayNgService::class.java).apply {
        putExtra(XRayNgService.ACTION_KEY, XRayNgService.ACTION_START_KEY)
        putExtra(XRayNgService.CONFIGURATION_KEY, config)
        putExtra(XRayNgService.CONFIGURATION_DOMAIN_KEY, domain)
        putExtra(XRayNgService.NOTIFICATION_CLASS_KEY, notificationClass)
        putExtra(XRayNgService.ALLOWED_APPS_KEY, allowedApplications)
    }
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

fun stopService(context: Context) {
    val intent = Intent(context, XRayNgService::class.java).apply {
        putExtra(XRayNgService.ACTION_KEY, XRayNgService.ACTION_STOP_KEY)
    }
    context.startService(intent)
}