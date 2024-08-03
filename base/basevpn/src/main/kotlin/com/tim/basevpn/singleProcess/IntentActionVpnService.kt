package com.tim.basevpn.singleProcess

import android.content.Intent
import com.tim.basevpn.intent.Action
import com.tim.basevpn.intent.actionFromIntent
import com.tim.basevpn.logger.Logger
import com.tim.basevpn.state.ConnectionState

abstract class IntentActionVpnService : BindableVpnService() {

    private var logger: Logger? = null

    open override fun initDependencies(intent: Intent) {
        super.initDependencies(intent)
        logger = Logger("${this::class.simpleName}:IntentActionVpnService: ")
        logger?.d("initDependencies()")
    }

    open override fun clearDependencies() {
        super.clearDependencies()
        logger?.d("clearDependencies()")
        logger = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        logger?.d("onStartCommand()")
        requireNotNull(intent)
        return when (intent.actionFromIntent()) {
            Action.START -> {
                logger?.d("onStartCommand(): Action.START")
                initDependencies(intent)
                updateState(ConnectionState.CONNECTING)
                prepare(intent)
                start()
                START_REDELIVER_INTENT
            }

            Action.STOP -> {
                logger?.d("onStartCommand(): Action.STOP")
                stop()
                START_NOT_STICKY
            }

            Action.MEASURE -> {
                logger?.d("onStartCommand(): Action.MEASURE")
                //intent.parsePingUrl()?.let(::measurePing)
                START_STICKY
            }

            else -> START_STICKY
        }
    }

    abstract override fun start()
}