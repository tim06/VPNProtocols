package com.tim.shadowsocksr.config

import com.tim.shadowsocksr.ShadowsocksRVpnConfig
import com.tim.shadowsocksr.utils.printToFile
import java.io.File

/**
 * Write *config files to cache dir
 *
 * @param config current user configuration
 * for build *.conf
 *
 * @Author: Timur Hojatov
 */
internal class ConfigWriter(
    private val config: ShadowsocksRVpnConfig
) {

    private val localPort = config.localPort ?: DEFAULT_LOCAL_PORT

    fun printConfigsToFiles(
        dataDir: String,
        protectPath: String
    ) {
        File("$dataDir/libssr-local.so-vpn.conf").printToFile(
            buildShadowsocksDaemonConfig()
        )
        File("$dataDir/ss-tunnel-vpn.conf").printToFile(
            buildDnsTunnelConfig()
        )
        File("$dataDir/libpdnsd.so-vpn.conf").printToFile(
            buildDnsDaemonConfig(dataDir, protectPath)
        )
    }

    fun buildShadowSocksDaemonCmd(
        dataDir: String,
        nativeDir: String
    ) = listOf(
        "$nativeDir/$SS_LOCAL_FILE_NAME",
        "-V",
        "-x",
        "-b",
        "127.0.0.1",
        "--host",
        config.host,
        "-P",
        dataDir,
        "-c",
        "$dataDir/$SS_LOCAL_CONFIG_FILE_NAME"
    )

    internal fun buildDnsDaemonCmd(
        dataDir: String,
        nativeDir: String
    ) = listOf(
        "$nativeDir/$PDNSD_FILE_NAME",
        "-c",
        "$dataDir/$PDNSD_CONFIG_FILE_NAME",
        "-v5"
    )

    internal fun buildDnsTunnelCmd(
        dataDir: String,
        nativeDir: String
    ) = listOf(
        "$nativeDir/$SS_LOCAL_FILE_NAME",
        "-V",
        "-u",
        "--host",
        config.host,
        "-b",
        "127.0.0.1",
        "-P",
        dataDir,
        "-c",
        "$dataDir/$SSTUNNEL_CONFIG_FILE_NAME",
        "-L",
        "${config.dnsAddress}:${config.dnsPort}"
    )

    internal fun buildTun2SocksCmd(
        fd: String,
        dataDir: String,
        nativeDir: String
    ) = listOf(
        "$nativeDir/$LIB_TUN_SOCKS_FILE_NAME",
        "--netif-ipaddr",
        "172.19.0.2",
        "--netif-netmask",
        "255.255.255.0",
        "--socks-server-addr",
        "127.0.0.1:${localPort}",
        "--tunfd",
        fd,
        "--tunmtu",
        "1500",
        "--sock-path",
        "$dataDir/sock_path",
        /*"--logger",
        "syslog",*/
        "--loglevel",
        "5",
        "--dnsgw",
        "172.19.0.1:${localPort + TUN2SOCKS_PLUS_PORT}"
    )

    private fun buildShadowsocksDaemonConfig(): String = "{" +
            "\"server\": \"${config.host}\", " +
            "\"server_port\": ${config.remotePort}, " +
            "\"local_port\": ${localPort}, " +
            "\"password\": \"${config.password}\", " +
            "\"method\": \"${config.method}\", " +
            "\"timeout\": ${SHADOWSOCKS_DAEMON_TIMEOUT}, " +
            "\"protocol\": \"${config.protocol}\", " +
            "\"obfs\": \"${config.obfs}\", " +
            "\"obfs_param\": \"${config.obfsParam}\", " +
            "\"protocol_param\": \"${config.protocolParam}\"" +
            "}"

    private fun buildDnsTunnelConfig(): String = "{" +
            "\"server\": \"${config.host}\", " +
            "\"server_port\": ${config.remotePort}, " +
            "\"local_port\": ${localPort + DNS_TUNNEL_PLUS_PORT}, " +
            "\"password\": \"${config.password}\", " +
            "\"method\": \"${config.method}\", " +
            "\"timeout\": ${DNS_TUNNEL_TIMEOUT}, " +
            "\"protocol\": \"${config.protocol}\", " +
            "\"obfs\": \"${config.obfs}\", " +
            "\"obfs_param\": \"${config.obfsParam}\", " +
            "\"protocol_param\": \"${config.protocolParam}\"" +
            "}"

    private fun buildDnsDaemonConfig(
        dataDir: String,
        protectPath: String
    ): String = "global {" +
            "perm_cache = 2048;" +
            "protect = \"${protectPath}\";" +
            "cache_dir = \"${dataDir}\";" +
            "server_ip = 0.0.0.0;" +
            "server_port = ${localPort + DNS_DAEMON_GLOBAL_PLUS_PORT};" +
            "query_method = tcp_only;" +
            "min_ttl = 15m;" +
            "max_ttl = 1w;" +
            "timeout = 10;" +
            "daemon = off;" +
            "}" +
            "server {" +
            "label = \"local\";" +
            "ip = 127.0.0.1;" +
            "port = ${localPort + DNS_DAEMON_SERVER_PLUS_PORT};" +
            "reject = 224.0.0.0/3, ::/0;" +
            "reject_policy = negate;" +
            "reject_recursively = on;" +
            "}" +
            "rr {" +
            "name=localhost;" +
            "reverse=on;" +
            "a=127.0.0.1;" +
            "owner=localhost;" +
            "soa=localhost,root.localhost,42,86400,900,86400,86400;" +
            "}"


    private companion object {
        private const val SS_LOCAL_FILE_NAME = "libssr-local.so"
        private const val SS_LOCAL_CONFIG_FILE_NAME = "libssr-local.so-vpn.conf"

        private const val PDNSD_FILE_NAME = "libpdnsd.so"
        private const val PDNSD_CONFIG_FILE_NAME = "libpdnsd.so-vpn.conf"

        private const val SSTUNNEL_CONFIG_FILE_NAME = "ss-tunnel-vpn.conf"

        private const val LIB_TUN_SOCKS_FILE_NAME = "libtun2socks_ssr.so"

        private const val DEFAULT_LOCAL_PORT = 1080

        private const val SHADOWSOCKS_DAEMON_TIMEOUT = 600
        private const val TUN2SOCKS_PLUS_PORT = 53

        private const val DNS_TUNNEL_PLUS_PORT = 63
        private const val DNS_TUNNEL_TIMEOUT = 60

        private const val DNS_DAEMON_GLOBAL_PLUS_PORT = 53
        private const val DNS_DAEMON_SERVER_PLUS_PORT = 63
    }
}
