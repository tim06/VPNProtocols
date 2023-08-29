package com.tim.vpnprotocols.view.openvpn

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.configuration.OpenVPNConfig
import com.tim.openvpn.connection.OpenVPNVpnConnection
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
        OpenVPNVpnConnection(
            context = requireContext(),
            stateListener = ::updateConnectionState
        )
    }

    private val vpnPermission = registerForActivityResult(VpnActivityResultContract()) {
        if (it) {
            vpnConnection.start(OpenVPNConfig(configuration = configuration))
        }
    }

    private val layoutBinding: OpenvpnFragmentLayoutBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        }
    }

    private fun updateConnectionState(state: ConnectionState) {
        layoutBinding.stateTextView.text = state.name
    }
}

val configuration = "client\n" +
        "nobind\n" +
        "dev tun\n" +
        "remote-cert-tls server\n" +
        "proto tcp\n" +
        "remote 1.5.8.1 403\n" +
        "auth-user-pass\n" +
        "<key>\n" +
        "-----BEGIN PRIVATE KEY-----\n" +
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC/81Qn1YuRpVZj\n" +
        "9eBlkeLfdk0NDfTnSVJad9bQSj5uMBxhJ1UQvQOciwXZyH8/3uzW7b8ELnevIQHg\n" +
        "mcHDNN0nLwYiTI97RH7YVFIgcljivjsrUretyMDIReFxpH5fv+tDNTh1C46gFJTw\n" +
        "92jxYtKmb2AWfM5y4ROwOInFx7y8vjNSmnkCo370iRV5rJOz2C22yUuaBBX270kt\n" +
        "zkCoyXvDONGCotgFfKa2fPHbLFYTVCgipz5PgZre1NEn1i0dHeoRxBQfG4FIPGNS\n" +
        "vFiHzGXDlODVqr7he361IwDoWrFgBwUNF5J36hEvTsmopg95oIM2iAUqxPIxhn1y\n" +
        "qq7GthVbAgMBAAECggEAGhrMvE7JmJOsmuiX+B2Yhf0lP8bA9+lxVpYLXrASKyhw\n" +
        "25zz79RSLD/TRQnZlcVKtB+I2wKPdVT0V+ghQVvW+HASkfyT24GhDJOfHa7fZsAp\n" +
        "9NuJg7a/J8gqf6rizZZ2DOGtMsHRHPU08kvQzQSRvbUnk2IC2P3COzTdNnV9C1/y\n" +
        "4VMsfVtV2OGnI4jMTA0hb10ClIW/GsjTmocHh90dup8exxXd0VMoykVdZW5XVMFH\n" +
        "+D8kUb6Ek8zyJSXaubsxmYKv5rf/JhQSEfWFRZfLZPOX6J89pMlJWnC5sXt/TUZ4\n" +
        "fOkrx+zxTjROp3PQWjWakNVtSrsk5RomfKJJwVem9QKBgQDqIWoWsniMghkIkMfZ\n" +
        "I9ri0fEeJob2sr95XOSPWn86ltrFhrYVM5MYUYt1PalWgrR79h4CXAP7IkuSJquJ\n" +
        "y88a5bkoU0Urze8tQ0uNUzpq0jknq1wp8hCKR7Cx8UdFXgxLX+u4E/AfUe9xwrfe\n" +
        "0P/iJ8IgwKbPVsjZp9gRZJsCDwKBgQDR4UujN0GDLJMew9pIibcyA/jGScAMB+ec\n" +
        "cLUXGNth8GJuDm60eOlAV7KeIrrij9hRExbjM5SS3Ud8ug8VDU33gJgGBc/gB1yY\n" +
        "8ln+6FkZqMztkEIoCZJeKJibMteZT1JU0gIfiF7+ciDj2yfT39ahHLaZP2ZBSxo3\n" +
        "DzQRrWoT9QKBgQDU5jGcFgn8asjsuwqfbzU5EAMbkZkkd4IZj9jeakJLOqYQ++BT\n" +
        "AyT89hnEMJ/tZMlN941uQ9Hy6UqiybsugEABi2eFPcMmhAq7s/fduRLj0+nZIr+Z\n" +
        "/N4BgBMym95dO5oeaEjmiGrPcCg14ARm/tHQCqtCMSz+WUImebPGjR9PDQKBgE/G\n" +
        "0mhc5YMF23ozOfKenkrdpZ9Bg0VPb+NQGBWKdkFZDSEGTWA+IXM6ooNnciASS0gt\n" +
        "+GIuRgg5IiYv1vHKl9s/PwnzBZwDUFg2rqytBskxF4wpbGwpj0BJMBC2F6uHsiTZ\n" +
        "msL/pBQVr5jMwevQRpYBAwtnROgGsxVAqysY/pxNAoGACZrFNipF63ClXrOj3rPW\n" +
        "+FnYZa/XW2APUseMD2sE3aRN/kGAq2/RzcrARuLjJcC2yabJ3Bt0VcRXa35d6K4+\n" +
        "qX3fmUTiX43zUGAWvm+2vSuC4R+dsj5uyrYtZ5NFh+BRUBEcwVYy6IhHSTSECt2h\n" +
        "OKTA4NYkvWchIAGnYSQxeLo=\n" +
        "-----END PRIVATE KEY-----\n" +
        "</key>\n" +
        "<cert>\n" +
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIDUjCCAjqgAwIBAgIQdsaTyAWoXIBa7izqbIL1HzANBgkqhkiG9w0BAQsFADAW\n" +
        "MRQwEgYDVQQDDAtFYXN5LVJTQSBDQTAeFw0yMzA0MTgyMDAxMThaFw0yNTA3MjEy\n" +
        "MDAxMThaMA8xDTALBgNVBAMMBHRlc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw\n" +
        "ggEKAoIBAQC/81Qn1YuRpVZj9eBlkeLfdk0NDfTnSVJad9bQSj5uMBxhJ1UQvQOc\n" +
        "iwXZyH8/3uzW7b8ELnevIQHgmcHDNN0nLwYiTI97RH7YVFIgcljivjsrUretyMDI\n" +
        "ReFxpH5fv+tDNTh1C46gFJTw92jxYtKmb2AWfM5y4ROwOInFx7y8vjNSmnkCo370\n" +
        "iRV5rJOz2C22yUuaBBX270ktzkCoyXvDONGCotgFfKa2fPHbLFYTVCgipz5PgZre\n" +
        "1NEn1i0dHeoRxBQfG4FIPGNSvFiHzGXDlODVqr7he361IwDoWrFgBwUNF5J36hEv\n" +
        "Tsmopg95oIM2iAUqxPIxhn1yqq7GthVbAgMBAAGjgaIwgZ8wCQYDVR0TBAIwADAd\n" +
        "BgNVHQ4EFgQUrLCakqn4h2kXvvRpsxFqRZ1jkq4wUQYDVR0jBEowSIAU9SJbk2ub\n" +
        "7NJDuqWApFo890Qvq2ahGqQYMBYxFDASBgNVBAMMC0Vhc3ktUlNBIENBghRMmhhH\n" +
        "bRLBOV666QGkS/opeMY78jATBgNVHSUEDDAKBggrBgEFBQcDAjALBgNVHQ8EBAMC\n" +
        "B4AwDQYJKoZIhvcNAQELBQADggEBALwY65r2YzFJZbFwUmva/RHhD0S1ouZ6MtNv\n" +
        "ovHNC2p7PfG02nlyc2XIuH2LJLwcyoHFy+AEtOWhKo+bV3k2asisopEMbXFm0hqp\n" +
        "cdPDt17M6vtMMvGJfaMonIMlRCq5++wl6imfzo/Oerfp7oNT9T3XDqtjoCF0+DyI\n" +
        "24qdKVJqbzjcwq5P0lyD9pRmoOp7JXzFp97qWWHbFLnsSEipC9BQDPu91xzdQqI2\n" +
        "iu3QsWGbYx+yLNTfzruc2wA/DkGEvZdtuPa2RlRkqIrtmB2oGBMGnWA/BQPEO4ok\n" +
        "rUkojZedQ/VgDIPuEtsaI1XXjqPrtoYYyDrK9LJN265EQ2VER1w=\n" +
        "-----END CERTIFICATE-----\n" +
        "</cert>\n" +
        "<ca>\n" +
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIDSzCCAjOgAwIBAgIUTJoYR20SwTleuukBpEv6KXjGO/IwDQYJKoZIhvcNAQEL\n" +
        "BQAwFjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0EwHhcNMjMwNDE4MTk0MTI3WhcNMzMw\n" +
        "NDE1MTk0MTI3WjAWMRQwEgYDVQQDDAtFYXN5LVJTQSBDQTCCASIwDQYJKoZIhvcN\n" +
        "AQEBBQADggEPADCCAQoCggEBAM11Q4QSlfEMuOQa6cR9+AIJPiLU2KavFRg49LoD\n" +
        "Yza4I1xB05XG81GkWGzdz1E/wL2C5e9mqerhHxtaJkHBZLysAsmScw9MG4Exq1oK\n" +
        "9F6VjfNktQq2Mp1YMGI9ZJwpMuN1wwUoOCBYciyElwq0HYl0Nvst+Z7XxgOrEIWR\n" +
        "Sg+wK57uW7L6RtR9dOD5qVMhVZbCWVn9nupCpOg5PS0Ysn/69/5m5ey5Pao95mG8\n" +
        "8xZnMTxudZvgclYs/BsHvwKDzMiqlJ/n7N+J0qMZnbgNA8giqVR+di1c3P3Xny7Y\n" +
        "Z2+FY5frpRCIpjo+K7t5yPNPhU/uCL9LmLmYNSCwgT/QprECAwEAAaOBkDCBjTAM\n" +
        "BgNVHRMEBTADAQH/MB0GA1UdDgQWBBT1IluTa5vs0kO6pYCkWjz3RC+rZjBRBgNV\n" +
        "HSMESjBIgBT1IluTa5vs0kO6pYCkWjz3RC+rZqEapBgwFjEUMBIGA1UEAwwLRWFz\n" +
        "eS1SU0EgQ0GCFEyaGEdtEsE5XrrpAaRL+il4xjvyMAsGA1UdDwQEAwIBBjANBgkq\n" +
        "hkiG9w0BAQsFAAOCAQEAupEbeJS5KMlA9JkIaU8voowoOJzvzDYMDikRXofKKrS8\n" +
        "pwbduqeQPIB6mFwpmFl5+OfBFGgjc6kqNOaFjJ4hQkWAq3RuHGrZvMlqcEmerogu\n" +
        "OFMYqyihrw5Mx0H7yLfALrDdCtd+/SaAV8XsWU9plwB+ZS4t9PcHq8eTH/a+dTMa\n" +
        "DukLqKKLWZl1m+uUEajx0qP9dV3Exm+2kM+NGrvoR0P/CEZp/Uq/UIVfhJyOyT7B\n" +
        "tkFaBc537OZ0OWE0fVLefNgoVJfne/RmEikNirJ76VMnkaU/D2IdsMye4xVonZSU\n" +
        "iybHTLTjBN5O+5Hv2mzfmz0I8F6zjSOaZpqdOarjwg==\n" +
        "-----END CERTIFICATE-----\n" +
        "</ca>\n" +
        "key-direction 1\n" +
        "<tls-auth>\n" +
        "#\n" +
        "# 2048 bit OpenVPN static key\n" +
        "#\n" +
        "-----BEGIN OpenVPN Static key V1-----\n" +
        "011d612735696633fdfab2a4effa6db9\n" +
        "404575e168c61fbccf0072360250fc40\n" +
        "53c00a453829455824052f711d57fb89\n" +
        "5bcc598f3acecc60272b88d839f6a970\n" +
        "ed02a91c69448fb927fc83ec15b2989a\n" +
        "7791ef8a5409b98c364914b5fabcfd9f\n" +
        "285a87d712e2f4b2849d32adcd8c6ca9\n" +
        "71aea74aaec127bf6acd03b5ed12a4ea\n" +
        "623813a737599552921826e88af5dfaa\n" +
        "a0ffcb3226295675de5a6e46d5c7bc10\n" +
        "30aa2540a9dbe669f947fd7f72d2efeb\n" +
        "a511a7545bf6dd9aa171881b71c7628c\n" +
        "321297efe847c990e032f3f45ec52f52\n" +
        "064b9ef5db43885030e6fd91a39b9790\n" +
        "73c85e5f0b9d5d1a4e70a35963971223\n" +
        "4ebcabe57d0f0b7ef1448e54c1fd7162\n" +
        "-----END OpenVPN Static key V1-----\n" +
        "</tls-auth>\n" +
        "\n" +
        "redirect-gateway def1"
val ovpnConfig1 = OpenVPNConfig(
    name = "OVPN Config",
    host = "5.2.5.1",
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
