package com.tim.basevpn.utils

import android.os.Handler
import android.os.Looper
import android.os.RemoteCallbackList
import com.tim.basevpn.IConnectionStateListener
import timber.log.Timber

/**
 * @Author: Timur Hojatov
 */
inline fun RemoteCallbackList<IConnectionStateListener>.sendCallback(
    crossinline block: (IConnectionStateListener) -> Unit
) {
    Handler(Looper.getMainLooper()).post {
        if (registeredCallbackCount > 0) {
            val callbacks = beginBroadcast()
            for (i in 0 until callbacks) {
                runCatching {
                    block.invoke(getBroadcastItem(i))
                }.onFailure { exception ->
                    Timber.e(exception)
                }
            }
            finishBroadcast()
        }
    }
}
