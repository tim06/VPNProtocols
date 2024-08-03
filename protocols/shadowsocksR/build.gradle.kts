plugins {
    id("vpnprotocols.android.library")
}

android {
    namespace = "com.tim.shadowsocksr"
}

dependencies {
    compileOnly(libs.androidx.datastore)

    api(libs.tim.base)
    api(libs.tim.notification)

    implementation(libs.androidx.lifecyclektx)
    implementation(libs.kotlin.coroutines.core)
}