# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Jordy\AppData\Local\Android\sdk1/tools/proguard/proguard-android.txt
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
-dontwarn com.google.android.gms.**
-dontwarn com.google.common.**
-dontwarn org.apache.**
-dontwarn com.smaato.soma.**
-dontwarn com.dropbox.**
-dontwarn com.cleveroad.audiowidget.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn com.github.siyamed.**
-dontwarn com.zendesk.**
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-keep public class org.jsoup.** {
public *;
}
-keep public class pl.droidsonroids.** {
public *;
}
-keep public class pl.droidsonroids.gif.GifIOException{<init>(int, java.lang.String);}
#-keep public class java.io.** {
#public *;
#}
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
  public *;
}

