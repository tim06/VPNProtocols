package com.tim.vpnprotocols.sample.singBox.view

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tim.sample.singBox.R
import com.tim.singBox.service.VPNService
import com.tim.vpnprotocols.sample.singBox.VpnActivityResultContract
import go.Seq

class MainActivity : AppCompatActivity() {

    private val vpnPermission = registerForActivityResult(
        VpnActivityResultContract()
    ) { granted: Boolean ->
        if (granted) {
            start()
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            vpnPermission.launch(Unit)
        }
    }

    private val configuration: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        Seq.setContext(this)

        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                vpnPermission.launch(Unit)
            }
        }
        findViewById<Button>(R.id.stopButton).setOnClickListener { stop() }
    }

    private fun start() {

        VPNService.startService(
            context = this,
            config = configuration,
        )
    }

    private fun stop() {
        VPNService.stopService(this)
    }
}