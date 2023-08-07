plugins {
    id("vpnprotocols.android.library")
}

android {
    namespace = "com.tim.ikev2"
}

dependencies {
    api(libs.tim.base)
    api(libs.tim.notification)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference)
    implementation(libs.material)
}
