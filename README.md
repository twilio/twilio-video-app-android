Android Video SDK
=============================

## Getting Started

1.  Install the Android SDK
    * Define `$ANROID_HOME`, `$ANDROID_SDK_HOME`, and `$ANDROID_SDK_ROOT`
1.  Install the Android NDK
    * Define `$NDK_ROOT`, `$ANDROID_NDK_HOME`, and `$ANDROID_NDK_ROOT`
1.  Add the following to your `$PATH`
    * `$ANDROID_HOME/tools`
    * `$ANDROID_HOME/platform-tools`
    * `$ANDROID_NDK_ROOT`
1.  Download google-services.json for each needed build variant.
    1. [Internal Debug (default)](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal.debug) - Download to `app/src/internal/debug`
    1. [Internal Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal) - Download to `app/src/internal/release`
    1. [Production Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app) - Download to `app/src/production/release`

## Developing

If you have all the dependencies installed, you can proceed with development. The SDK is built with gradle and can be imported into Android Studio. For first time users, just import the top level build.gradle file into Android Studio.

### Enabling Native Debugging for the Application
By default, the Android Gradle plugin publishes the release variant of a library project. To override this behavior and enable native debugging of the application, add `PUBLISH_CONFIG=debug` to your `local.properties` file.

### Building Release Application
The Twilio keystore and credentials are needed to build Internal and Production releases. Contact project admins for help.

## Releasing
The SDK is built and consumed with Android Studio as an aar. However, we currently ship a tar file with a 'fat' jar that contains all the native dependencies. To package a release execute the following.

```
./gradlew video:clean video:packageRelease
```

## Bintray Publishing

These are executed from Jenkins, but these are a reference for building and uploading release artifacts

### Uploading a Release Candidate
```
./gradlew -PreleaseCandidate=true -Pmaven.repo=https://api.bintray.com/maven/twilio/internal-releases/video-android/ video:uploadArchives
```

### Uploading a Release
```
./gradlew -Prelease=true -Pmaven.repo=https://api.bintray.com/maven/twilio/internal-releases/video-android/ video:uploadArchives
```

