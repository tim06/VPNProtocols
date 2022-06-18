package com.tim.basevpn.delegate

import android.os.RemoteCallbackList
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Delegate for [RemoteCallbackList]
 *
 * @Author: Timur Hojatov
 */
class StateDelegate : ReadOnlyProperty<Any, RemoteCallbackList<IConnectionStateListener>> {

    private val callbackList = RemoteCallbackList<IConnectionStateListener>()

    override fun getValue(
        thisRef: Any,
        property: KProperty<*>
    ): RemoteCallbackList<IConnectionStateListener> = callbackList
}
