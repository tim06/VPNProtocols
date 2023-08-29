package com.tim.notification.timer

import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

class DisconnectTimer(
    private val updateAction: (String) -> Unit,
    private val disconnectAction: () -> Unit
) {

    private var timer: Timer? = null

    fun start(timeToDisconnect: Long = TimeUnit.HOURS.toMillis(2)) {
        val endTime = System.currentTimeMillis() + timeToDisconnect

        timer?.cancel()
        timer = timer(
            name = "VPN Disconnect",
            daemon = true,
            initialDelay = 0,
            period = 1000
        ) {
            val now = System.currentTimeMillis()
            if (now < endTime) {
                val remained = (endTime - now)
                updateAction.invoke(formatMilliseconds(remained))
            } else {
                disconnectAction.invoke()
                timer?.cancel()
            }
        }
    }

    fun cancel() {
        timer?.cancel()
        timer = null
    }

    private fun formatMilliseconds(milliseconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds - TimeUnit.HOURS.toMillis(hours))
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes))

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
