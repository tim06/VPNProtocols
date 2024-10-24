package com.tim.vpnprotocols.view.xtls

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.state.ConnectionState
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.ShadowsocksFragmentLayoutBinding
import com.tim.vpnprotocols.view.base.VpnActivityResultContract
/*import com.tim.xtlsr.XTLSRVpnConfig
import com.tim.libbox.sagernet.Action
import com.tim.libbox.sagernet.SagerNet
import com.tim.libbox.sagernet.bg.VpnService
import com.tim.libbox.sagernet.database.DataStore
import com.tim.libbox.sagernet.database.ProfileManager
import com.tim.vpnprotocols.xrayNeko.fmt.ProxyEntity
import com.tim.libbox.sagernet.database.SagerDatabase
import com.tim.vpnprotocols.xrayNeko.parser.RawUpdater*/

/**
 * XTLS implementation
 *
 */
class XTLSServiceFragment : Fragment(R.layout.shadowsocks_fragment_layout) {

    private val layoutBinding: ShadowsocksFragmentLayoutBinding by viewBinding()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            vpnPermission.launch(Unit)
        } else {
            Snackbar.make(
                requireView(),
                "Notification blocked",
                Snackbar.LENGTH_LONG
            ).setAction("Settings") {
                // Responds to click on the action
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }.show()
        }
    }

    private val vpnPermission = registerForActivityResult(VpnActivityResultContract()) {
        if (it) {
            startVpn()
        }
    }

    private val stateListener: ((ConnectionState) -> Unit) = { connectionStatus ->
        updateState(connectionStatus)
    }
    private val listener = object : IConnectionStateListener.Stub() {
        override fun stateChanged(status: ConnectionState?) {
            status?.let { state ->
                Handler(Looper.getMainLooper()).post {
                    stateListener.invoke(state)
                }
            }
        }

        override fun trafficUpdate(txRate: Long, rxRate: Long, txTotal: Long, rxTotal: Long) {

        }
    }

    private var vpnService: IVPNService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            vpnService = IVPNService.Stub.asInterface(p1)
            vpnService?.registerCallback(listener)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            stateListener.invoke(ConnectionState.DISCONNECTED)
            vpnService = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutBinding.apply {
            startButton.setOnClickListener {
                if (isNotificationPermissionGranted()) {
                    vpnPermission.launch(Unit)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
            stopButton.setOnClickListener {
                stopVpn()
            }
        }
        /*val intent = Intent(
            requireContext(),
            VpnService::class.java
        ).apply {
            action = Action.SERVICE
            putExtra(
                NOTIFICATION_IMPL_CLASS_KEY,
                VpnNotificationImpl::class.java.name
            )
        }*/
        /*lifecycleScope.launch(Dispatchers.IO) {
            requireContext().bindService(
                intent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }*/
    }

    private fun startVpn() {
        /*vpnService?.startVPN(
            VpnConfiguration(
                XTLSRVpnConfig(
                    host = "",
                    publicKey = "",
                    shortId = "",
                    uuid = ""
                ),
                emptySet()
            )
        )*/
    }

    private fun stopVpn() {
        vpnService?.stopVPN()
        requireContext().unbindService(serviceConnection)
        //requireContext().stopService(Intent(requireContext(), VpnService::class.java))
    }

    private fun updateState(state: ConnectionState) {
        when (state) {
            ConnectionState.DISCONNECTED -> {
                layoutBinding.stateTextView.text = "STOPPED"
            }

            ConnectionState.DISCONNECTING -> {
                layoutBinding.stateTextView.text = "STOPPING"
            }

            ConnectionState.CONNECTING -> {
                layoutBinding.stateTextView.text = "CONNECTING"
            }

            ConnectionState.CONNECTED -> {
                layoutBinding.stateTextView.text = "CONNECTED"
            }

            else -> {
                layoutBinding.stateTextView.text = "STOPPED"
            }
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
