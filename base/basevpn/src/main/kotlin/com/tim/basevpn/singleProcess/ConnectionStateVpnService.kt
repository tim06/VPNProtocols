package com.tim.basevpn.singleProcess

import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteCallbackList
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.tim.basevpn.BuildConfig
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.logger.Logger
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.state.ConnectionStateHolder
import com.tim.basevpn.storage.ProtocolsStorage
import com.tim.state.getStateStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

abstract class ConnectionStateVpnService : NotificationVpnService() {

    private var stateHolder: ConnectionStateHolder? = null
    private var storage: DataStore<Preferences>? = null
    private var logger: Logger? = null
    var callbackList: RemoteCallbackList<IConnectionStateListener>? = null

    open override fun initDependencies(intent: Intent) {
        super.initDependencies(intent)
        storage = getStateStorage(applicationContext).also { dataStore ->
            ProtocolsStorage.init(dataStore)
        }
        stateHolder = ConnectionStateHolder(
            requireNotNull(storage),
            stringPreferencesKey(this.javaClass.simpleName)
        )
        callbackList = RemoteCallbackList<IConnectionStateListener>()
        logger = Logger("${this::class.simpleName}:ConnectionStateVpnService: ")
        logger?.d("initDependencies()")
    }

    open override fun clearDependencies() {
        super.clearDependencies()
        logger?.d("clearDependencies()")
        logger = null
        storage = null
        stateHolder = null
        callbackList?.kill()
        callbackList = null
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    fun updateState(connectionState: ConnectionState) {
        logger?.d("updateState(): $connectionState")
        callbackList?.sendCallback { callback -> callback.stateChanged(connectionState) }
        runBlocking {
            withContext(Dispatchers.IO) {
                runCatching { stateHolder?.setCurrentState(connectionState) }
            }
        }
    }

    private inline fun RemoteCallbackList<IConnectionStateListener>.sendCallback(
        crossinline block: (IConnectionStateListener) -> Unit
    ) {
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