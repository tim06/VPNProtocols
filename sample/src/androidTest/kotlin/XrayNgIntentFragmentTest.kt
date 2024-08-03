import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.uiautomator.BySelectorHack
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.tim.basevpn.state.ConnectionState
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.view.MainViewActivity
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.junit.Rule
import org.junit.Test

class XrayNgIntentFragmentTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainViewActivity>()

    @Test
    fun test() = run {
        step("Navigate to xRayNg fragment") {
            val xrayNgButton = KButton { withId(R.id.xrayNgButton) }
            xrayNgButton.click()
            val stateTextView = KTextView { withId(R.id.stateTextView) }
            stateTextView.isVisible()
            stateTextView.hasText(ConnectionState.DISCONNECTED.name)
        }
        step("Start xrayNgConnection") {
            val startButton = KButton { withId(R.id.startButton) }
            startButton.isVisible()
            startButton.click()
        }
        step("Accept permission") {
            val selectorRu = BySelectorHack.newInstance { text("ОК") }
            val selectorEn = BySelectorHack.newInstance { text("OK") }
            val obj = if (device.uiDevice.hasObject(selectorRu)) {
                device.uiDevice.findObject(selectorRu)
            } else {
                device.uiDevice.findObject(selectorEn)
            }
            obj?.click()
        }
        step("Check connection state") {
            val stateTextView = KTextView { withId(R.id.stateTextView) }
            flakySafely { stateTextView.hasText(ConnectionState.CONNECTED.name) }
        }
    }
}