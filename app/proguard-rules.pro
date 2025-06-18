-keep class tvi.webrtc.** { *; }
-dontwarn tvi.webrtc.**
-keep class com.twilio.video.** { *; }
-keep class com.twilio.common.** { *; }
-keepattributes InnerClasses

# https://github.com/firebase/firebase-android-sdk/issues/4900#issuecomment-1520001376
-keep class com.google.android.gms.internal.** { *; }

# Facebook Conceal proguard config
-keep class com.facebook.crypto.** { *; }
-keep class com.facebook.jni.** { *; }
-keepclassmembers class com.facebook.cipher.jni.** { *; }

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
 -keep,allowobfuscation,allowshrinking interface retrofit2.Call
 -keep,allowobfuscation,allowshrinking class retrofit2.Response

 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
 -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

 # Needed when using the VirtualBackgroundProcessor
 -keep class com.google.mlkit.common.** { *; }
 -keep class com.google.mlkit.vision.** { *; }