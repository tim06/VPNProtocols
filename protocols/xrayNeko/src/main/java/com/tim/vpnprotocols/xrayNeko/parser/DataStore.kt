package com.tim.vpnprotocols.xrayNeko.parser

object DataStore {
    val globalAllowInsecure: Boolean = true
    val logLevel: Int = 0
    val localDNSPort: Int = 6450
    val mtu: Int = 9000
    val enableClashAPI: Boolean = false
    var remoteDns: String = "https://dns.google/dns-query"
    var directDns: String = "local"
    var enableDnsRouting: Boolean = true
    var enableFakeDns: Boolean = false
    var trafficSniffing: Int = 1
    var ipv6Mode: Int = IPv6Mode.DISABLE
    var resolveDestination: Boolean = false
    var tunImplementation: Int = TunImplementation.MIXED
    var mixedPort: Int = 2080
    var bypassLanInCore: Boolean = false
    var allowAccess: Boolean = false
}

object IPv6Mode {
    const val DISABLE = 0
    const val ENABLE = 1
    const val PREFER = 2
    const val ONLY = 3
}

object TunImplementation {
    const val GVISOR = 0
    const val SYSTEM = 1
    const val MIXED = 2
}