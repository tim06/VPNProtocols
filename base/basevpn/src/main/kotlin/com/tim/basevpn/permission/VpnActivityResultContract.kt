package com.tim.basevpn.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.result.contract.ActivityResultContract

class VpnActivityResultContract : ActivityResultContract<Unit, Boolean>() {
    override fun createIntent(context: Context, input: Unit): Intent = VpnService.prepare(context)
    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        resultCode == Activity.RESULT_OK

    override fun getSynchronousResult(context: Context, input: Unit): SynchronousResult<Boolean>? {
        val isGranted = VpnService.prepare(context) == null
        return if (isGranted) SynchronousResult(true) else null
    }
}
