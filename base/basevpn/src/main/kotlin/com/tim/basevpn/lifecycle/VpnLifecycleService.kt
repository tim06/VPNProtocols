package com.tim.basevpn.lifecycle

import android.content.Intent
import android.net.VpnService
import android.os.IBinder
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import com.tim.basevpn.logger.Logger

/**
 * A VpnService that is also a [LifecycleOwner].
 */
abstract class VpnLifecycleService : VpnService(), LifecycleOwner {

    private val dispatcher = ServiceLifecycleDispatcher(this)
    private var logger: Logger? = null

    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle

    @CallSuper
    override fun onCreate() {
        logger?.d("onCreate()")
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    @CallSuper
    override fun onBind(intent: Intent): IBinder? {
        initDependencies(intent)
        logger?.d("onBind()")
        prepare(intent)
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    @CallSuper
    override fun onStart(intent: Intent?, startId: Int) {
        logger?.d("onStart()")
        dispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    // this method is added only to annotate it with @CallSuper.
    // In usual Service, super.onStartCommand is no-op, but in LifecycleService
    // it results in dispatcher.onServicePreSuperOnStart() call, because
    // super.onStartCommand calls onStart().
    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger?.d("onStartCommand()")
        return super.onStartCommand(intent, flags, startId)
    }

    @CallSuper
    override fun onDestroy() {
        logger?.d("onDestroy()")
        dispatcher.onServicePreSuperOnDestroy()
        clearDependencies()
        super.onDestroy()
    }

    open fun initDependencies(intent: Intent) {
        logger = Logger("${this::class.simpleName}:VpnLifecycleService: ")
        logger?.d("initDependencies()")
    }

    open fun clearDependencies() {
        logger?.d("clearDependencies()")
        logger = null
    }

    abstract fun start()

    abstract fun stop()

    abstract fun prepare(intent: Intent)
}