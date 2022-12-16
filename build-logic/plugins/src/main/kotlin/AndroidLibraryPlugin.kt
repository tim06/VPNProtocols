import com.android.build.gradle.LibraryExtension
import com.tim.vpnprotocols.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*

class AndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("kotlin-parcelize")
                apply("com.vanniktech.maven.publish.base")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = (findProperty("android.targetSdk") as String).toInt()

                sourceSets["main"].jniLibs.srcDir("src/main/jniLibs")
                publishing {
                    singleVariant("release") {
                        withSourcesJar()
                    }
                }
                afterEvaluate {
                    extensions.configure<PublishingExtension> {
                        publications.create<MavenPublication>("release") {
                            from(components["release"])
                            // https://github.com/vanniktech/gradle-maven-publish-plugin/issues/326
                            val id = project.property("POM_ARTIFACT_ID").toString()
                            artifactId = artifactId.replace(project.name, id)
                        }
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