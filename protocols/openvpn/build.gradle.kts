plugins {
    id("vpnprotocols.android.library")
}

android {
    namespace = "com.tim.openvpn"
}

dependencies {
    api(libs.tim.base)
    api(libs.tim.notification)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecyclektx)
    implementation(libs.kotlin.coroutines.core)

    compileOnly(libs.androidx.datastore)

    testImplementation(libs.test.junit)
}