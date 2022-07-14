plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
}

setupLibraryModule("com.tim.basevpn", true)

dependencies {
    api(libs.androidx.lifecyclektx)
    api(libs.androidx.fragment)
}