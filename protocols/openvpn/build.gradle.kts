plugins {
    id("vpnprotocols.android.library")
}

android {
    namespace = "com.tim.openvpn"
}

dependencies {
    testImplementation(libs.test.junit)

    api(libs.tim.base)
    api(libs.tim.notification)
}
