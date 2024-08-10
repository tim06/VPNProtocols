package com.tim.vpnprotocols.xrayNeko.fmt.shadowsocks

import com.tim.vpnprotocols.xrayNeko.fmt.SingBoxOptions
import com.tim.vpnprotocols.xrayNeko.util.Util
import com.tim.vpnprotocols.xrayNeko.parser.decodeBase64UrlSafe
import com.tim.vpnprotocols.xrayNeko.parser.getIntNya
import com.tim.vpnprotocols.xrayNeko.parser.getStr
import com.tim.vpnprotocols.xrayNeko.util.linkBuilder
import com.tim.vpnprotocols.xrayNeko.util.toLink
import com.tim.vpnprotocols.xrayNeko.util.unUrlSafe
import com.tim.vpnprotocols.xrayNeko.util.urlSafe
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject

fun ShadowsocksBean.fixPluginName() {
    if (plugin.startsWith("simple-obfs")) {
        plugin = plugin.replaceFirst("simple-obfs", "obfs-local")
    }
}

fun parseShadowsocks(url: String): ShadowsocksBean {

    if (url.substringBefore("#").contains("@")) {
        var link = url.replace("ss://", "https://").toHttpUrlOrNull() ?: error(
            "invalid ss-android link $url"
        )

        if (link.username.isBlank()) { // fix justmysocks's shit link
            link = (("https://" + url.substringAfter("ss://")
                .substringBefore("#")
                .decodeBase64UrlSafe()).toHttpUrlOrNull()
                ?: error("invalid jms link $url")
                    ).newBuilder().fragment(url.substringAfter("#")).build()
        }

        // ss-android style

        if (link.password.isNotBlank()) {
            return ShadowsocksBean().apply {
                serverAddress = link.host
                serverPort = link.port
                method = link.username
                password = link.password
                plugin = link.queryParameter("plugin") ?: ""
                name = link.fragment
                fixPluginName()
            }
        }

        val methodAndPswd = link.username.decodeBase64UrlSafe()

        return ShadowsocksBean().apply {
            serverAddress = link.host
            serverPort = link.port
            method = methodAndPswd.substringBefore(":")
            password = methodAndPswd.substringAfter(":")
            plugin = link.queryParameter("plugin") ?: ""
            name = link.fragment
            fixPluginName()
        }
    } else {
        // v2rayN style
        var v2Url = url

        if (v2Url.contains("#")) v2Url = v2Url.substringBefore("#")

        val link = ("https://" + v2Url.substringAfter("ss://")
            .decodeBase64UrlSafe()).toHttpUrlOrNull() ?: error("invalid v2rayN link $url")

        return ShadowsocksBean().apply {
            serverAddress = link.host
            serverPort = link.port
            method = link.username
            password = link.password
            plugin = ""
            val remarks = url.substringAfter("#").unUrlSafe()
            if (remarks.isNotBlank()) name = remarks
        }
    }

}

fun ShadowsocksBean.toUri(): String {

    val builder = linkBuilder().username(Util.b64EncodeUrlSafe("$method:$password"))
        .host(serverAddress)
        .port(serverPort)

    if (plugin.isNotBlank()) {
        builder.addQueryParameter("plugin", plugin)
    }

    if (name.isNotBlank()) {
        builder.encodedFragment(name.urlSafe())
    }

    return builder.toLink("ss").replace("$serverPort/", "$serverPort")

}

fun JSONObject.parseShadowsocks(): ShadowsocksBean {
    return ShadowsocksBean().apply {
        serverAddress = getStr("server")
        serverPort = getIntNya("server_port")
        password = getStr("password")
        method = getStr("method")
        name = optString("remarks", "")

        val pId = getStr("plugin")
        if (!pId.isNullOrBlank()) {
            plugin = pId + ";" + optString("plugin_opts", "")
        }
    }
}

fun buildSingBoxOutboundShadowsocksBean(bean: ShadowsocksBean): SingBoxOptions.Outbound_ShadowsocksOptions {
    return SingBoxOptions.Outbound_ShadowsocksOptions().apply {
        type = "shadowsocks"
        server = bean.serverAddress
        server_port = bean.serverPort
        password = bean.password
        method = bean.method
        if (bean.plugin.isNotBlank()) {
            plugin = bean.plugin.substringBefore(";")
            plugin_opts = bean.plugin.substringAfter(";")
        }
    }
}
