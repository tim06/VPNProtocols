package com.tim.vpnprotocols.view.openvpn

import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.configuration.OpenVPNConfig
import com.tim.openvpn.service.OpenVPNService
import com.tim.shadowsocksr.service.ShadowsocksRService
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.OpenvpnFragmentLayoutBinding
import com.tim.vpnprotocols.util.initStateListener
import com.tim.vpnprotocols.view.base.BaseVpnFragment

/**
 * OpenVPN implementation
 */
class OpenvpnFragment : BaseVpnFragment(R.layout.openvpn_fragment_layout) {

    private val layoutBinding: OpenvpnFragmentLayoutBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initStateListener(OpenVPNService::class.java.simpleName, ::updateState)
        layoutBinding.apply {
            startButton.setOnClickListener { start() }
            stopButton.setOnClickListener { OpenVPNService.stopService(requireContext())}
        }
    }

    override fun launch() {
        OpenVPNService.startService(
            context = requireContext(),
            config = OpenVPNConfig(configuration = configuration)
        )
    }

    private fun updateState(connectionState: ConnectionState) {
        layoutBinding.stateTextView.text = connectionState.name
    }
}

val configuration = "client\n" +
        "proto udp\n" +
        "explicit-exit-notify\n" +
        "remote 77.221.155.220 1194\n" +
        "dev tun\n" +
        "resolv-retry infinite\n" +
        "nobind\n" +
        "persist-key\n" +
        "persist-tun\n" +
        "remote-cert-tls server\n" +
        "verify-x509-name server_sQBLj0hRJgC3iA81 name\n" +
        "auth SHA256\n" +
        "auth-nocache\n" +
        "cipher AES-128-GCM\n" +
        "tls-client\n" +
        "tls-version-min 1.2\n" +
        "tls-cipher TLS-ECDHE-ECDSA-WITH-AES-128-GCM-SHA256\n" +
        "ignore-unknown-option block-outside-dns\n" +
        "setenv opt block-outside-dns # Prevent Windows 10 DNS leak\n" +
        "verb 3\n" +
        "<ca>\n" +
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIB2DCCAX2gAwIBAgIUVMh/G5x/yVGZrw8x/oydM0q9SHwwCgYIKoZIzj0EAwIw\n" +
        "HjEcMBoGA1UEAwwTY25faVJtMEx0SGZZT1llZUtnejAeFw0yNDA4MDMwNzU0MzZa\n" +
        "Fw0zNDA4MDEwNzU0MzZaMB4xHDAaBgNVBAMME2NuX2lSbTBMdEhmWU9ZZWVLZ3ow\n" +
        "WTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQpATAag9gkJJWTYU4ap3SRfGCRckE6\n" +
        "Pnz3i02kalf+BK4nSmy6MezWIgH90S+L3rGCGwIlZmbempbSp+KaiI9uo4GYMIGV\n" +
        "MAwGA1UdEwQFMAMBAf8wHQYDVR0OBBYEFDr9sO7L0myL5O/uLiSV+V4ug0uZMFkG\n" +
        "A1UdIwRSMFCAFDr9sO7L0myL5O/uLiSV+V4ug0uZoSKkIDAeMRwwGgYDVQQDDBNj\n" +
        "bl9pUm0wTHRIZllPWWVlS2d6ghRUyH8bnH/JUZmvDzH+jJ0zSr1IfDALBgNVHQ8E\n" +
        "BAMCAQYwCgYIKoZIzj0EAwIDSQAwRgIhAME8lX3j6kjNc4UTC/i8qSoRFcJSACvm\n" +
        "MSaCG9+nd6zCAiEAlNpTQlkg+uWfC8rgRTteN1LXj1q8FbaEFCGxIya0Byk=\n" +
        "-----END CERTIFICATE-----\n" +
        "</ca>\n" +
        "<cert>\n" +
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIB2DCCAX6gAwIBAgIQUqJCQhuTlErhb82imPA0BjAKBggqhkjOPQQDAjAeMRww\n" +
        "GgYDVQQDDBNjbl9pUm0wTHRIZllPWWVlS2d6MB4XDTI0MDgwMzA3NTQzN1oXDTM0\n" +
        "MDgwMTA3NTQzN1owETEPMA0GA1UEAwwGY2xpZW50MFkwEwYHKoZIzj0CAQYIKoZI\n" +
        "zj0DAQcDQgAEsSUHAWvUi2FUSVgM3apkxgvaLW9oqv4p+FAVy33T9D0JP9mBkUDr\n" +
        "c7dtzjOypb3oXj60KP5hPOq85zGTYZi4uaOBqjCBpzAJBgNVHRMEAjAAMB0GA1Ud\n" +
        "DgQWBBRHjDHx2OlkCOsKcf8F2HeTv0VtnDBZBgNVHSMEUjBQgBQ6/bDuy9Jsi+Tv\n" +
        "7i4klfleLoNLmaEipCAwHjEcMBoGA1UEAwwTY25faVJtMEx0SGZZT1llZUtneoIU\n" +
        "VMh/G5x/yVGZrw8x/oydM0q9SHwwEwYDVR0lBAwwCgYIKwYBBQUHAwIwCwYDVR0P\n" +
        "BAQDAgeAMAoGCCqGSM49BAMCA0gAMEUCIQDiM8baIsGLd7yCTyODs3xlfNjh1JzY\n" +
        "WnWHt9XFDYhokwIgYSHFERSN16FL0dFH4DSpxNiAo0gkyiusj2t2B2NvXB8=\n" +
        "-----END CERTIFICATE-----\n" +
        "</cert>\n" +
        "<key>\n" +
        "-----BEGIN PRIVATE KEY-----\n" +
        "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQg6/jsEaOyPZQdCfz4\n" +
        "S7Ae0ASb4Mtq4ECET4TO5K49O46hRANCAASxJQcBa9SLYVRJWAzdqmTGC9otb2iq\n" +
        "/in4UBXLfdP0PQk/2YGRQOtzt23OM7KlvehePrQo/mE86rznMZNhmLi5\n" +
        "-----END PRIVATE KEY-----\n" +
        "</key>\n" +
        "<tls-crypt>\n" +
        "#\n" +
        "# 2048 bit OpenVPN static key\n" +
        "#\n" +
        "-----BEGIN OpenVPN Static key V1-----\n" +
        "a53c0647da734209da30a17078f257ef\n" +
        "9d141bda2be3aa7e195a79a85600bdca\n" +
        "49d663ea5c0bf398cb6074ac82b5c63d\n" +
        "ae313642d139e069221d788ac56a37a2\n" +
        "846216d0d6628d02950a4198a299f162\n" +
        "22d18abc0dabade9d102a60771f4fbe8\n" +
        "38c7e71fa89a2d75d968e0777d2ac07c\n" +
        "0d246adc900ced2ba9a89a1eee0e242c\n" +
        "cad877e787ecf0ecf130a61951bbbd4d\n" +
        "6c608b3b291b9db32489fe26936a65e9\n" +
        "926802997772db5c5cca38886f4cd0bf\n" +
        "56bbeba750e34371813be2e08c2151c8\n" +
        "68428bd678fd37fe47b1db3b58f5e91e\n" +
        "a42090755b1418a13d5131780d886418\n" +
        "cfdb2b08241c95a07fed11dd6c69fad4\n" +
        "acec4543d357478402f6df9f4da96100\n" +
        "-----END OpenVPN Static key V1-----\n" +
        "</tls-crypt>"
