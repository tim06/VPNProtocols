plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
}

addAndroidLibrarySection("com.tim.openvpn")

dependencies {
    api(project(":base:basevpn"))
    implementation(project(":base:notification"))

    implementation(libs.androidx.lifecyclektx)
}