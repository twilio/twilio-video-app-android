Android Video SDK
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

## Developing

If you have all the dependencies installed, you can proceed with development. The SDK is built with gradle and can be imported into Android Studio. For first time users, just import the top level build.gradle file into Android Studio.

## Releasing
The SDK is built and consumed with Android Studio as an aar. However, we currently ship a tar file with a 'fat' jar that contains all the native dependencies. To package a release execute the following.

```
./gradlew video:clean video:packageRelease
```

## Build Pipeline

The pom-release-candidate.xml is used to move artifacts from the snapshots to releases. The task itself
is managed in Jenkins and should NOT be run manually.

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

