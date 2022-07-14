package com.tim.basevpn.permission

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.tim.basevpn.extension.getActivity
import com.tim.basevpn.extension.getContext

/**
 * @Author: Timur Hojatov
 */
fun Fragment.vpnPermissionResult(result: (Boolean) -> Unit) = registerForActivityResult(
    VpnActivityResultContract()
) { isSuccess ->
    result.invoke(isSuccess)
}

fun Context.vpnPermissionResult(result: (Boolean) -> Unit) =
    getActivity().registerForActivityResult(
        VpnActivityResultContract()
    ) { isSuccess ->
        result.invoke(isSuccess)
    }

fun LifecycleOwner.vpnPermissionResult(
    result: (Boolean) -> Unit
) = getContext().vpnPermissionResult { isSuccess ->
    result.invoke(isSuccess)
}
