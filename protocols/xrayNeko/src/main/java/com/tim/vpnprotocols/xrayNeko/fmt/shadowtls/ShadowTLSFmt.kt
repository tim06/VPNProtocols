package com.tim.vpnprotocols.xrayNeko.fmt.shadowtls

import com.tim.vpnprotocols.xrayNeko.fmt.SingBoxOptions
import com.tim.vpnprotocols.xrayNeko.fmt.v2ray.buildSingBoxOutboundTLS

fun buildSingBoxOutboundShadowTLSBean(bean: ShadowTLSBean): SingBoxOptions.Outbound_ShadowTLSOptions {
    return SingBoxOptions.Outbound_ShadowTLSOptions().apply {
        type = "shadowtls"
        server = bean.serverAddress
        server_port = bean.serverPort
        version = bean.version
        password = bean.password
        tls = buildSingBoxOutboundTLS(bean)
    }
}
