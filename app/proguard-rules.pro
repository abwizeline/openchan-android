# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\AndroidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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

-optimizations   code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*,!method/inlining/*
-optimizationpasses 5
-allowaccessmodification
# -renamesourcefileattribute SourceFile

-dontwarn com.android.installreferrer
-dontwarn com.appsflyer.*

-keep class com.my.tracker.** { *; }
-dontwarn com.my.tracker.**
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep class com.android.installreferrer.** { *; }
-keep class com.android.vending.billing.** { *; }
-keep class com.android.billingclient.api.** { *; }

-keep class com.appsee.** { *; }
-dontwarn com.appsee.**
-keep class android.support.** { *; }
-keep interface android.support.** { *; }
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keepattributes SourceFile,LineNumberTable

-keep class com.android.vending.billing.**
-keep class cn.pedant.SweetAlert.Rotate3dAnimation {
    public <init>(...);
 }
-keep class de.blinkt.openvpn.**

-dontwarn retrofit2.**
# -keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn com.squareup.**
-keep class com.squareup.** { *; }
-keepclasseswithmembers class * {
    @com.squareup.* <methods>;
}
-keepclasseswithmembers interface * {
    @com.squareup.* <methods>;
}

-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keepclasseswithmembers class * {
    @okhttp3.* <methods>;
}
-keepclasseswithmembers interface * {
    @okhttp3.* <methods>;
}

-keep class okio.** { *; }
-dontwarn okio.**
-keep class javax.** { *; }
