package com.tim.basevpn.utils

import android.os.Handler
import android.os.Looper
import android.os.RemoteCallbackList
import android.util.Log
import com.tim.basevpn.BuildConfig
import com.tim.basevpn.IConnectionStateListener

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
                    if (BuildConfig.DEBUG) {
                        Log.e("State", "$exception")
                    }
                }
            }
            finishBroadcast()
        }
    }
}
