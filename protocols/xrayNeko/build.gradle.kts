plugins {
    id("vpnprotocols.android.library")
}

android {
    namespace = "com.tim.vpn.xrayNeko"
}

dependencies {
    compileOnly(files("libs/libcore.aar"))

    api(libs.tim.base)
    api(libs.tim.state)
    api(libs.tim.notification)
    implementation(libs.kotlin.coroutines.core)
    implementation("androidx.appcompat:appcompat:1.7.0")

    api("org.yaml:snakeyaml:1.30")
    implementation("org.ini4j:ini4j:0.5.4")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.esotericsoftware:kryo:5.2.1")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.3")
}