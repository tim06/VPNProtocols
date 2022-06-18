package com.tim.basevpn.permission

import android.content.Context
import android.net.VpnService

/**
 * Is VPN connection establish allowed
 */
fun Context.isVpnPermissionGranted() = VpnService.prepare(this) == null
