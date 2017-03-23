# Retrofit
-dontwarn rx.**
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-dontwarn retrofit.**
-dontwarn retrofit.appengine.UrlFetchClient
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions

# Gson
-keep class com.google.gson.** { *; }

# Hockeyapp
-keep class net.hockeyapp.android.UpdateFragment
-keepclassmembers class net.hockeyapp.android.UpdateFragment {
  *;
}


# Guava
-keep class com.google.common.io.Resources {
    public static <methods>;
}
-keep class com.google.common.collect.Lists {
    public static ** reverse(**);
}
-keep class com.google.common.base.Charsets {
    public static <fields>;
}

-keep class com.google.common.base.Joiner {
    public static com.google.common.base.Joiner on(java.lang.String);
    public ** join(...);
}
-keep class com.google.common.collect.MapMakerInternalMap$ReferenceEntry
-keep class com.google.common.cache.LocalCache$ReferenceEntry
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn java.lang.ClassValue
-dontwarn com.google.j2objc.annotations.Weak
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
