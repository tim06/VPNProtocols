package com.tim.ikev2

import com.tim.basevpn.configuration.IVpnConfiguration
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ikev2Configuration(
    val name: String,
    val host: String,
    val login: String,
    val password: String
): IVpnConfiguration
