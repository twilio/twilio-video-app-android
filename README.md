# Android Video SDK

- [Getting Started](#getting-started)
- [Project Modules](#project-modules)
- [Video App](#video-app)
- [Tests](#tests)
- [Native Debugging](#native-debugging)
- [Code of Conduct](#code-of-conduct)
- [License](#license)


[Programmable Video](https://www.twilio.com/video) provides developers with the infrastructure 
and APIs to build WebRTC applications. The Android Video SDK allows developers to integrate 
multi-party voice and video calling into an Android app. This project contains the source for the
SDK along with a canonical app that demonstrates the platform's capabilities. For developers
interested in an introduction to the Video Android SDK, we recommend visiting the [Twilio
Video Quickstart for Android](https://github.com/twilio/video-quickstart-android) which contains 
step-by-step instructions for building a simple Android video application along with a few examples 
of how the SDK can be used.

## Getting Started

To get started we recommend you use Android Studio for all your development.
In order to use our project please make sure you have the following installed:

1.  Install the Android SDK
    * Define `$ANROID_HOME`, `$ANDROID_SDK_HOME`, and `$ANDROID_SDK_ROOT`
1.  Download Android NDK r12b. The Android NDK is a set of tools that allow developers to implement
parts of their application or libraries in native code using languages like C and C++. The Video
Android SDK contains native C and C++ code that uses the Twilio Video C++ SDK. The two SDKs interact using the [Java Native Interface (JNI)](https://docs.oracle.com/javase/7/docs/technotes/guides/jni/).
    * Direct download links
        * [Windows 32-bit](https://dl.google.com/android/repository/android-ndk-r12b-windows-x86.zip)
        * [Windows 64-bit](https://dl.google.com/android/repository/android-ndk-r12b-windows-x86_64.zip)
        * [Mac OS X](https://dl.google.com/android/repository/android-ndk-r12b-darwin-x86_64.zip)
        * [Linux 64-bit (x86)](https://dl.google.com/android/repository/android-ndk-r12b-linux-x86_64.zip)
1. Setup Android NDK r12b.
    * Set environment variables `$NDK_ROOT`, `$ANDROID_NDK_HOME`, and `$ANDROID_NDK_ROOT` with
    location of Android NDK r12b.
    * Add line `ndk.dir=/path/to/ndk/r12b` to `local.properties`
1.  Add the following to your `$PATH`
    * `$ANDROID_HOME/tools`
    * `$ANDROID_HOME/platform-tools`
    * `$ANDROID_NDK_ROOT`
1.  **Twilio developers** download the internal google-services.json files here:
      * [Internal Debug (default)](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal.debug) - Download to `app/src/internal/debug`
      * [Internal Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal) - Download to `app/src/internal/release`
      * [Twilio Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app) - Download to `app/src/twilio/release`
1.  Setup your Programmable Video credentials

    #### Video Android App
    These credentials are only required if you intend on using the community variant of the
    application. See [Building the Community Flavor](#building-the-community-flavor) for more
    details. Set your credentials in `twilio-video-app.json` located in the `app` directory.

    An example json file is provided under [app/twilio-video-app-example.json](app/twilio-video-app-example.json). 
    The following values MUST be set to build the community variant:

    ```
    account_sid
    api_key
    api_key_secret
    ```

    #### Video Android SDK
    Set your credentials in `twilio-video.json` located in the `library` directory.
    There are two sets of key/value pairs: mandatory and optional. Mandatory values are
    required to be set before building the project. Optional values are not required to be set,
    but as a Twilio developer they are required to run tests across different server
    environments and topologies.

    An example json file is provided under [library/twilio-video-example.json](library/twilio-video-example.json).
    For Twilio developers, these values represent prod credentials and a P2P configuration profile SID.

    ##### Mandatory Credentials

    The following values MUST be set to execute tests:

    ```
    account_sid
    api_key
    api_key_secret
    configuration_profile_sid
    ```

    ##### Optional Credentials

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
* **token**: A utility module for generating Video Access Tokens. **This module is intended to be 
used for testing and local development purposes only. Do not build an application that 
generates access tokens locally. Please follow the 
 [User Identity and Access Tokens guide](https://www.twilio.com/docs/api/video/identity) for proper
 instructions. Putting your Account SID, API Key, and API Key Secret inside 
 of an Android application will compromise your Twilio API credentials associated with your Twilio 
 account.**
* **twilioapi**: A utility module for using the Twilio REST API to get ice servers

## Video App

The Video App demonstrates a multi-party voice and video application built with the Android 
SDK. The application consists of the following [product flavors](http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Product-flavors):

1. Internal - The application intended for internal testing and QA at Twilio
1. Twilio - The application intended for every day use at Twilio
1. Community - The application intended for developers interested in using Programmable Video

### Building the Community Flavor
The community flavor of the application is meant for developers who would like to work with the 
Android SDK in the context of a full-fledged application without needing to bother with implementing 
authentication and managing a token server. **This variant generates access tokens locally within
the application. This practice is intended for local development and is not encouraged for your 
applications. Please follow the 
[User Identity and Access Tokens guide](https://www.twilio.com/docs/api/video/identity) for proper
token generation instructions in your application. Putting your Account SID, 
API Key, and API Key Secret inside of an Android application will compromise your Twilio API 
credentials associated with your Twilio account.**

To get started with the community flavor follow these steps:

1. Setup your `app/twilio-video-app.json` according to steps in [Getting Started](#getting-started).

2. In Android Studio navigate to View → Tool Windows → Build Variants.

    <img width="700px" src="images/community-variant/build-variants.png"/>
  
3. Select the `communityDebug` Build Variant under the app module.

    <img width="700px" src="images/community-variant/community-debug-variant.png"/>
  
4. Run the application.

### HockeyApp
The internal release flavor of the application requires setting the project property `hockeyAppId` 
to register for application updates. The following snippet demonstrates how to build an internal 
release with a Hockey App ID.

`./gradlew -PhockeyAppId=1234 app:assembleInternalRelease`

## Tests

The tests are located in the library module and use the AndroidJUnitRunner.
The tests interact with the backend infrastructure and will result in billing activity on your account.

### Test Coverage
Enabling test coverage requires setting project property `testCoverageEnabled`. The snippet below
demonstrates executing unit and instrumentation tests with code coverage enabled.

`./gradlew -PtestCoverageEnabled=true library:clean library:jacocoTestReport`

## Code of Conduct

This project adheres to the Contributor Covenant [Code of Conduct](CODE_OF_CONDUCT.md). By
participating, you are expected to uphold this code. Please report unacceptable behavior to
[video-conduct@twilio.com](mailto:video-conduct@twilio.com).

## License 

    Copyright 2017 Twilio, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
