plugins {
    id("vpnprotocols.android.library")
}

android {
    namespace = "com.tim.vpn.xrayNg"
}

dependencies {
    compileOnly(files("libs/libv2ray.aar"))

    implementation(libs.tim.base)
    implementation(libs.tim.notification)
    implementation(libs.kotlin.coroutines.core)
    implementation("androidx.appcompat:appcompat:1.7.0")
}