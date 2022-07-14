-keep class com.tim.openvpn.OpenVPNConfig { *; }
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
-keep class com.tim.basevpn.permission.VpnActivityResultContract { *; }
-keep class com.tim.basevpn.permission.VpnPermissionRequestKt { *; }
-keep class com.tim.basevpn.permission.VpnPermissionKt { *; }
-keepclasseswithmembernames class com.tim.basevpn.permission.* { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class com.tim.notification.NotificationHelper { *; }