Android Conversations SDK
=============================

##Getting Started

1.  Install the Android SDK
    * Define `$ANROID_HOME`, `$ANDROID_SDK_HOME`, and `$ANDROID_SDK_ROOT`
1.  Install the Android NDK
    * Define `$NDK_ROOT`, `$ANDROID_NDK_HOME`, and `$ANDROID_NDK_ROOT`
1.  Add the following to your `$PATH`
    * `$ANDROID_HOME/tools`
    * `$ANDROID_HOME/platform-tools`
    * `$ANDROID_NDK_ROOT`

## Dependencies

In order to build the Android Conversations SDK you will need to obtain the dependency libraries. This includes *BoringSSL*, *ReSIP*, *WebRTC*, and the *Core*. To build these dependencies please checkout [twilio-sdk-build-tools](https://code.hq.twilio.com/client/twilio-sdk-build-tools). Each dependency should be installed to `/usr/local/twilio-sdk`

Alternatively, dependencies can be obtained from the workspace of the latest [twilio-sdk-android-multi](http://172.16.25.60:8080/job/twilio-sdk-android-multi/) Jenkins build.

## Developing

If you have all the dependencies installed, you can proceed with development. The SDK is built with gradle and can be imported into Android Studio. For first time users, just import the top level build.gradle file into Android Studio.

## Releasing
The Conversations SDK is built and consumed with Android Studio as an aar. However, we currently ship a tar file with a 'fat' jar that contains all the native dependencies. To package a release execute the following.

```
./gradlew conversations:clean conversations:packageRelease
```

Upon completion twilio-conversations-android.tar.bz2 will be located in `conversations/build/outputs/tar`