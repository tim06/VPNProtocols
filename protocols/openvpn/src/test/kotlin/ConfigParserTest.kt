import com.tim.openvpn.OpenVPNConfigParser
import org.junit.Test

/**
 * @Author: Тимур Ходжатов
 */
class ConfigParserTest {

    private val ca = "<ca>\n" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIDSzCCAjOgAwIBAgIUOeYMltmx8IsRiVH0mP3fUApi4t8wDQYJKoZIhvcNAQEL\n" +
            "BQAwFjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0EwHhcNMjIwODEwMDYwOTUwWhcNMzIw\n" +
            "ODA3MDYwOTUwWjAWMRQwEgYDVQQDDAtFYXN5LVJTQSBDQTCCASIwDQYJKoZIhvcN\n" +
            "AQEBBQADggEPADCCAQoCggEBAKS3Vq/7NXXXeBdNKV9QRYtVohUtfN0B/qDnItcH\n" +
            "n9NUymONBphIaKY14lh5XeXQK/NrcgE7Yzc2aTqGj92N8aNj3OlA10lg46j9vzY2\n" +
            "UyDMB9G4yJe852QzezIqvyU1xGalL4JJ/jS8KA5lafZSclSAHmNOr+3ghNNOJpw5\n" +
            "yC8aS2/WRFfM3P0zpy++dfwDYadLyYu30iI2YNUCHaySx7O0LsjSm2MBVXLYovAR\n" +
            "dZZ6RPlqT3Z7wfP00gimtIH3Q/iCQxBcKl9XQgj8lGNMKy85EUOhWHJeklBuuJZR\n" +
            "IQI32euUX60cEfOSyqhmMWFwQxgah/lTxpS/jRrDapc+Yv0CAwEAAaOBkDCBjTAM\n" +
            "BgNVHRMEBTADAQH/MB0GA1UdDgQWBBSbN1bsIV9nL+AxUukF+HL2D2bx2zBRBgNV\n" +
            "HSMESjBIgBSbN1bsIV9nL+AxUukF+HL2D2bx26EapBgwFjEUMBIGA1UEAwwLRWFz\n" +
            "eS1SU0EgQ0GCFDnmDJbZsfCLEYlR9Jj931AKYuLfMAsGA1UdDwQEAwIBBjANBgkq\n" +
            "hkiG9w0BAQsFAAOCAQEAD7JM2vW+lbN/vV+OLky7iQm0gnWXxxxN1DD2xhzIU11c\n" +
            "/xx7/ISSzEB23OS5iXmFaOSEnupuHrKDD+spKN2uUium1+9J3Ii7tfqY4m1IRrzV\n" +
            "pyrrwLimyPaAAQ05/FTYLG1RKSDfoFPzb6e21akR2FncyGz/JHA5PQTiBrBeTBJx\n" +
            "gYcedU3iFm1je9N2fbR7RhYAQYx3Usza8hHFgCs/5TLdTO0YS01vxF8Gwnw3qb8n\n" +
            "Ghen9R0Ejbx+vRcd+IiKZQPBL6FrwLYDK4AftWJwPyaI71e+1F0Zy1P0QaeKRgoC\n" +
            "UqW2SoCFu8OiIl5O9XdGnBS5aACq4fu+315ISdwf3w==\n" +
            "-----END CERTIFICATE-----\n" +
            "</ca>\n"

