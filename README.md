Android Video SDK
=============================

## Getting Started

The following steps must be done to setup your machine to build the Android Video SDK.

1. Install [Android SDK](https://developer.android.com/studio/index.html)
    1. Define `$ANROID_HOME`, `$ANDROID_SDK_HOME`, and `$ANDROID_SDK_ROOT`
2. Install [Android NDK r12b](https://developer.android.com/ndk/downloads/older_releases.html)
    1. Define `$NDK_ROOT`, `$ANDROID_NDK_HOME`, and `$ANDROID_NDK_ROOT`
3. Add the following to your `$PATH`
    * `$ANDROID_HOME/tools`
    * `$ANDROID_HOME/platform-tools`
    * `$ANDROID_NDK_ROOT`
    
### Credentials

Credentials are required in the form of a `twilio-video.json` located in the `library` directory. 
There are two sets of key/value pairs: mandatory and optional. Mandatory values are 
required to be set before building the project. Optional values are not required to be set, 
but as a Twilio developer they are required to run tests across server environments and topologies.

The following values must be set to execute tests. An example json file is provided 
under [library/twilio-video-example.json](library/twilio-video-example.json). For Twilio 
developers, these values represent prod credentials and a P2P configuration profile SID:

```
account_sid
auth_token
api_key
api_key_secret
configuration_profile_sid
```


The following values are additional prod configuration profile SIDs that allow developers to 
test SFU and SFU Recording. Note these are optional values, but are required 
to ensure the entire test suite can be executed. The values are not mandatory because not 
every developer is guaranteed to have configuration profile SIDs for SFU or SFU Recording:

```
sfu_configuration_profile_sid
sfu_recording_configuration_profile_sid
```

The following values are optional but are needed to run the test suite against dev 
or staging environments:

```
dev_account_sid
dev_auth_token
dev_api_key
dev_api_key_secret
dev_p2p_configuration_profile_sid
dev_sfu_configuration_profile_sid
dev_sfu_recording_configuration_profile_sid

stage_account_sid
stage_auth_token
stage_api_key
stage_api_key_secret
stage_p2p_configuration_profile_sid
stage_sfu_configuration_profile_sid
stage_sfu_recording_configuration_profile_sid
```

### Google Services

To build the Video application download google-services.json for each needed build variant.

- [Internal Debug (default)](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal.debug) - Download to `app/src/internal/debug`
- [Internal Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal) - Download to `app/src/internal/release`
- [Production Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app) - Download to `app/src/production/release`

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

