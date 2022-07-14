import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

fun Project.setupLibraryModule(
    name: String,
    publish: Boolean = false,
    block: LibraryExtension.() -> Unit = {}
) = setupBaseModule<LibraryExtension> {
    namespace = name
    sourceSets["main"].jniLibs.srcDir("src/main/jniLibs")
    if (publish) {
        apply(plugin = "com.vanniktech.maven.publish.base")
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

    buildTypes {
        release {
            isMinifyEnabled = false
            consumerProguardFile(
                "proguard-rules.pro"
            )
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
        }
    }
    block()
}

private inline fun <reified T : BaseExtension> Project.setupBaseModule(
    crossinline block: T.() -> Unit = {}
) = extensions.configure<T>("android") {
    compileSdkVersion((findProperty("android.compileSdk") as String).toInt())
    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        allWarningsAsErrors = true
    }
    packagingOptions {
        resources.pickFirsts += "META-INF/AL2.0"
        resources.pickFirsts += "META-INF/LGPL2.1"
        resources.pickFirsts += "META-INF/*kotlin_module"
    }
    block()
}

private fun BaseExtension.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}