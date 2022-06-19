plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.tim.vpnprotocols"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()

        applicationId = "com.tim.vpnprotocols"
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("/Users/tim/Desktop/vpn_protocols_keys/release_key.jks")
            com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir).apply {
                storePassword = getProperty("storePwd")
                keyAlias = getProperty("keyAlias")
                keyPassword = getProperty("keyPwd")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                "proguard-rules.pro",
                getDefaultProguardFile("proguard-android-optimize.txt")
            )
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            isMinifyEnabled = false
            proguardFiles(
                "proguard-rules.pro",
                getDefaultProguardFile("proguard-android-optimize.txt")
            )
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    sourceSets["main"].jniLibs.srcDir("src/main/jniLibs")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    //Modules
    //implementation(project(":base:basevpn"))
    //implementation(project(":protocols:shadowsocksR"))
    //implementation(project(":protocols:openvpn"))

    //AndroidX
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecyclektx)
    implementation(libs.material)

    //Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    //Logger
    implementation(libs.timber)

    //Viewbinding
    implementation(libs.viewbinding)

    //Navigation
    implementation(libs.navigation.ui)
    implementation(libs.navigation.fragment)

    //Compose
    implementation(libs.androidx.composeactivity)
    implementation(libs.compose.ui)
    implementation(libs.compose.uitooling)
    implementation(libs.compose.navigation)
    implementation(libs.material3)
    implementation(libs.material3) {
        exclude(group = "com.google.code.findbugs", module = "jsr305")
    }

    implementation(files("/Users/tim/Downloads/openvpn-1.0.0.aar"))
    implementation(files("/Users/tim/Downloads/shadowsocksr-1.0.0.aar"))
}