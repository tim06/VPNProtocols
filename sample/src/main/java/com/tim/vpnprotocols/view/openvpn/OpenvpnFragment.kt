package com.tim.vpnprotocols.view.openvpn

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.OpenVPNConnection
import com.tim.openvpn.configuration.OpenVPNConfig
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.OpenvpnFragmentLayoutBinding
import com.tim.vpnprotocols.view.shadowsocksr.VpnActivityResultContract

/**
 * OpenVPN implementation
 *
 * @Author: Timur Hojatov
 */
class OpenvpnFragment : Fragment(R.layout.openvpn_fragment_layout) {

    private val vpnConnection by lazy {
        OpenVPNConnection(
            context = requireContext(),
            stateListener = ::updateConnectionState
        )
    }

    private val vpnPermission = registerForActivityResult(VpnActivityResultContract()) {
        if (it) {
            vpnConnection.start(ovpnConfig1)
        }
    }

    private val layoutBinding: OpenvpnFragmentLayoutBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vpnConnection.bindService(true)
        layoutBinding.apply {
            startButton.setOnClickListener {
                vpnPermission.launch(Unit)
            }
            stopButton.setOnClickListener {
                vpnConnection.stop()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnConnection.apply {
            stopServiceIfNeed()
            clear()
        }
    }

    private fun updateConnectionState(state: ConnectionState) {
        layoutBinding.stateTextView.text = state.name
    }
}

val configuration = "client\n" +
        "dev tun\n" +
        "proto tcp\n" +
        "remote us12.freeconnect.link 443\n" +
        "resolv-retry infinite\n" +
        "nobind\n" +
        "persist-key\n" +
        "persist-tun\n" +
        "remote-cert-tls server\n" +
        "tls-cipher TLS_CHACHA20_POLY1305_SHA256:TLS-DHE-RSA-WITH-AES-256-GCM-SHA384:TLS-DHE-RSA-WITH-AES-256-CBC-SHA:TLS-DHE-RSA-WITH-AES-128-GCM-SHA256:TLS-DHE-RSA-WITH-AES-128-CBC-SHA:TLS_AES_256_GCM_SHA384:TLS-RSA-WITH-AES-256-CBC-SHA\n" +
        "verb 5\n" +
        "\n" +
        "<ca>\n" +
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIGMDCCBBigAwIBAgIJAJ/pf2R2mgCMMA0GCSqGSIb3DQEBCwUAMIGjMQswCQYD\n" +
        "VQQGEwJQQTEPMA0GA1UECAwGUGFuYW1hMQ8wDQYDVQQHDAZQYW5hbWExGjAYBgNV\n" +
        "BAoMEUZSRUVWUE5QTEFORVQgTExDMRowGAYDVQQLDBFGUkVFVlBOUExBTkVULkNP\n" +
        "TTEQMA4GA1UEAwwHUm9vdCBDQTEoMCYGCSqGSIb3DQEJARYZc3VwcG9ydEBmcmVl\n" +
        "dnBucGxhbmV0LmNvbTAgFw0yMjEwMDQxNzU3MjZaGA8yMDcyMDkyMTE3NTcyNlow\n" +
        "gaMxCzAJBgNVBAYTAlBBMQ8wDQYDVQQIDAZQYW5hbWExDzANBgNVBAcMBlBhbmFt\n" +
        "YTEaMBgGA1UECgwRRlJFRVZQTlBMQU5FVCBMTEMxGjAYBgNVBAsMEUZSRUVWUE5Q\n" +
        "TEFORVQuQ09NMRAwDgYDVQQDDAdSb290IENBMSgwJgYJKoZIhvcNAQkBFhlzdXBw\n" +
        "b3J0QGZyZWV2cG5wbGFuZXQuY29tMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIIC\n" +
        "CgKCAgEA2kwXegCAWFsY9mcLvlMmbT0OB634ga6qX6JzFrEejXRNQo+YnRmfyavS\n" +
        "IQ15TEo8w4GhLsWVAMAXq+lzhIHgWpnvXjRvqKprwG7Q09MkcyYyH389BfKi3yyV\n" +
        "3Kk4JJ55T+CMjORnjrhMJY3kYidJ6SV6BSdFYUg/89slwViI9Jm+ZkyRpwiGPuqB\n" +
        "HdA5rcX7mro8E+dROyTsm/eNWuS02zlZVxwtmkrKq0oQTW4R0Kx+rL4hFHS/LR64\n" +
        "Y6kyPH9rd/LFwrdNUir+Ebk0tnouUnzAqoSIu7ISuvsEJNHZmzbihEyHGzfuuTbQ\n" +
        "VFKxO67ZJnMICcPsRXh/WH0IPU9Igq0vK6pTU0sdYlv4Z2zcKInmeRuOHkFyvBwt\n" +
        "qXGMpX1+B6rFQGeY+TE1QaaCvg4y1isyB15Vju4fZsNKfg3D6ym/6KUNHFyKbgUd\n" +
        "3ThJ8U6yOQd2vfG45fKJY7wsVE0XtF7QGgAQ2yAw+NunIMF6nid+6z5IzVFJrb71\n" +
        "Cbh2p49DSPdsDbbF95qqvlBoQlXS8FyKGT4Z576zDgdjCZWMay5865XsoE2F2vd7\n" +
        "zK0klGK74wqKrBhL35iOffv9c8hJbcyLG5eZW2ZgzN85zciohk5F9HNr1or97uXz\n" +
        "XUtLosAocNqA/Mh2dh3ATG9lDL5lb5ysuWesuMrR1tPSyWHuXK8CAwEAAaNjMGEw\n" +
        "HQYDVR0OBBYEFJ2qSoC+Qkt7eg06Sr1uoyRCAeBwMB8GA1UdIwQYMBaAFJ2qSoC+\n" +
        "Qkt7eg06Sr1uoyRCAeBwMA8GA1UdEwEB/wQFMAMBAf8wDgYDVR0PAQH/BAQDAgGG\n" +
        "MA0GCSqGSIb3DQEBCwUAA4ICAQBZTi8w0oa457vutRcZaGpDiufRqnSkyFIIAf/J\n" +
        "Rh79AQoC/2IvvPB0z1+JEffQWljDOOXPIujm9iwL0n/BRIcT5zBUi7DVrcbCveQl\n" +
        "hdzJ+3DdI0OFMcEFHPOlEgB2XxetvN4GVXUBY6VjkrPIzflMSJjdl264OTORp64X\n" +
        "V6EQy2EkoNdRpR3lYbaJ0Fp+JeXIjJncUxYp+ddS3CFysLU1JtriVys1uNnPTIal\n" +
        "CryXvr9nBO3r2t92v/x7dXHeU05GW6GHUxitbbw0pSaaON8SCfPjALmK6EwhdQY/\n" +
        "jaCYkkgm9s/xmFkYy5Da0ciGg/qaunkqEG6K8pRo7WoNavOB98nx8XsU/8Pb9dNP\n" +
        "Jo6S8SSXuB6kQBg6MayXXuIVL+V5RI/ao127b/WoF090DLR0ymMT8dt9VSKBaE6O\n" +
        "PmUUD1BFoDTJY2Z8d9Da8qW//EvIeRRO42Jb20XN6jtsvj8avFZt66yywxCw3VXY\n" +
        "d47fyOskFiTZQI+kyQPzfhreTLO8E90OxQ/m0CGx2jOqEF6Q2kNrdnu/1fxbwsA1\n" +
        "LmHlXS1lneRD1Co2YlC4JsaSgdhyLICOdE4gtGHtjSRgMaeicK4F3pcII/MkOL04\n" +
        "IkQrgZ3o1M3ztC4BxF53EfIyi1vO22yvb3Lh+qVKRFzKgTQ5gcVVHjPzZKhl3DtT\n" +
        "/IQa0w==\n" +
        "-----END CERTIFICATE-----\n" +
        "</ca>\n" +
        "\n" +
        "<cert>\n" +
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIFJTCCAw2gAwIBAgICAJYwDQYJKoZIhvcNAQELBQAwgaMxCzAJBgNVBAYTAlBB\n" +
        "MQ8wDQYDVQQIDAZQYW5hbWExDzANBgNVBAcMBlBhbmFtYTEaMBgGA1UECgwRRlJF\n" +
        "RVZQTlBMQU5FVCBMTEMxGjAYBgNVBAsMEUZSRUVWUE5QTEFORVQuQ09NMRAwDgYD\n" +
        "VQQDDAdSb290IENBMSgwJgYJKoZIhvcNAQkBFhlzdXBwb3J0QGZyZWV2cG5wbGFu\n" +
        "ZXQuY29tMCAXDTIyMTEyMTIwNDY1MVoYDzIwNTIxMTEzMjA0NjUxWjCBrzELMAkG\n" +
        "A1UEBhMCUEExDzANBgNVBAgMBlBhbmFtYTEPMA0GA1UEBwwGUGFuYW1hMRYwFAYD\n" +
        "VQQKDA1GUkVFVlBOUExBTkVUMSQwIgYDVQQLDBtGUkVFVlBOUExBTkVUIEF1dGhv\n" +
        "cml6YXRpb24xGTAXBgNVBAMMEDIwZjE5ZDI1NDllOTcwYWUxJTAjBgkqhkiG9w0B\n" +
        "CQEWFmZyZWVAZnJlZXZwbnBsYW5ldC5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IB\n" +
        "DwAwggEKAoIBAQDWPfEjlVafITlv6Gb21Zg34ocw7nxFg6zYRXCEq7n/KjKo6pJB\n" +
        "BAbyOjXINdss4CywFABsB4WQrwZRlVTrXc2+d9kHr08/2fvSa0FI0PB8edDE1bWR\n" +
        "OX8GEtXQndiF/nW5OMYyEZGEMKM8IOXX/koNCEWNaZk5LMoQ76VdYW4bus04XEMq\n" +
        "DqSuAvEedf1EidOkqJmd10LfVHlYTGGMnD2Ris2hvHZb92qd86QfMI0erP6DM0jn\n" +
        "5b39sCPhQ8+5gjftDYWb66+QsAM1BBxp5Q4gnZvagF8Av1tBtA1JTNi54+gy+zz9\n" +
        "R6E3HVOEZ6Xviq57vY2KMcnB5v8aQiEZXhRnAgMBAAGjUzBRMB0GA1UdDgQWBBQ2\n" +
        "nD6WdxqQZdjtDPD40JQBrVwhjjAfBgNVHSMEGDAWgBSdqkqAvkJLe3oNOkq9bqMk\n" +
        "QgHgcDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4ICAQBTJqaO8RJl\n" +
        "xttkOUNgV5EgbBeWkeFpqAy0xET1JScwAuJtLSxIjw7PNH9JUOSeUvJEXJAw7YaO\n" +
        "K2c2h6NcfPWz6Rmfy3zUFB41CgbMOl/gI2dYam5Xhwt7lcuiqiMjXnHTF4eQ80bS\n" +
        "b+s8y+JpaWmoGTIWaTTnjdcoE6oegFSICeAlqt5ttv32SmxZxUl6E81h9cy9XwaB\n" +
        "CowgxA68NolWBb4/P5GENHjsuRSx6ToHNgOHIuLDf2NA4uF/gViaB69fwsydm83e\n" +
        "5A1cA1vqerqXBFOYmgWzvsZu+8IoOwERC1bcDiA4KL2ih6dyieE7vuf7V7wSMlGA\n" +
        "x56Pu+5KYmqEdLbgn0RWVe2RWOr62C07CZdCt5e9q/ADxNmurDVkh2T6GCfPSUJP\n" +
        "ydUwCHDG+aCxFpdLrz+zC8JSKjm7XvHxeIwwPPVQSPGnERKF7m6kq/2KyBMocGPx\n" +
        "l52SFLYDb5tpF5cRHsu7Ew8BZZrX7RwbXc1cXKGcn5ApYLJ5Yp/4aNO4xsgKfnAy\n" +
        "hbqksuv/KN3zqd38bjM3lutweuBVSHal1tDMWex4BQRXiMGf8NIFuVxg9P7DrspS\n" +
        "TR10pgnuD2dPbGk50fJV0i0sVlhCxd4fOIJTqMErVzrDOirTSRATnvb1sSiuG711\n" +
        "7exfThMgVwGRxmC/GQYq1pNwr57NvimGGw==\n" +
        "-----END CERTIFICATE-----\n" +
        "\n" +
        "</cert>\n" +
        "\n" +
        "<key>\n" +
        "-----BEGIN PRIVATE KEY-----\n" +
        "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDWPfEjlVafITlv\n" +
        "6Gb21Zg34ocw7nxFg6zYRXCEq7n/KjKo6pJBBAbyOjXINdss4CywFABsB4WQrwZR\n" +
        "lVTrXc2+d9kHr08/2fvSa0FI0PB8edDE1bWROX8GEtXQndiF/nW5OMYyEZGEMKM8\n" +
        "IOXX/koNCEWNaZk5LMoQ76VdYW4bus04XEMqDqSuAvEedf1EidOkqJmd10LfVHlY\n" +
        "TGGMnD2Ris2hvHZb92qd86QfMI0erP6DM0jn5b39sCPhQ8+5gjftDYWb66+QsAM1\n" +
        "BBxp5Q4gnZvagF8Av1tBtA1JTNi54+gy+zz9R6E3HVOEZ6Xviq57vY2KMcnB5v8a\n" +
        "QiEZXhRnAgMBAAECggEBAJzIR6eaqgFZ9waGNJN+TB7Zj8WnQRo9+kdqyaTNZxfs\n" +
        "cZZb0xvxLoQlRSZ3AyYcB5fzizuDZaMz8pPRjIuR4fb5DcIp8PzxMPATvXrkLJlr\n" +
        "K/Rf0LeuM97l+cLpQIMObGhXu/L4GF18mnhuOsWOOfK/kuah2JeWx2kNdx6XJlTz\n" +
        "UE8lfGVgOzQzFPWuXPn+t4i+b01eTHUOSntbHPtet00bBOq/bJCD/gFyPRnnFxgx\n" +
        "dDfpg4jmU+lbYVrxMQMFEkEZeSc9XeXQTDdL2IFUMDaLHT6Nwo66RSjdlJWlfdHF\n" +
        "1Tw4uaSxtDOBibFwDRL15q0HeBddiL1Ki+WJgHOch4ECgYEA76nQL+W4cpMqVq8r\n" +
        "4yjQNAMARmWU3z1vscbgA9xpFVEI3JLTvHn4lFZB/XXs4qXjwWpGjieKh0mqQ8zt\n" +
        "5Yte8fA4YenFsOVKKsMjb3fcVUs4piyelPRcm6/OQa/Gi+Yf1j3NRdZ8Ylv80PQK\n" +
        "Vfgm+hcgegfdFIprwuAPOYnQ7JkCgYEA5NiE7NHTx6rN9tacdd+fkSiFpo3j4R3N\n" +
        "CiHP/Q+pjmsG0fugkB7M7Q3UwpD8sH8lUukyGi2UJx5a92dgvXLQMHm3ffbzG+dR\n" +
        "mAJhuQuWcUrvl/tI/qIzwU3QzKZo3BjhoBpBiNKcI+IOjB0EM2Hqa+ligQvuIvJJ\n" +
        "RGz8V3+uqP8CgYEAuOl3hI0ku9oKL6mmHfVOduLd4nLb40ZemHbOPoxf05+bS7xJ\n" +
        "e8FL5v+KmMnUxKajzIZ3+5RMkMdohDloT9QxpE/o4Lri2fJ/P/EhtQ5wxKpuAeCp\n" +
        "VloX3dNOO2YotaYPaFh0ue6cN7Oro3i3RM9bG1ieqSjKDeAi9s+zT3yi4vkCgYEA\n" +
        "rnFSGkf0XL6Z1DBSAhRdyUQPZR/B9qdF1hRiPHBE+rgg8c4S3elsosMpTMtCo7O4\n" +
        "GtSCCax2w78C7paSCrfLdThDJDUrqjiRXQNrxKfNnTzXGI5HXTr9oruTj3zGcAhT\n" +
        "bTy2efq5ZSz7k9jGi/h2vp2gJeiXNXJlYVCGpphA5rcCgYANcl80M+psaJIyYE5O\n" +
        "oW1M50dTVJn0EkEbSPbewtawH0AnJN0WYCpGkB5aUtCO24C2Xue0UaXMLDAb6CZT\n" +
        "uGH5sbzfWMwk9zU/1Y4FpsYs7GFsgB70gQSGSzD+V9hF/XRNhZqdvCup4nkWcPjz\n" +
        "3jrTdSYkAnLceoI92gK8+nSUzA==\n" +
        "-----END PRIVATE KEY-----\n" +
        "\n" +
        "</key>\n" +
        "\n" +
        "<tls-crypt>\n" +
        "-----BEGIN OpenVPN Static key V1-----\n" +
        "f2a6a6bdd71f2bd3dc4a4c03745ddc1c\n" +
        "2b5dcfc4e2fe38e5ba646c252f7a2db3\n" +
        "dbf5d15da101f2ab30463f2eef3f4ff3\n" +
        "1fed800f38fc25462ff85a630962eccf\n" +
        "42ec68f97c91995c5443e99a82e6671c\n" +
        "7139a0deff0b8b4655c545d7c55f48b5\n" +
        "c817410da9081dd55488f56ca7bc771c\n" +
        "a8662b13ee69e4791029f3d20e1f79de\n" +
        "255329b922b0dca06b241c795c94fb17\n" +
        "ef8e9d309d78ee8271d791d540604d3d\n" +
        "77ad38f34331c59a4d2ac17976c43287\n" +
        "64fe9b45384ed611ffb99e2b6425d245\n" +
        "b89d2a92c80d8252953a34be5ceb1104\n" +
        "b9cd8c51217a506fd7cf098326417a17\n" +
        "14400ecacb9bcddccd7cf6111615051c\n" +
        "c3abcb7c515cdd302b75bcafa8e6ed28\n" +
        "-----END OpenVPN Static key V1-----\n" +
        "</tls-crypt>"
val ovpnConfig1 = OpenVPNConfig(
    name = "OVPN Config",
    host = "5.23.52.159",
    port = 443,
    type = "tcp-client",
    cipher = "AES-256-CBC",
    auth = "SHA512",
    ca = "<ca>\n" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIDSzCCAjOgAwIBAgIUEzOio8W5x4FAok5U2lK0dUlTdPgwDQYJKoZIhvcNAQEL\n" +
            "BQAwFjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0EwHhcNMjMwNjE0MTQzMTMxWhcNMzMw\n" +
            "NjExMTQzMTMxWjAWMRQwEgYDVQQDDAtFYXN5LVJTQSBDQTCCASIwDQYJKoZIhvcN\n" +
            "AQEBBQADggEPADCCAQoCggEBANIr7XwSI/vdsH+dLVlRsgAqnsiWtbVRYLWI/eiY\n" +
            "WPORcksySwrTaOhYxT60Doa3JwUtzs5VB259T1/kB7zamTofA3RuRiiKZW3Lw5eG\n" +
            "YaDDol75oV8e/RI/n0m9zW5uLQl3fcFyNk+/iwRu81DZ5t4+HcQjv78XEZU+y3aU\n" +
            "mCdX6GqufXlts0FsNKywVAaqPhJD0B4KTrga96W3R8aMdlNwB9tz5fJ4YMtP6hrY\n" +
            "1zem0EXxx1L1PgpKtiIs7o2NQbs1dwrXNlsXD5Zu9gBxkiJ1XgjEJs/VywVarhol\n" +
            "BT2g9Jtj/LnIjkPg1zjERmiwC5TJITxMQzZbAn53UdhsOFcCAwEAAaOBkDCBjTAM\n" +
            "BgNVHRMEBTADAQH/MB0GA1UdDgQWBBQPtUmmTw//xD5xlyuhXDKmCY41kjBRBgNV\n" +
            "HSMESjBIgBQPtUmmTw//xD5xlyuhXDKmCY41kqEapBgwFjEUMBIGA1UEAwwLRWFz\n" +
            "eS1SU0EgQ0GCFBMzoqPFuceBQKJOVNpStHVJU3T4MAsGA1UdDwQEAwIBBjANBgkq\n" +
            "hkiG9w0BAQsFAAOCAQEAIUwH+aAC/x05OWshpXF/8RxiVjL2hOdJ7UG1JyxxxxpB\n" +
            "gQnCD1XhEGPGSXZHapMXHQKkBjZ7NDKkSyztR8z7Ich/EiNpXxczErbmVxYkzmxz\n" +
            "Xhh2mWCMSasjIeKXUcjGv6KXuFJ851+DgjHb156VF4oATIeyVbBkCht3AQ2Vf2Fj\n" +
            "36p6zx5TaQjfCVwbifQO+lZh1/ftVEKqnGXlmbbB/hd8M+v7DrkGOUCaHLbUij3a\n" +
            "73n3mncQ4t9fueglq0146VK9eCpwJmlnN2rXYvCXS6zjz4/archLwz3fyrk0wjyP\n" +
            "+tRwALifKEWQW0OgWyXexsVa4jhGGPq3TUbiMd09IA==\n" +
            "-----END CERTIFICATE-----\n" +
            "</ca>",
    key = "<key>\n" +
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDRgY/SHOmF+uPe\n" +
            "qI0i+bDsntHsAZZtAnQ0rrrKdgSGLt2npvE15Cxe7wfawWmTCRJRF5j9Z0JVilYh\n" +
            "oja/alpr0t76UBtiPZw8YOEoB+U9uTkn/8tO7tiomJCPWDMWmxyE9dS4D4EgKgFR\n" +
            "B0YPaUKwPiTl0X4JYMUnGsXmIMm4SzyfgGvjJadLhEyw0qUDbWE8QeOX4QouHIO9\n" +
            "FBSc0aFpr8xokSRlxoKhHbxTzjeD+p22FogYZEsLmnVDs27IpT/V6fUR9JpnumYn\n" +
            "qkWcaoN87Ai55g8+DL6+Elmj6dLl4mN08j9UiR+6FrRWec0iWqZafFerkAdoBbLP\n" +
            "4qnpsrTTAgMBAAECggEAFLY5UK1ZDMzL4ERLh6ylqA7fc/SIOYinklm/z6oxMk7J\n" +
            "zPw9FnXTSSNaZG8Aj++ypt+tWUfvD+PYNgUCYpQSElzLfHWU73ZlzrZJLBrzt9Fq\n" +
            "7CBmqgwE0U4b5H/0tQRqol5nRDcMLfDZJniQnY9rb18FaM74JdcyRnZoICMXridc\n" +
            "MIHVfxIfxdtg0r8+8LMGTJsGAbU/hfB3GzEbdikEzaAwB0KUqT4Auz8TSKYhUUMN\n" +
            "KFZL3ONZFIv41uaKFh0vAF6B2ZiQZ5y9dQ9mUHEI286fuRi412un/ishY0fyYSt4\n" +
            "Xka+5EI97esKGsUA/tZ6iWaybBSQnVmD2Za3gOEu6QKBgQDkjOtAcctWzN/6Zgtt\n" +
            "vSjWsAtBnO8Lum461cI4AbxfBQu1noo9Gk41MUovQVS1xcxgC9ulGLX/KXX941E+\n" +
            "ZTHpnepfCLJrt9n4hzvzBTjrQWRdHtGYX13P+lD4WxDFiywcB9+mLVEKYPBl3N98\n" +
            "oWIWZhvQls6zE01XGk92dn7bVwKBgQDqqxlgq2lLtubBFF33s49go8obnBD2Yku9\n" +
            "R+Ebf66boDnQjuR9zdWNb9WBd2mBc2iptzYtRkfr32n+Hk3XORUsq2TEsjJWgie+\n" +
            "wNYvgo32ZYMvO3FSFAt0PnnDBYuU/s/68LkYqLwqXMk2+QaMQKpuiC7oQCPilBe7\n" +
            "pBaTXLuA5QKBgA1/4VliH0+WNFiW9G/b73CW32NVwKzhqycgfyNNvvGh26oznhiz\n" +
            "2BC+Q5J8+mLuNv8pVCTxS7axnT2M0ryrv4H8CN7qJZfFOUO7wSe+lDkZGLODYX/t\n" +
            "ih4BvAyfXGM+sDFLqcMoBBRn22s4256B7chC6butXLIpHrVZYX4uy5lrAoGAS+SX\n" +
            "6X6mKBl32VvH5BMLPJrcAz/e8H/rtMRpVeZeRvBzojKETPdjqEFR0HQZCAeWAsVA\n" +
            "3TWMjwkLE2cjj4ZjhbXMehyTUQz446Uj3+ueE+DSo1TbGktnROnroRnLAZ3DD4oI\n" +
            "oqP3bs/hN0kkR/ml5OdgHDaaJG32mvW/Y1srPM0CgYAiRvaj+cw5iU1Xse9IsD4l\n" +
            "VhOcefilJLyaFJagSuPyrEo1Qv+MlK/O7++WhaFdzclS1JaUD46ckZtsTfLXDw4H\n" +
            "+qexQNT9GD2qQXeE9ufeAwQuAXvRtXWenJZMz4NpbJUSTCnacaCEuVUzjfI6Ar64\n" +
            "igh0c8jh1M27bqjr8LWg9A==\n" +
            "-----END PRIVATE KEY-----\n" +
            "</key>",
    cert = "<cert>\n" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIDVTCCAj2gAwIBAgIRALRfsQQ2ayW7nF7kKoCP2CQwDQYJKoZIhvcNAQELBQAw\n" +
            "FjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0EwHhcNMjMwNjE0MTQzMTMxWhcNMzMwNjEx\n" +
            "MTQzMTMxWjARMQ8wDQYDVQQDDAZjbGllbnQwggEiMA0GCSqGSIb3DQEBAQUAA4IB\n" +
            "DwAwggEKAoIBAQDRgY/SHOmF+uPeqI0i+bDsntHsAZZtAnQ0rrrKdgSGLt2npvE1\n" +
            "5Cxe7wfawWmTCRJRF5j9Z0JVilYhoja/alpr0t76UBtiPZw8YOEoB+U9uTkn/8tO\n" +
            "7tiomJCPWDMWmxyE9dS4D4EgKgFRB0YPaUKwPiTl0X4JYMUnGsXmIMm4SzyfgGvj\n" +
            "JadLhEyw0qUDbWE8QeOX4QouHIO9FBSc0aFpr8xokSRlxoKhHbxTzjeD+p22FogY\n" +
            "ZEsLmnVDs27IpT/V6fUR9JpnumYnqkWcaoN87Ai55g8+DL6+Elmj6dLl4mN08j9U\n" +
            "iR+6FrRWec0iWqZafFerkAdoBbLP4qnpsrTTAgMBAAGjgaIwgZ8wCQYDVR0TBAIw\n" +
            "ADAdBgNVHQ4EFgQU9fDdvojICOaSLI2ja05l4d6AzQYwUQYDVR0jBEowSIAUD7VJ\n" +
            "pk8P/8Q+cZcroVwypgmONZKhGqQYMBYxFDASBgNVBAMMC0Vhc3ktUlNBIENBghQT\n" +
            "M6KjxbnHgUCiTlTaUrR1SVN0+DATBgNVHSUEDDAKBggrBgEFBQcDAjALBgNVHQ8E\n" +
            "BAMCB4AwDQYJKoZIhvcNAQELBQADggEBAFdT/w/JcorLizh05UURJIBEbzrwAb1b\n" +
            "7LNAsN6hWaYAi++G6dbfUkpSNYa0l4zf4RJz8BXOQZvtWP4Y5YLVxMVYNZp/Ken/\n" +
            "MzqnCmZAeXxmNZcBESQer0VbatO8l8eBZhDO9TqX+kIHNyv+NpMZ14V2ERXktUwI\n" +
            "J/0XBwSg9JPwS51EIaeNi7YquwIEBCO6J/g2m74DY35H+Ao7frccf5ifTG83jEWP\n" +
            "r5hzPR+pfZz9FBHVFahznerQYjQBF4OSgH7e+vyo4UaOXO+MrRTv/NYRaxnbGyXb\n" +
            "NRVSowaYbQ+bsn+zhknNqf4qEMQjNZNRGrVv1tGwJ3qOKkMYugMzL5o=\n" +
            "-----END CERTIFICATE-----\n" +
            "</cert>",
    tlsCrypt = "<tls-crypt>\n" +
            "-----BEGIN OpenVPN Static key V1-----\n" +
            "1116283ccd5fd53d2bcbd0980fdce893\n" +
            "2fa081140b92600cb2b0310db5977262\n" +
            "1b1d68feba4e010965f2ccd4ba5ba06e\n" +
            "71198ad9ad3ecb5cd0086eb9eae0cfc1\n" +
            "b0d2e81fc9886b281cb7b54a6983a4c8\n" +
            "315b2a898ee9f05fc2cd240928b41368\n" +
            "7ec5c2bfe0cdc92214c8f305a179052a\n" +
            "1257f333ce51b6f199fc57906b80911e\n" +
            "ec898032fd0d1baf041648474ec516aa\n" +
            "54c47ad07118206f4a711bfb6abfc05e\n" +
            "54e133686b3fa16a250acff032f7180f\n" +
            "297cadb3ee8565b9bedaa2d95e736a3f\n" +
            "3ac54632bf849286f532c938bf91b158\n" +
            "9da412427559147de1e8b0bfa049bbeb\n" +
            "ef29f647a17cee5ffacc4285bed5a594\n" +
            "836162b28ff7d35bed971436c4577c03\n" +
            "-----END OpenVPN Static key V1-----\n" +
            "</tls-crypt>",
    configuration = configuration
)
