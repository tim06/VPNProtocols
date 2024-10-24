package com.tim.vpnprotocols.sample.singBox.view

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonParser
import com.tim.sample.singBox.R
import com.tim.singBox.service.VPNService
import com.tim.vpnprotocols.sample.singBox.VpnActivityResultContract
import go.Seq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

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

        lifecycleScope.launch(Dispatchers.IO) {
            val folder = File(cacheDir.path + "/log")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val logFile = File("${folder.path}/box.log")
            if (logFile.exists()) {
                logFile.delete()
            }
        }
    }

    private fun start() {
        val resultConfiguration = configuration.addLogs()
        VPNService.startService(
            context = this,
            config = resultConfiguration,
        )
    }

    private fun stop() {
        VPNService.stopService(this)
    }

    private fun String.addLogs(): String {
        return runCatching {
            val testPath = "${cacheDir.path}/log/box.log"
            val source = JsonParser.parseString(this)
            val sourceJsonObject = source.asJsonObject
            val sourceLogJsonObject = sourceJsonObject.getAsJsonObject("log")
            sourceLogJsonObject.addProperty("output", testPath)
            sourceJsonObject.add("log", sourceLogJsonObject)
            sourceJsonObject.toString()
        }.onFailure {
            println("JsonElementError: $it")
        }.getOrDefault(this)
    }
}