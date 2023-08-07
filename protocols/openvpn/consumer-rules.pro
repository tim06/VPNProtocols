-keep class com.tim.openvpn.configuration.OpenVPNConfig { *; }
-keep class com.tim.openvpn.delegate.DelegateKt { *; }
-keep class com.tim.openvpn.utils.NativeLibsHelper { *; }
-keep class com.tim.openvpn.core.NativeUtils { *; }
-keep class com.tim.basevpn.IVPNService { *; }
-keep class * extends android.os.Binder { *; }
-keep class com.tim.basevpn.IConnectionStateListener { *; }
-keep class com.tim.basevpn.state.ConnectionState { *; }
-keep class com.tim.basevpn.delegate.VpnConnectionServiceDelegate { *; }
-keep class com.tim.basevpn.delegate.VPNRunner { *; }
-keep class com.tim.basevpn.delegate.StateDelegate { *; }
-keep class com.tim.basevpn.permission.VpnPermissionKt { *; }
-keepclasseswithmembernames class com.tim.basevpn.permission.* { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class net.openvpn.** { *; }
-keep class com.tim.openvpn.OpenVPNThreadv3 { *; }