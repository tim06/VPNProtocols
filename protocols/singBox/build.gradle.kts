plugins {
    id("vpnprotocols.android.library")
}

android {
    namespace = "com.tim.singBox"
}

dependencies {
    compileOnly(files("libs/libbox.aar"))
    compileOnly(libs.androidx.datastore)

    api(libs.tim.base)
    api(libs.tim.state)
    api(libs.tim.notification)
    api(libs.androidx.core)

    implementation(libs.androidx.lifecyclektx)
    implementation(libs.kotlin.coroutines.core)
}