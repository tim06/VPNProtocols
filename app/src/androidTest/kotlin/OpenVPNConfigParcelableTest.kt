import android.os.Parcel
import com.tim.openvpn.OpenVPNConfig
import kotlinx.parcelize.parcelableCreator
import org.junit.Test

/**
 * @Author: Тимур Ходжатов
 */
class OpenVPNConfigParcelableTest {
    @Test
    fun openvpn_config_parcelable_test() {
        val originalConfig = OpenVPNConfig(
            name = "Test",
            host = "1.2.3.4",
            port = 443,
            type = "test_type",
            cipher = "test_cipher",
            auth = "test_auth",
            ca = "test_ca",
            key = "test_key",
            cert = "test_cert",
            tlsCrypt = "test_tls"
        )
        // Marshall
        val originalParcel = Parcel.obtain()
        originalConfig.writeToParcel(originalParcel, 0)
        val byteArray = originalParcel.marshall()
        originalParcel.recycle()

        // String with ByteArray
        val encodedString = byteArray.contentToString()

        // ByteArray from String
        val filtered = encodedString.dropWhile { it == Char(91) || it == Char(93) }
        val bytes = filtered.split(",")
        val result = ByteArray(bytes.size) {
            bytes[it].trim().toByteOrNull() ?: -1
        }

        // Unmarshall
        val resultParcel = Parcel.obtain()
        resultParcel.unmarshall(result, 0, result.size)
        resultParcel.setDataPosition(0)
        val userDtoFromParcel = parcelableCreator<OpenVPNConfig>().createFromParcel(resultParcel)
        resultParcel.recycle()

        assert(originalConfig == userDtoFromParcel)
    }
}