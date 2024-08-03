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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
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

    configurations.configureEach {
        resolutionStrategy {
            force("androidx.test:core:1.6.0")
        }
    }
}

dependencies {
    // xtls
    //implementation(files("libs/libcore.aar"))
    implementation(files("libs/libv2ray.aar"))

    //Modules
    implementation(project(":base:state"))
    implementation(project(":base:basevpn"))
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

    implementation("io.github.tim06.xray-configuration:xray-configuration-android:1.0")

    //Test
    /*testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junit)
    androidTestImplementation(libs.test.android.junit)
    androidTestImplementation("io.mockk:mockk-android:1.13.11")
    androidTestImplementation("com.google.truth:truth:1.4.3")*/
    //androidTestImplementation("androidx.test:runner:1.6.1")

    // Espresso dependencies
    /*androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")*/
    //androidTestImplementation(libs.test.android.junit)
    //androidTestImplementation("androidx.test:runner:1.6.1")
    /*androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")*/

    // Mockito dependencies
    /*testImplementation("org.mockito:mockito-core:4.1.0")
    androidTestImplementation("org.mockito:mockito-android:4.1.0")
    testImplementation("org.mockito:mockito-inline:4.1.0")*/

    // Coroutines for testing
    /*testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")*/
    //debugImplementation("androidx.fragment:fragment-testing:1.8.1")
    /*androidTestImplementation("androidx.navigation:navigation-testing:2.7.7")
    androidTestImplementation("androidx.test:core:1.6.0")
    testImplementation("androidx.test:core:1.6.0")

    debugImplementation("androidx.fragment:fragment-testing-manifest:1.8.1")
    androidTestImplementation("androidx.fragment:fragment-testing:1.8.1")*/

    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
    androidTestImplementation("com.kaspersky.android-components:kaspresso:1.5.4")
    androidTestImplementation("com.kaspersky.android-components:kaspresso-allure-support:1.5.4")
    androidTestUtil("androidx.test:orchestrator:1.4.2")

    // temp
    implementation("com.google.code.gson:gson:2.9.0")
}