    private val cert = "<cert>\n" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIDVDCCAjygAwIBAgIQRJqv+NxtybLXM2TWovnJnzANBgkqhkiG9w0BAQsFADAW\n" +
            "MRQwEgYDVQQDDAtFYXN5LVJTQSBDQTAeFw0yMjA4MTAwNjA5NTFaFw0zMjA4MDcw\n" +
            "NjA5NTFaMBExDzANBgNVBAMMBmNsaWVudDCCASIwDQYJKoZIhvcNAQEBBQADggEP\n" +
            "ADCCAQoCggEBANsxKkXrgIC+rPffd4JcnC+/z3RW4VlmcfaXpDQrR9qKr7a6B3u0\n" +
            "SvZ8ZCwxvuHTUt7ZotFvUTOcz2q/0Ucc0VHVUuMks5qPNzHYXAxFpLLzP3/ig7A/\n" +
            "o+/uYRgnpA5QYqhzXzqbzYwks9wGOsMkmAAKXF9o+2mJmCSkWdgvbFr/Twtlaprg\n" +
            "+LecEeGIap49a+9s5ZM+42mpXpYyxwYxDMyJrsR76Umejedgr51DRHL0ZhhXPTzP\n" +
            "ISt2eRSHB4xXvm3SGxUjFR8wiJNZrvmYc8UVYVisceas44HsOOQN4g2AlIkngeoo\n" +
            "Sl1J9JPD66eSdFCFdraoXlEow0vKleET0eMCAwEAAaOBojCBnzAJBgNVHRMEAjAA\n" +
            "MB0GA1UdDgQWBBR3tmgkBBpxSFT5RsxlPCV8G21xoDBRBgNVHSMESjBIgBSbN1bs\n" +
            "IV9nL+AxUukF+HL2D2bx26EapBgwFjEUMBIGA1UEAwwLRWFzeS1SU0EgQ0GCFDnm\n" +
            "DJbZsfCLEYlR9Jj931AKYuLfMBMGA1UdJQQMMAoGCCsGAQUFBwMCMAsGA1UdDwQE\n" +
            "AwIHgDANBgkqhkiG9w0BAQsFAAOCAQEAlujFdgdM4k2eQHEkoHxljdwabptpHx44\n" +
            "oGbS86Cj0X57Kes0BAruIDlgFyDbrHPvXsrB4Hw62PbK6CYhfyFgQ5n45lD7piV2\n" +
            "pu7nwXSndDYuapgUiAAPbLb04Ri0LsqjuqbBGUOMn0RbgXF9u79DlhY8XMJdVRjG\n" +
            "W9+RHhA9+q6Oc2RIQCx1Jk8mivfcsRC7AKBR3xeQ3vN5pG1d3ByJP7y7FiBWhRXD\n" +
            "rsuHemBllAZDdJH9Qil28uQmaGTNXmFxX48icGF5XBByPJIaruRx8TziRO2kt3jV\n" +
            "0+FC3DkUNJ+Xdutc6Mf6UUCjZ4P0waejh4en2VyUByAMtx7ud0tixw==\n" +
            "-----END CERTIFICATE-----\n" +
            "</cert>\n"

    private val key = "<key>\n" +
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDbMSpF64CAvqz3\n" +
            "33eCXJwvv890VuFZZnH2l6Q0K0faiq+2ugd7tEr2fGQsMb7h01Le2aLRb1EznM9q\n" +
            "v9FHHNFR1VLjJLOajzcx2FwMRaSy8z9/4oOwP6Pv7mEYJ6QOUGKoc186m82MJLPc\n" +
            "BjrDJJgAClxfaPtpiZgkpFnYL2xa/08LZWqa4Pi3nBHhiGqePWvvbOWTPuNpqV6W\n" +
            "MscGMQzMia7Ee+lJno3nYK+dQ0Ry9GYYVz08zyErdnkUhweMV75t0hsVIxUfMIiT\n" +
            "Wa75mHPFFWFYrHHmrOOB7DjkDeINgJSJJ4HqKEpdSfSTw+unknRQhXa2qF5RKMNL\n" +
            "ypXhE9HjAgMBAAECggEBAMN0rAV9YzFq1kKSerc8beoGWK+daYWW3LjPsBT1IzW1\n" +
            "xcpjSJj+xHnXolvmYBhvoM4ntBCd+wLTKgI+2hdXkzAt0aPK7n08iDNig3JOB/er\n" +
            "AUzAaD2aAk3Je7tWXeyFsfQULS7OEBSQBfKFe5DX6NLgak6KBuatU4YZKCSm5TCe\n" +
            "IqqVfcAsE8HDBdJFzbHbIFZc0SUWONodIXhkSPOqV4lQbBg2HTSLzyAWtSLsahwT\n" +
            "dxFY7pUvupQP8mTj3CciVji1uRAWXHSV8Tz1/X0JCmB/HEI9Z3RLa5LzxdTDhOh5\n" +
            "aqXJagL5WEwSJ+dB59vvEYGX5SX4zS95dck8FJhliIECgYEA+B4dORW9vRAV7C7R\n" +
            "oIe8oIyiTF4m9XZxxPpxegcUjDI+aAE1CwTcSvI5yshKvYBR6HW8OwdpyujHQAnV\n" +
            "yjvHOJCv1z2hEIccyf+uZKRcFpqscbhpssNrO/XsAm/PCoI6LlE9NO18m3QU41Kb\n" +
            "MD6VaFLlXMHuuhAAkHydLqld4isCgYEA4ifOPLWhlhQh2gyEZUQykMj1Y9OISolA\n" +
            "YWhsu4bt6gka2zVfPEgDdpib4nEWU/S2hb6SLX8sAVTBoMCUtW8hRKruz9qqFsTs\n" +
            "bx5GAEVMCAkuZBKsd+t880j/pgiMP3fwEi9qq2mk8Lxf6M6XOzEY4iULV9Gem8XF\n" +
            "dIGCtgNYSykCgYBP83Q7NtRDHfHSf2uxLMwNeAO8VAJ9ykLy1DjIt4tZeb8+SFVN\n" +
            "Ta2mwNL6kvvygGzkWTvkUYk3hzbdXc30MrsJcSYYV5WU+9S9CpSpfYTnC8RRiLkW\n" +
            "j334flAkoN64ZipVTnxIl9Y1SXyJm87CBwih60RZVWyVrB5icKU9qfnErQKBgC7l\n" +
            "fjjMmUNbzWE/8zxkle+8HFalCMAcEgOg+dsxMai0VqNaP/NqI322S6z7MlwahLbb\n" +
            "O8i/dEqaXsSM6sls0ANDRt5HQ7oQ/85TAsUPmaKY9Mu/q4/6fyCb5JdzclG+90Nu\n" +
            "HbqJ37Aj/+dw359goP5UHDQLvc4jhryQFqDTtL1xAoGAcEobdZWlnSz1RqJVSVtW\n" +
            "fpPvnp94JjNGucAmaOfbU7uHH9Lz1rUFdDQCt+X/WkwhRyH1UgXA4JvdTTWzc/ND\n" +
            "FRkbXTwfRidQTUNdx8rejD6FclSF8+OQjISkX3Sp+tbMUKwhUb75uNTy0GYFFhsC\n" +
            "Vbrup+mRXtPwaLQWqEK8wWo=\n" +
            "-----END PRIVATE KEY-----\n" +
            "</key>\n"

