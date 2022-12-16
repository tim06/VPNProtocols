plugins {
    id("vpnprotocols.android.library")
}

android {
    namespace = "com.tim.shadowsocksr"
}

dependencies {
    api(libs.tim.base)
    api(libs.tim.notification)
}