package com.tim.basevpn.singleProcess

import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.logger.Logger
import com.tim.basevpn.state.ConnectionState

abstract class BindableVpnService : ConnectionStateVpnService() {

    private var iVpnService: IVPNService? = null
    private var binder: IBinder? = null
    private var logger: Logger? = null

    open override fun initDependencies(intent: Intent) {
        super.initDependencies(intent)
        binder = object : IVPNService.Stub() {
            override fun startVPN() {
                start()
            }

            override fun stopVPN() {
                stop()
            }

            override fun getState(): ConnectionState = ConnectionState.DISCONNECTED
            //runBlocking { connectionStateHolder.getCurrentState() }

            override fun registerCallback(cb: IConnectionStateListener?) {
                callbackList?.register(cb)
                //stateCallback.register(cb)
                //lifecycleScope.launch { updateState(connectionStateHolder.getCurrentState()) }
            }

            override fun unregisterCallback(cb: IConnectionStateListener?) {
                callbackList?.unregister(cb)
                //stateCallback.unregister(cb)
            }
        }
        logger = Logger("${this::class.simpleName}:BindableVpnService: ")
        logger?.d("initDependencies()")
    }

    open override fun clearDependencies() {
        super.clearDependencies()
        logger?.d("clearDependencies()")
        logger = null
        binder = null
    }

    override fun onBind(intent: Intent): IBinder? {
        return if (intent.action == "android.net.VpnService") {
            null
        } else {
            super.onBind(intent)
            binder
        };
    }
}