rootProject.name = "VPNProtocols"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        mavenLocal()
    }
}

include(
    ":app",
    ":base:notification",
    ":base:basevpn",
    ":protocols:openvpn",
    ":protocols:shadowsocksR"
)
