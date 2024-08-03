# VPN Protocols
Android client implementation of VPN. Supported protocols:
- [x] OpenVPN
- [x] ShadowsocksR
- [x] XrayNg — V2RayNg implementation
- [x] ~~Ikev2 Deprecated~~
- [ ] WireGuard
- [ ] AnyConnect

# Usage
Add library with Gradle
```
implementation("io.github.tim06:xrayNg:1.1.0")
implementation("io.github.tim06:openvpn:1.1.0")
implementation("io.github.tim06:shadowsocksr:1.1.0")
```
Easy to use
```
# XrayNg
val xrayConfiguration: String = "full_xray_configuration"
XRayNgService.startService(
  context = context,
  config = xrayConfiguration
)

# Openvpn
val openVpnConfiguration: String = "full_openvpn_configuration"
OpenVPNService.startService(
  context = context,
  config = OpenVPNConfig(configuration = configuration)
)

# ShadowsocksR
val shadowsocksRConfiguration: ShadowsocksRVpnConfig = ShadowsocksRVpnConfig()
ShadowsocksRService.startService(
  context = context,
  config = shadowsocksRConfiguration
)
```

# Thanks
- [xray-core](https://github.com/XTLS/Xray-core/)
- [ics-openvpn](https://github.com/schwabe/ics-openvpn/)
- [shadowsocksR](https://github.com/shadowsocksrr/shadowsocksr-libev/)
