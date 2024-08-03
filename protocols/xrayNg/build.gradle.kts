plugins {
    id("vpnprotocols.android.library")
}

android {
    namespace = "com.tim.vpn.xrayNg"
}

dependencies {
    compileOnly(files("libs/libv2ray.aar"))
    compileOnly(libs.androidx.datastore)

    api(libs.tim.base)
    api(libs.tim.state)
    api(libs.tim.notification)

    implementation(libs.kotlin.coroutines.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecyclektx)
}