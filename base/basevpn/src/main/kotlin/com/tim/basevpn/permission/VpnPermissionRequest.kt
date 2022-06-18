package com.tim.basevpn.permission

import android.content.Context
import androidx.fragment.app.Fragment
import com.tim.basevpn.extension.getActivity

/**
 * @Author: Timur Hojatov
 */
fun Fragment.vpnPermissionResult(result: (Boolean) -> Unit) = registerForActivityResult(
    VpnActivityResultContract()
) { isSuccess ->
    result.invoke(isSuccess)
}

fun Context.vpnPermissionResult(result: (Boolean) -> Unit) = getActivity()!!.registerForActivityResult(
    VpnActivityResultContract()
) { isSuccess ->
    result.invoke(isSuccess)
}
