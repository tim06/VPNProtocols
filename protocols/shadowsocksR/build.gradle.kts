plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
}

setupLibraryModule("com.tim.shadowsocksr", true)

dependencies {
    api(libs.tim.base)
    api(libs.tim.notification)

    api(libs.androidx.lifecyclektx)
}