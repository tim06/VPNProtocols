plugins {
    id("vpnprotocols.android.library")
}

android {
    namespace = "com.tim.basevpn"
}

dependencies {
    implementation(libs.tim.state)
    implementation(libs.tim.notification)
    implementation(libs.androidx.lifecyclektx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.datastore)
}