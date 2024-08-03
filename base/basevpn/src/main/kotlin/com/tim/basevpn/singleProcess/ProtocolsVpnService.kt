package com.tim.basevpn.singleProcess

import android.content.Intent
import com.tim.basevpn.intent.parseAllowedApplications
import com.tim.basevpn.logger.Logger

abstract class ProtocolsVpnService : IntentActionVpnService() {

    var allowedApplications: Array<String>? = null
    private var logger: Logger? = null

    open override fun initDependencies(intent: Intent) {
        super.initDependencies(intent)
        logger = Logger("${this::class.simpleName ?: ProtocolsVpnService}:ProtocolsVpnService: ")
        logger?.d("initDependencies()")
    }

    open override fun clearDependencies() {
        super.clearDependencies()
        logger?.d("clearDependencies()")
        logger = null
        allowedApplications = null
    }

    open override fun prepare(intent: Intent) {
        allowedApplications = intent.parseAllowedApplications()
    }

    override fun onRevoke() {
        logger?.d("onRevoke()")
        stop()
        super.onRevoke()
    }

    companion object {
        const val ACTION_KEY = "ACTION_KEY"
        const val ACTION_START_KEY = "ACTION_START_KEY"
        const val ACTION_STOP_KEY = "ACTION_STOP_KEY"
        const val ACTION_MEASURE_KEY = "ACTION_MEASURE_KEY"

        const val NOTIFICATION_CLASS_KEY = "NOTIFICATION_CLASS_KEY"
        const val ALLOWED_APPS_KEY = "ALLOWED_APPS_KEY"
        const val PING_URL_KEY = "PING_URL_KEY"
    }
}