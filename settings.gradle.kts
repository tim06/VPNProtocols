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
    }
}

rootProject.name = "VPNProtocols"
include(
    ":sample",
    ":base:notification",
    ":base:basevpn",
    ":protocols:openvpn",
    ":protocols:shadowsocksR"
)
