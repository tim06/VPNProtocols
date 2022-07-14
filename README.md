# VPN Protocols
Android client implementation of VPN. Supported protocols:
- [x] OpenVPN
- [x] ShadowsocksR
- [ ] WireGuard
- [ ] AnyConnect

# Usage
Add library with Gradle
```
# Groovy
implementation 'io.github.tim06:openvpn:1.0.12'
implementation 'io.github.tim06:shadowsocksr:1.0.12'

# Kotlin
implementation("io.github.tim06:openvpn:1.0.12")
implementation("io.github.tim06:shadowsocksr:1.0.12")
```
Easy to use with delegate access
```
import com.tim.openvpn.delegate.openVPN
# or
import com.tim.shadowsocksr.delegate.shadowsocksR

# Activity
val openvpn by openVPN(OpenVPNConfig()) { state -> }
val shadowsocksR by shadowsocksR(ShadowsocksRVpnConfig()) { state -> }

openvpn.start() / openvpn.stop()
shadowsocksR.start() / shadowsocksR.stop()
```
# Tested server part
```
# OpenVPN
wget https://git.io/vpn -O openvpn-install.sh
sudo chmod +x openvpn-install.sh
sudo bash openvpn-install.sh
TCP / 443

# ShadowsocksR
wget --no-check-certificate https://raw.githubusercontent.com/teddysun/shadowsocks_install/master/shadowsocksR.sh
chmod +x shadowsocksR.sh
./shadowsocksR.sh 2>&1 | tee shadowsocksR.log
```

# Thanks
- [ics-openvpn](https://github.com/schwabe/ics-openvpn/)
- [shadowsocksR](https://github.com/shadowsocksrr/shadowsocksr-libev/)