    private val tlsCrypt = "<tls-crypt>\n" +
            "-----BEGIN OpenVPN Static key V1-----\n" +
            "5691df4d3880e2fbbabb6322dc5df2be\n" +
            "619be2f23b055da219a443dc0154a9be\n" +
            "7d1b4627423896b9c5680790634e173f\n" +
            "61aa73ec998562bd7b05c06b5233a6a9\n" +
            "d744c0d15bf947c3a8e302ad0be0bcb9\n" +
            "05b2d1f1a317ddb45503e5155fb69c5e\n" +
            "4c8068f2a927423550a478a9d88ade0b\n" +
            "557bb3f974a6f841d134b387fe5c0b63\n" +
            "f91098d5ebf889d911e59694dbbfd574\n" +
            "4ee584287b01806a9a29e26418c22338\n" +
            "435a1dc478dc6048835a469837b6204e\n" +
            "5e80baff2528e91b11c9e078fe518374\n" +
            "0e931d0cc3df08b4a5e0aa5858855c9b\n" +
            "e9df541b5c2f4d3c028426c75f816895\n" +
            "64cad206681df30ad38aad1196879ad1\n" +
            "754b208b1d07807cbed94b1f1512c98a\n" +
            "-----END OpenVPN Static key V1-----\n" +
            "</tls-crypt>\n"

    private val config = "client\n" +
            "dev tun\n" +
            "proto tcp\n" +
            "remote 12.34.56.78 443\n" +
            "resolv-retry infinite\n" +
            "nobind\n" +
            "persist-key\n" +
            "persist-tun\n" +
            "remote-cert-tls server\n" +
            "auth SHA512\n" +
            "cipher AES-256-CBC\n" +
            "ignore-unknown-option block-outside-dns\n" +
            "block-outside-dns\n" +
            "verb 3\n" +
            ca +
            cert +
            key +
            tlsCrypt

    @Test
    fun parseOpenVPNConfigTest() {
        val config = OpenVPNConfigParser().configFromLines(config.lines())

        assert(config.host == "12.34.56.78")
        assert(config.port == 443)
        assert(config.auth == "SHA512")
        assert(config.cipher == "AES-256-CBC")


        assert(config.ca?.trim() == ca.trim())
        assert(config.cert?.trim() == cert.trim())
        assert(config.key?.trim() == key.trim())
        assert(config.tlsCrypt?.trim() == tlsCrypt.trim())
    }
}