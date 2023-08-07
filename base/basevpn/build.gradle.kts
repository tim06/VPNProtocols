plugins {
    id("vpnprotocols.android.library")
}

android {
    namespace = "com.tim.basevpn"
}

dependencies {
    implementation(libs.androidx.lifecyclektx)
}