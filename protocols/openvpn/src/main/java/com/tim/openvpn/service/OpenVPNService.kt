package com.tim.openvpn.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.ParcelFileDescriptor
import android.os.Build
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.R
import com.tim.basevpn.delegate.StateDelegate
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.CONFIG_EXTRA
import com.tim.basevpn.utils.addRoutes
import com.tim.basevpn.utils.sendCallback
import com.tim.notification.NotificationHelper
import com.tim.openvpn.OpenVPNConfig
import com.tim.openvpn.model.TunOptions
import com.tim.openvpn.thread.OpenVPNThread
import com.tim.openvpn.thread.OpenVpnManagementThread
import com.tim.openvpn.utils.NativeLibsHelper

internal class OpenVPNService : VpnService(), Handler.Callback, VpnServiceManager {

    private val nativeDir by lazy {
        applicationContext.applicationInfo.nativeLibraryDir
    }
    private val tmpDir by lazy {
        applicationContext.cacheDir.absolutePath
    }
    private val cacheDir by lazy {
        applicationContext.cacheDir.canonicalPath
    }

    // Notification
    private val notificationHelper by lazy {
        NotificationHelper(
            service = this,
            notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
        )
    }

    private val nativeLibsHelper by lazy {
        NativeLibsHelper(applicationContext)
    }
    private var management: OpenVpnManagementThread? = null
    private var processThread: Thread? = null
    private val stateCallback by StateDelegate()

    private lateinit var config: OpenVPNConfig

    private val binder = object : IVPNService.Stub() {
        override fun startVPN() {
            Thread {
                startOpenVPN()
            }.start()
        }

        override fun stopVPN() {
            stateCallback.sendCallback { it.stateChanged(ConnectionState.DISCONNECTING) }
            management?.stopVPN()
            stateCallback.sendCallback { it.stateChanged(ConnectionState.DISCONNECTED) }
        }

        override fun registerCallback(cb: IConnectionStateListener?) {
            stateCallback.register(cb)
        }

        override fun unregisterCallback(cb: IConnectionStateListener?) {
            stateCallback.unregister(cb)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        config = intent
            ?.extras
            ?.getParcelable(CONFIG_EXTRA)
            ?: return null
        notificationHelper.startNotification()
        return binder
    }

    override fun handleMessage(msg: Message): Boolean {
        val r = msg.callback
        return if (r != null) {
            r.run()
            true
        } else {
            false
        }
    }

    override fun protectFd(fileDescriptor: Int) {
        protect(fileDescriptor)
    }

    override fun openTun(
        tunOptions: TunOptions
    ): ParcelFileDescriptor? = startTun(tunOptions)

    override fun onRevoke() {
        endVpnService()
        super.onRevoke()
    }

    private fun startOpenVPN() {
        // Write OpenVPN binary
        val args = nativeLibsHelper.buildOpenvpnArgv()

        // start a Thread that handles incoming messages of the management socket
        management = OpenVpnManagementThread(
            cacheDir = cacheDir,
            vpnServiceManager = this,
            stateListener = { state ->
                stateCallback.sendCallback { callback ->
                    callback.stateChanged(state)
                }
            }
        ) {
            endVpnService()
        }

        // start a process thread
        processThread = Thread(
            OpenVPNThread(
                config = config,
                threadArgs = args,
                nativeDir = nativeDir,
                tmpDir = tmpDir,
                socketCacheDir = cacheDir
            ),
            PROCESS_THREAD_NAME
        ).apply {
            start()
        }
    }

    /**
     * Establish connection with [tunOptions]
     */
    private fun startTun(tunOptions: TunOptions): ParcelFileDescriptor? {
        val builder = Builder().apply {
            addAddress(tunOptions.localIp.ip, tunOptions.localIp.len)
            setMtu(tunOptions.mtu)
            addRoutes(this@OpenVPNService)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setMetered(false)
            }
            setSession("Session")
        }

        // Reset information
        management?.tunOptions = null

        return runCatching {
            val tun = builder.establish()
                ?: throw NullPointerException(
                    "Android establish() method returned null " +
                            "(Really broken network configuration?)"
                )
            tun
        }.getOrNull()
    }

    private fun endVpnService() {
        notificationHelper.stopNotification()
    }

    companion object {
        const val MANAGEMENT_THREAD_NAME = "OpenVPNManagementThread"

        private const val PROCESS_THREAD_NAME = "OpenVPNProcessThread"
    }
}
