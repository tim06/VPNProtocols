plugins {
    `kotlin-dsl`
}

group = "com.tim.vpnprotocols"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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