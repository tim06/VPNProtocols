plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
}

setupLibraryModule("com.tim.openvpn", true)

dependencies {
    api(libs.tim.base)
    api(libs.tim.notification)
}