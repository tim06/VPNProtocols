import com.vanniktech.maven.publish.MavenPublishBaseExtension

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.publish) apply false
}

allprojects {
    plugins.withId("com.vanniktech.maven.publish.base") {
        group = "io.github.tim06"
        version = findProperty("protocols.version") as String

        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)
            signAllPublications()

            pom {
                name.set("VPN Protocols")
                description.set("Android VPN library.")
                inceptionYear.set("2022")
                url.set("https://github.com/tim06/VPNProtocols/")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("tim06")
                        name.set("Timur Hojatov")
                        url.set("https://github.com/tim06")
                        email.set("timhod06@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/tim06/VPNProtocols/")
                    connection.set("scm:git:git://github.com/tim06/VPNProtocols.git")
                    developerConnection.set("scm:git:ssh://git@github.com:tim06/VPNProtocols.git")
                }
            }
        }
    }
}