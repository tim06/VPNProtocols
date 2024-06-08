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
        google()
        mavenCentral()
        mavenLocal()

        // todo remove RoomigrantLib
        maven { setUrl("https://jitpack.io") }
    }
}

rootProject.name = "VPNProtocols"
include(
    ":sample",
    ":base:notification",
    ":base:basevpn",
    ":protocols:openvpn",
    ":protocols:shadowsocksR",
    ":protocols:ikev2",
    //":protocols:xtlsr",
    ":protocols:xrayNg",
    //":protocols:xrayNeko",
)
