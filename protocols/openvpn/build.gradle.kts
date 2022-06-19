plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    id("maven-publish")
}

addAndroidLibrarySection("com.tim.openvpn")

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.tim"
            artifactId = "openvpn"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
            pom {
                name.set("Android OpenVPN library")
                description.set("Android OpenVPN implementation")
                url.set("https://github.com/tim06/VPNProtocols")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("tim06")
                        name.set("Timur Hojatov")
                        email.set("timhod06@gmail.com")
                    }
                }
            }
        }
    }
    repositories {
        mavenCentral()
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