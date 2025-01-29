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