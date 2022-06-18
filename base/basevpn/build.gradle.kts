plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
}

addAndroidLibrarySection("com.tim.basevpn")

dependencies {
    implementation(libs.androidx.lifecyclektx)
    implementation(libs.androidx.fragment)
}