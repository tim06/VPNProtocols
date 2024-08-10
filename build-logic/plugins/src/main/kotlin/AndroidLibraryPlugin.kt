import com.android.build.gradle.LibraryExtension
import com.tim.vpnprotocols.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import java.net.URI
import java.util.Properties

class AndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("kotlin-parcelize")
                apply("com.vanniktech.maven.publish")
                apply("signing")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = (findProperty("android.targetSdk") as String).toInt()

                sourceSets["main"].jniLibs.srcDir("src/main/jniLibs")
                buildTypes {
                    getByName("release") {
                        consumerProguardFiles("consumer-rules.pro")
                        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                    }
                }
                afterEvaluate {
                    fun propertyOrEnv(propertyName: String): String {
                        val file = project.rootProject.file("gradle.properties")
                        if (file.exists()) {
                            val properties = Properties()
                            file.inputStream().use {
                                properties.load(it)
                            }
                            return if (properties.containsKey(propertyName)) {
                                properties.getProperty(propertyName)
                            } else ""
                        }

                        return runCatching { System.getenv(propertyName) }.getOrElse { "" }
                    }

                    extensions.configure<PublishingExtension> {
                        publications.create<MavenPublication>("release") {
                            from(components["release"])

                            // https://github.com/vanniktech/gradle-maven-publish-plugin/issues/326
                            val aid = project.property("POM_ARTIFACT_ID").toString()
                            artifactId = artifactId.replace(project.name, aid)
                            group = "io.github.tim06"
                            version = findProperty("protocols.version") as String

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

                        repositories {
                            maven {
                                name = "mavenCentral"
                                val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                                url = URI(releasesRepoUrl)
                                credentials {
                                    username = propertyOrEnv("mavenUsername")
                                    password = propertyOrEnv("mavenPassword")
                                }
                            }
                        }
                    }
                    configure<SigningExtension> {
                        extra["signing.keyId"] = propertyOrEnv("signingInMemoryKeyId")
                        extra["signing.password"] = propertyOrEnv("signingInMemoryKeyPassword")
                        extra["signing.secretKeyRingFile"] = propertyOrEnv("signingInMemoryKey")

                        val pubExt = checkNotNull(extensions.findByType(PublishingExtension::class.java))
                        val publication = pubExt.publications["release"]
                        sign(publication)
                    }
                }
            }
            dependencies {
                add("androidTestImplementation", kotlin("test"))
                add("testImplementation", kotlin("test"))
            }
        }
    }
}