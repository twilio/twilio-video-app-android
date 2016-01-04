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

Alternatively, dependencies can be obtained from the workspace of the latest `twilio-sdk-android-multi` Jenkins build.

## Build

If you have all the dependencies installed, you can proceed with building the SDK. Run the following from the root directory:

* `$ ./script/mksdk.sh release` for release version
* `$ ./script/mksdk.sh debug` for debug version

When the build is complete the library archive is stored in the `output` directory.


## Developing

The Android Conversations SDK project contains the following:

* `sdk`: Java and JNI implementations provided to developers
* `sdktests`: Instrumentation tests that run on connected devices
* `quickstart`: The gradle based example application provided to customers

