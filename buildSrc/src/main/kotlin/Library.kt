import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import com.android.build.gradle.LibraryExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get

internal val Project.libraryExtension: LibraryExtension
    get() = extensions.getByType()

fun Project.addAndroidLibrarySection(name: String) = libraryExtension.run {
    namespace = name
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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

    sourceSets["main"].jniLibs.srcDir("src/main/jniLibs")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // TODO wait to resolve https://github.com/gradle/gradle/issues/15383
    dependencies {
        implementation("com.jakewharton.timber:timber:4.7.1")
    }
}
