package com.tim.xtlsr

import com.tim.basevpn.configuration.IVpnConfiguration
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.group.RawUpdater
import kotlinx.parcelize.Parcelize

@Parcelize
data class XTLSRVpnConfig(
    val host: String,
    val publicKey: String,
    val shortId: String,
    val uuid: String
) : IVpnConfiguration {

    fun build(): ProxyEntity? {
        var profile: ProxyEntity? = null
        RawUpdater.parseRaw(
            text = defaultConfig.invoke(
                host,
                publicKey,
                shortId,
                uuid
            )
        )?.forEach { proxy ->
            profile = ProfileManager.createProfile(99L, proxy)
        }
        return profile
    }

    private companion object {
        private val defaultConfig = { host: String,
                                      publicKey: String,
                                      shortId: String,
                                      uuid: String ->
            "{\n" +
                    "  \"dns\": {\n" +
                    "    \"independent_cache\": true,\n" +
                    "    \"rules\": [],\n" +
                    "    \"servers\": [\n" +
                    "      {\n" +
                    "        \"address\": \"https://8.8.8.8/dns-query\",\n" +
                    "        \"address_resolver\": \"dns-direct\",\n" +
                    "        \"strategy\": \"ipv4_only\",\n" +
                    "        \"tag\": \"dns-remote\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"address\": \"https://223.5.5.5/dns-query\",\n" +
                    "        \"address_resolver\": \"dns-local\",\n" +
                    "        \"detour\": \"direct\",\n" +
                    "        \"strategy\": \"ipv4_only\",\n" +
                    "        \"tag\": \"dns-direct\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"address\": \"local\",\n" +
                    "        \"detour\": \"direct\",\n" +
                    "        \"tag\": \"dns-local\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"address\": \"rcode://success\",\n" +
                    "        \"tag\": \"dns-block\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"inbounds\": [\n" +
                    "    {\n" +
                    "      \"listen\": \"127.0.0.1\",\n" +
                    "      \"listen_port\": 6450,\n" +
                    "      \"override_address\": \"8.8.8.8\",\n" +
                    "      \"override_port\": 53,\n" +
                    "      \"tag\": \"dns-in\",\n" +
                    "      \"type\": \"direct\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"domain_strategy\": \"\",\n" +
                    "      \"endpoint_independent_nat\": true,\n" +
                    "      \"inet4_address\": [\n" +
                    "        \"172.19.0.1/28\"\n" +
                    "      ],\n" +
                    "      \"mtu\": 9000,\n" +
                    "      \"sniff\": true,\n" +
                    "      \"sniff_override_destination\": false,\n" +
                    "      \"stack\": \"system\",\n" +
                    "      \"tag\": \"tun-in\",\n" +
                    "      \"type\": \"tun\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"domain_strategy\": \"\",\n" +
                    "      \"listen\": \"127.0.0.1\",\n" +
                    "      \"listen_port\": 2080,\n" +
                    "      \"sniff\": true,\n" +
                    "      \"sniff_override_destination\": false,\n" +
                    "      \"tag\": \"mixed-in\",\n" +
                    "      \"type\": \"mixed\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"log\": {\n" +
                    "    \"level\": \"trace\"\n" +
                    "  },\n" +
                    "  \"outbounds\": [\n" +
                    "    {\n" +
                    "      \"flow\": \"xtls-rprx-vision\",\n" +
                    "      \"packet_encoding\": \"\",\n" +
                    "      \"server\": \"${host}\",\n" +
                    "      \"server_port\": 443,\n" +
                    "      \"tls\": {\n" +
                    "        \"enabled\": true,\n" +
                    "        \"insecure\": false,\n" +
                    "        \"reality\": {\n" +
                    "          \"enabled\": true,\n" +
                    "          \"public_key\": \"${publicKey}\",\n" +
                    "          \"short_id\": \"${shortId}\"\n" +
                    "        },\n" +
                    "        \"server_name\": \"www.microsoft.com\",\n" +
                    "        \"utls\": {\n" +
                    "          \"enabled\": true,\n" +
                    "          \"fingerprint\": \"chrome\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"uuid\": \"${uuid}\",\n" +
                    "      \"type\": \"vless\",\n" +
                    "      \"domain_strategy\": \"\",\n" +
                    "      \"tag\": \"proxy\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"tag\": \"direct\",\n" +
                    "      \"type\": \"direct\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"tag\": \"bypass\",\n" +
                    "      \"type\": \"direct\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"tag\": \"block\",\n" +
                    "      \"type\": \"block\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"tag\": \"dns-out\",\n" +
                    "      \"type\": \"dns\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"route\": {\n" +
                    "    \"auto_detect_interface\": true,\n" +
                    "    \"rules\": [\n" +
                    "      {\n" +
                    "        \"outbound\": \"dns-out\",\n" +
                    "        \"port\": [\n" +
                    "          53\n" +
                    "        ]\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"inbound\": [\n" +
                    "          \"dns-in\"\n" +
                    "        ],\n" +
                    "        \"outbound\": \"dns-out\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"ip_cidr\": [\n" +
                    "          \"224.0.0.0/3\",\n" +
                    "          \"ff00::/8\"\n" +
                    "        ],\n" +
                    "        \"outbound\": \"block\",\n" +
                    "        \"source_ip_cidr\": [\n" +
                    "          \"224.0.0.0/3\",\n" +
                    "          \"ff00::/8\"\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}"
        }
    }
}
