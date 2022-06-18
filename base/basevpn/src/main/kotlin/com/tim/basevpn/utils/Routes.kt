package com.tim.basevpn.utils

import android.content.Context
import android.net.VpnService
import com.tim.basevpn.R

/**
 * Add routes to [android.net.VpnService.Builder]
 * from resources list
 *
 * @param context for retrieve routes list
 *
 * @Author: Timur Hojatov
 */
fun VpnService.Builder.addRoutes(context: Context) {
    context.resources.getStringArray(R.array.bypass_private_route)
        .associate { route ->
            val split = route.split("/")
            Pair(split[0], Integer.parseInt(split[1]))
        }.onEach { map ->
            addRoute(map.key, map.value)
        }
}
