plugins {
    id("vpnprotocols.android.application")
    id("vpnprotocols.android.application.compose")
}

android {
    namespace = "com.tim.vpnprotocols"

    defaultConfig {
        applicationId = "com.tim.vpnprotocols"
        versionCode = 1
        versionName = "0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        splits.abi {
            reset()
            include(
                "arm64-v8a",
                "armeabi-v7a",
                "x86_64",
                "x86"
            )
        }
    }

    signingConfigs {
        create("release") {
            com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir, providers).apply {
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
        }
    }

    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    splits {
        abi {
            isEnable = true
            isUniversalApk = false
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }
}

dependencies {
    // xtls
    //implementation(files("libs/libcore.aar"))
    implementation(files("libs/libv2ray.aar"))

    //Modules
    //implementation(libs.tim.openvpn)
    //implementation(libs.tim.shadowsocksr)
    implementation(project(":protocols:openvpn"))
    implementation(project(":protocols:shadowsocksR"))
    implementation(project(":protocols:ikev2"))
    //implementation(project(":protocols:xtlsr"))
    implementation(project(":protocols:xrayNg"))

    //AndroidX
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecyclektx)
    implementation(libs.material)

    //Koin
    implementation(libs.koin.android)

    //Coroutines
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.android)

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

    //DataStore
    implementation(libs.androidx.datastore)

    //Test
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.android.junit)

    // temp
    implementation("com.google.code.gson:gson:2.9.0")
}