plugins {
    id("com.android.library")
    kotlin("android")
}

setupLibraryModule("com.tim.notification", true)

dependencies {
    api(libs.androidx.core)
}