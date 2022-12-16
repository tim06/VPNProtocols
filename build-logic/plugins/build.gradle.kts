plugins {
    `kotlin-dsl`
}

group = "com.tim.vpnprotocols"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "vpnprotocols.android.application"
            implementationClass = "AndroidApplicationPlugin"
        }
        register("androidApplicationCompose") {
            id = "vpnprotocols.android.application.compose"
            implementationClass = "AndroidApplicationComposePlugin"
        }
        register("androidLibrary") {
            id = "vpnprotocols.android.library"
            implementationClass = "AndroidLibraryPlugin"
        }
    }
}