plugins {
    id("vpnprotocols.android.library")
    id("kotlin-kapt") // TODO remove
}

android {
    namespace = "com.tim.xtlsr"

    buildFeatures {
        dataBinding {
            enable = true
        }
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(files("libs/libcore.aar"))

    api(libs.tim.base)
    api(libs.tim.notification)
    implementation(libs.kotlin.coroutines.core)

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("androidx.work:work-multiprocess:2.8.1")
    implementation("com.github.jenly1314:zxing-lite:2.1.1")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.simplecityapps:recyclerview-fastscroll:2.0.1") {
        exclude(group = "androidx.recyclerview")
        exclude(group = "androidx.appcompat")
    }

    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("com.github.MatrixDev.Roomigrant:RoomigrantLib:0.3.4")
    kapt("com.github.MatrixDev.Roomigrant:RoomigrantCompiler:0.3.4")
    implementation("com.google.code.gson:gson:2.9.0")

    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.3")
    implementation("org.yaml:snakeyaml:1.30")
    implementation("com.github.daniel-stoneuk:material-about-library:3.2.0-rc01")
    implementation("com.esotericsoftware:kryo:5.2.1")
}