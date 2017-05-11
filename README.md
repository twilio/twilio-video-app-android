# Android Video SDK

- [Getting Started](#getting-started)
- [Project Modules](#project-modules)
- [Tests](#tests)

The Android Video SDK provides multi-party voice and video calling on Android.

## Getting Started

To get started we recommend you use Android Studio for all your development.
In order to use our project please make sure you have the following installed:

1.  Install the Android SDK
    * Define `$ANROID_HOME`, `$ANDROID_SDK_HOME`, and `$ANDROID_SDK_ROOT`
1.  Install [Android NDK r12b](https://developer.android.com/ndk/downloads/older_releases.html)
    * Define `$NDK_ROOT`, `$ANDROID_NDK_HOME`, and `$ANDROID_NDK_ROOT`
1.  Add the following to your `$PATH`
    * `$ANDROID_HOME/tools`
    * `$ANDROID_HOME/platform-tools`
    * `$ANDROID_NDK_ROOT`
1.  Generate a google-services.json by following these instructions:
    * [Generate google-services.json](https://firebase.google.com/docs/android/setup#manually_add_firebase)
      * Copy the `google-services.json` to the `app` folder.
      This will use the same firebase app for all build flavors
    * For **Twilio developers** you can download the internal google-services.json here:
      * [Internal Debug (default)](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal.debug) - Download to `app/src/internal/debug`
      * [Internal Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal) - Download to `app/src/internal/release`
      * [Production Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app) - Download to `app/src/production/release`
1.  Setup your Programmable Video credentials

    Set your credentials in `twilio-video.json` located in the `library` directory.
    There are two sets of key/value pairs: mandatory and optional. Mandatory values are
    required to be set before building the project. Optional values are not required to be set,
    but as a Twilio developer they are required to run tests across different server
    environments and topologies.

    An example json file is provided under [library/twilio-video-example.json](library/twilio-video-example.json).
    For Twilio developers, these values represent prod credentials and a P2P configuration profile SID.

    #### Mandatory Credentials

    The following values MUST be set to execute tests:

    ```
    account_sid
    api_key
    api_key_secret
    configuration_profile_sid
    ```

    #### Optional Credentials

    The following values are for prod configuration profile SIDs that allow developers to
    test SFU and SFU Recording. Note these are optional values, but are required
    to ensure the entire test suite can be executed. The values are not mandatory because not
    every developer is guaranteed to have configuration profile SIDs for SFU or SFU Recording:

    ```
    sfu_configuration_profile_sid
    sfu_recording_configuration_profile_sid
    ```

    The following values are optional but are needed to run the test suite against dev
    or stage environments:

    ```
    dev_account_sid
    dev_api_key
    dev_api_key_secret
    dev_p2p_configuration_profile_sid
    dev_sfu_configuration_profile_sid
    dev_sfu_recording_configuration_profile_sid
    ```
    ```
    stage_account_sid
    stage_api_key
    stage_api_key_secret
    stage_p2p_configuration_profile_sid
    stage_sfu_configuration_profile_sid
    stage_sfu_recording_configuration_profile_sid
    ```

## Project Modules

* **app**: Provides a canonical multi-party voice and video calling application that uses the Android SDK
* **env**: Allows developers to set environment variables in native C/C++ using JNI.
This is only applicable for **Twilio developers**. Accessing dev or stage requires VPN.
* **library**: The Android SDK that provides the Java classes and interfaces used
by Android developers to perform multi-party voice and video calling
* **twilioapi**: A utility module for using the Twilio REST API to get ice servers

## Tests

The tests are located in the library module and use the AndroidJUnitRunner.
The tests interact with the backend infrastructure and will result in billing activity on your account.

### Test Coverage
Enabling test coverage requires setting project property `testCoverageEnabled`. The snippet below
demonstrates executing unit and instrumentation tests with code coverage enabled.

`./gradlew -PtestCoverageEnabled=true library:clean library:jacocoTestReport`
