plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    id("maven-publish")
}

addAndroidLibrarySection("com.tim.shadowsocksr")

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.tim"
            artifactId = "shadowsocksr"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "myrepo"
            url = uri("${project.buildDir}/repo")
        }
    }
}

dependencies {
    api(project(":base:basevpn"))
    implementation(project(":base:notification"))

    implementation(libs.androidx.lifecyclektx)
}