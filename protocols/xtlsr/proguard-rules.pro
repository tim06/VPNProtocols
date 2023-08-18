# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.tim.shadowsocksr.Native { *; }
-keep class com.tim.shadowsocksr.ShadowsocksRVpnConfig { *; }
-keep class com.tim.shadowsocksr.delegate.DelegateKt { *; }
-keep class com.tim.basevpn.delegate.VPNRunner { *; }
-keep class com.tim.basevpn.delegate.StateDelegate { *; }
-keep class * extends android.os.Binder { *; }
-keep class com.tim.notification.NotificationHelper { *; }