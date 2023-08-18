package com.tim.vpnprotocols.view.xtls

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcel
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.tim.basevpn.IConnectionStateListener
import com.tim.basevpn.IVPNService
import com.tim.basevpn.configuration.IVpnConfiguration
import com.tim.basevpn.configuration.VpnConfiguration
import com.tim.basevpn.state.ConnectionState
import com.tim.basevpn.utils.NOTIFICATION_IMPL_CLASS_KEY
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.ShadowsocksFragmentLayoutBinding
import com.tim.vpnprotocols.notification.VpnNotificationImpl
import com.tim.vpnprotocols.view.shadowsocksr.VpnActivityResultContract
import com.tim.xtlsr.XTLSRVpnConfig
import io.nekohasekai.sagernet.Action
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.bg.VpnService
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.group.RawUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * XTLS implementation
 *
 * @Author: Timur Hojatov
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
        val intent = Intent(
            requireContext(),
            VpnService::class.java
        ).apply {
            action = Action.SERVICE
            putExtra(
                NOTIFICATION_IMPL_CLASS_KEY,
                VpnNotificationImpl::class.java.name
            )
        }
        lifecycleScope.launch(Dispatchers.IO) {
            requireContext().bindService(
                intent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun startVpn() {
        lifecycleScope.launch {
            var profile: ProxyEntity? = null
            RawUpdater.parseRaw(config)?.forEach { proxy ->
                profile = ProfileManager.createProfile(99L, proxy)
            }
            if (profile != null) {
                vpnService?.startVPN(
                    VpnConfiguration(
                        XTLSRVpnConfig(
                            proxyEntity = profile!!
                        ),
                        emptySet()
                    )
                )
            }
        }
    }

    private fun stopVpn() {
        vpnService?.stopVPN()
        requireContext().unbindService(serviceConnection)
        requireContext().stopService(Intent(requireContext(), VpnService::class.java))
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

val config = "{\n" +
        "  \"dns\": {\n" +
        "    \"independent_cache\": true,\n" +
        "    \"rules\": [],\n" +
        "    \"servers\": [\n" +
        "      {\n" +
        "        \"address\": \"https://8.8.8.8/dns-query\",\n" +
        "        \"address_resolver\": \"dns-direct\",\n" +
        "        \"strategy\": \"ipv4_only\",\n" +
        "        \"tag\": \"dns-remote\"\n" +
        "      },\n" +
        "      {\n" +
        "        \"address\": \"https://223.5.5.5/dns-query\",\n" +
        "        \"address_resolver\": \"dns-local\",\n" +
        "        \"detour\": \"direct\",\n" +
        "        \"strategy\": \"ipv4_only\",\n" +
        "        \"tag\": \"dns-direct\"\n" +
        "      },\n" +
        "      {\n" +
        "        \"address\": \"local\",\n" +
        "        \"detour\": \"direct\",\n" +
        "        \"tag\": \"dns-local\"\n" +
        "      },\n" +
        "      {\n" +
        "        \"address\": \"rcode://success\",\n" +
        "        \"tag\": \"dns-block\"\n" +
        "      }\n" +
        "    ]\n" +
        "  },\n" +
        "  \"inbounds\": [\n" +
        "    {\n" +
        "      \"listen\": \"127.0.0.1\",\n" +
        "      \"listen_port\": 6450,\n" +
        "      \"override_address\": \"8.8.8.8\",\n" +
        "      \"override_port\": 53,\n" +
        "      \"tag\": \"dns-in\",\n" +
        "      \"type\": \"direct\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"domain_strategy\": \"\",\n" +
        "      \"endpoint_independent_nat\": true,\n" +
        "      \"inet4_address\": [\n" +
        "        \"172.19.0.1/28\"\n" +
        "      ],\n" +
        "      \"mtu\": 9000,\n" +
        "      \"sniff\": true,\n" +
        "      \"sniff_override_destination\": false,\n" +
        "      \"stack\": \"system\",\n" +
        "      \"tag\": \"tun-in\",\n" +
        "      \"type\": \"tun\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"domain_strategy\": \"\",\n" +
        "      \"listen\": \"127.0.0.1\",\n" +
        "      \"listen_port\": 2080,\n" +
        "      \"sniff\": true,\n" +
        "      \"sniff_override_destination\": false,\n" +
        "      \"tag\": \"mixed-in\",\n" +
        "      \"type\": \"mixed\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"log\": {\n" +
        "    \"level\": \"trace\"\n" +
        "  },\n" +
        "  \"outbounds\": [\n" +
        "    {\n" +
        "      \"flow\": \"xtls-rprx-vision\",\n" +
        "      \"packet_encoding\": \"\",\n" +
        "      \"server\": \"92.255.110.135\",\n" +
        "      \"server_port\": 443,\n" +
        "      \"tls\": {\n" +
        "        \"enabled\": true,\n" +
        "        \"insecure\": false,\n" +
        "        \"reality\": {\n" +
        "          \"enabled\": true,\n" +
        "          \"public_key\": \"ohXbv_1dvpByySIG4-gNT5FLQiv48a2srSAOOrYWbDY\",\n" +
        "          \"short_id\": \"aabbccdd\"\n" +
        "        },\n" +
        "        \"server_name\": \"www.microsoft.com\",\n" +
        "        \"utls\": {\n" +
        "          \"enabled\": true,\n" +
        "          \"fingerprint\": \"chrome\"\n" +
        "        }\n" +
        "      },\n" +
        "      \"uuid\": \"3a46d7d2-c550-4e0b-9ddf-82648e8f4ba1\",\n" +
        "      \"type\": \"vless\",\n" +
        "      \"domain_strategy\": \"\",\n" +
        "      \"tag\": \"proxy\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"tag\": \"direct\",\n" +
        "      \"type\": \"direct\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"tag\": \"bypass\",\n" +
        "      \"type\": \"direct\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"tag\": \"block\",\n" +
        "      \"type\": \"block\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"tag\": \"dns-out\",\n" +
        "      \"type\": \"dns\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"route\": {\n" +
        "    \"auto_detect_interface\": true,\n" +
        "    \"rules\": [\n" +
        "      {\n" +
        "        \"outbound\": \"dns-out\",\n" +
        "        \"port\": [\n" +
        "          53\n" +
        "        ]\n" +
        "      },\n" +
        "      {\n" +
        "        \"inbound\": [\n" +
        "          \"dns-in\"\n" +
        "        ],\n" +
        "        \"outbound\": \"dns-out\"\n" +
        "      },\n" +
        "      {\n" +
        "        \"ip_cidr\": [\n" +
        "          \"224.0.0.0/3\",\n" +
        "          \"ff00::/8\"\n" +
        "        ],\n" +
        "        \"outbound\": \"block\",\n" +
        "        \"source_ip_cidr\": [\n" +
        "          \"224.0.0.0/3\",\n" +
        "          \"ff00::/8\"\n" +
        "        ]\n" +
        "      }\n" +
        "    ]\n" +
        "  }\n" +
        "}"