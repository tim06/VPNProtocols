plugins {
    id("com.android.library")
    kotlin("android")
}

addAndroidLibrarySection("com.tim.notification")

dependencies {
    implementation(libs.androidx.core)
}