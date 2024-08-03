package com.tim.vpnprotocols.view.base

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

abstract class BaseVpnFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    private val vpnPermission = registerForActivityResult(
        VpnActivityResultContract()
    ) { granted: Boolean ->
        if (granted) {
            launch()
        }
    }
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            vpnPermission.launch(Unit)
        } else {
            showNotificationPermissionDeniedSnackbar()
        }
    }

    abstract fun launch()

    open fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            vpnPermission.launch(Unit)
        }
    }

    fun showSnackbar(text: String) {
        Snackbar.make(
            requireView(),
            text,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun showNotificationPermissionDeniedSnackbar() {
        Snackbar.make(
            requireView(),
            "Notification blocked",
            Snackbar.LENGTH_LONG
        ).setAction("Settings") {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            startActivity(intent)
        }.show()
    }
}