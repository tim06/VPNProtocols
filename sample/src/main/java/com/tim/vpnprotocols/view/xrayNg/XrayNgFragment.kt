package com.tim.vpnprotocols.view.xrayNg

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.ShadowsocksFragmentLayoutBinding
import com.tim.vpnprotocols.view.shadowsocksr.VpnActivityResultContract
import com.tim.vpnprotocols.xrayNg.startService
import com.tim.vpnprotocols.xrayNg.stopService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class XrayNgFragment : Fragment(R.layout.shadowsocks_fragment_layout) {

    private val layoutBinding: ShadowsocksFragmentLayoutBinding by viewBinding()

    private val vpnPermission = registerForActivityResult(VpnActivityResultContract()) {
        if (it) {
            startService(requireContext(), config2, "121.127.46.131:42118")
        }
    }
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

    val DIR_ASSETS = "assets"

    val config2 = "{\"log\":{\"loglevel\":\"info\"},\"dns\":{\"hosts\":{\"domain:googleapis.cn\":\"googleapis.com\"},\"servers\":[\"1.1.1.1\",{\"address\":\"223.5.5.5\",\"port\":53,\"domains\":[\"geosite:cn\",\"geosite:geolocation-cn\"],\"expectIPs\":[\"geoip:cn\"]}]},\"routing\":{\"domainStrategy\":\"IPIfNonMatch\",\"rules\":[{\"ip\":[\"1.1.1.1\"],\"port\":\"53\",\"outboundTag\":\"proxy\"},{\"ip\":[\"223.5.5.5\"],\"port\":\"53\",\"outboundTag\":\"direct\"},{\"domain\":[\"domain:googleapis.cn\"],\"outboundTag\":\"proxy\"},{\"ip\":[\"geoip:private\"],\"outboundTag\":\"direct\"},{\"ip\":[\"geoip:cn\"],\"outboundTag\":\"direct\"},{\"domain\":[\"geosite:cn\"],\"outboundTag\":\"direct\"},{\"domain\":[\"geosite:geolocation-cn\"],\"outboundTag\":\"direct\"},{\"port\":\"0-65535\",\"outboundTag\":\"proxy\"}]},\"inbounds\":[{\"listen\":\"127.0.0.1\",\"port\":10808,\"protocol\":\"socks\",\"settings\":{\"type\":\"io.github.tim06.inbounds.settings.SocksInboundConfigurationObject\",\"auth\":\"noauth\",\"udp\":true,\"userLevel\":8},\"sniffing\":{\"enabled\":true,\"destOverride\":[\"http\",\"tls\"],\"routeOnly\":false},\"tag\":\"socks\"},{\"listen\":\"127.0.0.1\",\"port\":10809,\"protocol\":\"http\",\"settings\":{\"type\":\"io.github.tim06.inbounds.settings.HttpInboundConfigurationObject\",\"userLevel\":8},\"sniffing\":null,\"tag\":\"http\"}],\"outbounds\":[{\"sendThrough\":null,\"protocol\":\"trojan\",\"settings\":{\"type\":\"io.github.tim06.outbounds.settings.TrojanOutboundConfigurationObject\",\"servers\":[{\"address\":\"121.127.46.131\",\"port\":42118,\"password\":\"ypDt8RkT7J\",\"level\":8}]},\"streamSettings\":{\"network\":\"tcp\",\"security\":\"tls\",\"tlsSettings\":{\"serverName\":\"wildlydrowse.com\",\"allowInsecure\":true,\"alpn\":[\"http/1.1\"],\"fingerprint\":\"chrome\"},\"tcpSettings\":{\"header\":{\"type\":\"none\"}}},\"tag\":\"proxy\",\"proxySettings\":null,\"mux\":{\"enabled\":false,\"concurrency\":-1,\"xudpConcurrency\":8,\"xudpProxyUDP443\":\"\"}},{\"sendThrough\":null,\"protocol\":\"freedom\",\"settings\":null,\"streamSettings\":null,\"tag\":\"direct\",\"proxySettings\":null,\"mux\":null},{\"sendThrough\":null,\"protocol\":\"blackhole\",\"settings\":{\"type\":\"io.github.tim06.outbounds.settings.BlackholeOutboundConfigurationObject\",\"response\":{\"type\":\"http\"}},\"streamSettings\":null,\"tag\":\"block\",\"proxySettings\":null,\"mux\":null}]}"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutBinding.startButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                vpnPermission.launch(Unit)
            }
        }

        layoutBinding.stopButton.setOnClickListener { stopService(requireContext()) }

        layoutBinding.stateTextView.text = "Test"
        copyAssets()
    }

    private fun copyAssets() {
        val extFolder = userAssetPath(requireContext())
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geo = arrayOf("geosite.dat", "geoip.dat")
                requireActivity().assets.list("")
                    ?.filter { geo.contains(it) }
                    ?.filter { !File(extFolder, it).exists() }
                    ?.forEach {
                        val target = File(extFolder, it)
                        requireActivity().assets.open(it).use { input ->
                            FileOutputStream(target).use { output ->
                                input.copyTo(output)
                            }
                        }
                        Log.i("qweqwe", "Copied from apk assets folder to ${target.absolutePath}")
                    }
            } catch (e: Exception) {
                Log.e("qweqwe", "asset copy failed", e)
            }
        }
    }

    private fun userAssetPath(context: Context): String {
        val extDir = context.getExternalFilesDir(DIR_ASSETS)
            ?: return context.getDir(DIR_ASSETS, 0).absolutePath
        return extDir.absolutePath
    }

    /*private fun checkConnection() {
        lifecycleScope.launch(Dispatchers.IO) {
            val hasConnection = hasConnection("www.instagram.com", 443)
            if (hasConnection) {

            } else {
                stopService(requireContext())
                delay(300)
                startService(requireContext(), config, "121.127.46.131:42119")
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Connected — $hasConnection", Toast.LENGTH_SHORT).show()
            }
        }
    }*/
}