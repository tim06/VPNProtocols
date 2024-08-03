plugins {
    id("vpnprotocols.android.library")
}

android {
    namespace = "com.tim.state"
}
dependencies {
    implementation(libs.androidx.datastore)
}