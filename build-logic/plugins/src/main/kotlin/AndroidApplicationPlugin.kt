import com.android.build.api.dsl.ApplicationExtension
import com.tim.vpnprotocols.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get

class AndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("kotlin-parcelize")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = (findProperty("android.targetSdk") as String).toInt()

                sourceSets["main"].jniLibs.srcDir("src/main/jniLibs")
            }
        }
    }

}