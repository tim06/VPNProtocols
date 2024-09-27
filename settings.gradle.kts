pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()

        // todo remove RoomigrantLib
        maven { setUrl("https://jitpack.io") }
    }
}

rootProject.name = "VPNProtocols"
include(
    ":sample",
    ":base:notification",
    ":base:basevpn",
    ":base:state",
    ":protocols:openvpn",
    ":protocols:shadowsocksR",
    ":protocols:ikev2",
    ":protocols:xrayNg",
    //":protocols:xrayNeko",
)

gradle.startParameter.excludedTaskNames.addAll(listOf(":build-logic:plugins:testClasses"))