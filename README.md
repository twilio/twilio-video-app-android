# Android Video SDK

- [Getting Started](#getting-started)
- [Project Modules](#project-modules)
- [Video App](#video-app)
- [Tests](#tests)
- [Setup an Emulator](#setup-an-emulator)
- [Library Size](#library-size)
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

_Sections of this document pertain only to developers that work at Twilio aka "Twilions". If you are not a Twilion please ignore these sections._

## Getting Started

To get started we recommend you use Android Studio for all your development.
In order to use our project please perform the following steps:

1.  Install the Android SDK using Android Studio.
1.  Download Android NDK r12b. The Android NDK is a set of tools that allow developers to implement
parts of their application or libraries in native code using languages like C and C++. The Video
Android SDK contains native C and C++ code that uses the Twilio Video C++ SDK. The two SDKs interact using the [Java Native Interface (JNI)](https://docs.oracle.com/javase/7/docs/technotes/guides/jni/).
    * Direct download links
        * [Windows 32-bit](https://dl.google.com/android/repository/android-ndk-r12b-windows-x86.zip)
        * [Windows 64-bit](https://dl.google.com/android/repository/android-ndk-r12b-windows-x86_64.zip)
        * [Mac OS X](https://dl.google.com/android/repository/android-ndk-r12b-darwin-x86_64.zip)
        * [Linux 64-bit (x86)](https://dl.google.com/android/repository/android-ndk-r12b-linux-x86_64.zip)
1. Setup Android NDK r12b.
    * Set the environment variable `$ANDROID_NDK_HOME` to the path of where you extracted Android NDK r12b.
    * Set the path in Android Studio by navigating to File → Project Structure → SDK Location → Android NDK location.

        <img width="700px" src="images/community-variant/android-ndk-location.png"/>

1.  **Twilions** download the google-services.json files here:
      * [Internal Debug (default)](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal.debug) - Download to `app/src/internal/debug`
      * [Internal Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal) - Download to `app/src/internal/release`
      * [Twilio Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app) - Download to `app/src/twilio/release`
1.  Setup your Programmable Video credentials. Credentials are required to run the SDK 
instrumentation tests and certain app flavors. The credentials in this project are managed 
using JSON files. The table below provides a short summary of required credentials:

    Credential | JSON Key | Description
    ---------- | ----------- | -----------
    Twilio Account SID | `account_sid` | Your main Twilio account identifier - [find it on your dashboard](https://www.twilio.com/console).
    API Key | `api_key` | Used to authenticate - [generate one here](https://www.twilio.com/console/video/runtime/api-keys).
    API Secret | `api_key_secret` | Used to authenticate - [just like the above, you'll get one here](https://www.twilio.com/console/video/runtime/api-keys).

    #### Video Android App
    Copy the JSON snippet below to `app/twilio-video-app.json` and use the
    table above as reference to fill in your Twilio credentials. **Injecting
    credentials into a client side app should not be done in production
    apps. This practice is only acceptable for development and testing
    purposes.**
    
    ```
    {
      "credentials": {
        "account_sid": "AC00000000000000000000000000000000",
        "api_key": "SK00000000000000000000000000000000",
        "api_key_secret": "00000000000000000000000000000000"
      }
    }
    ```

    #### Video Android SDK
    Copy the JSON snippet below to `library/twilio-video.json` and use the
    table above as reference to fill in your Twilio credentials. **Injecting
    credentials into a client side app should not be done in production
    apps. This practice is only acceptable for development and testing
    purposes.**

    The following values MUST be set to execute tests. For Twilions, these values represent
    `prod` credentials.

    ```
    {
      "credentials": {
        "account_sid": "AC00000000000000000000000000000000",
        "api_key": "SK00000000000000000000000000000000",
        "api_key_secret": "00000000000000000000000000000000"
      }
    }
    ```

## Project Modules

* **app**: Provides a canonical multi-party voice and video calling application that uses the Android SDK
* **env**: Allows developers to set environment variables in native C/C++ using JNI.
This is only applicable for **Twilions**. Accessing dev or stage requires VPN.
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
* **apkscale**: A utility app module for determining the size impact of the Video Android SDK

## Video App

The Video App demonstrates a multi-party voice and video application built with the Android 
SDK. The application consists of the following [product flavors](http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Product-flavors):

1. Internal - The application intended for internal testing and QA at Twilio. _This variant can only be built by Twilions._
1. Twilio - The application intended for every day use at Twilio. _This variant can only be built by Twilions._
1. Community - The application intended for developers interested in using Programmable Video. _This variant can be built by all developers._

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

### Enabling Test Retries
In general, retrying tests is not a practice that is encouraged. However, the instrumentation tests
rely on a stable network connection so sometimes test flakiness occurs when the suite is
run on Firebase Test Lab where network conditions vary. Some tests that are known to be flaky are
annotated with `@Retry(times = BuildConfig.MAX_TEST_RETRIES)`. By default,
`BuildConfig.MAX_TEST_RETRIES` is set to 1, so the test is executed only once. To retry known flaky tests, set the project property
`testRetriesEnabled` to `true`. This will result in setting `MAX_TEST_RETRIES` to 3 which results in the test being retried 3 times.


`./gradlew -PtestRetriesEnabled=true library:clean firebaseTestLabCheckLibraryNoStats`

### Test Coverage
Enabling test coverage requires setting project property `testCoverageEnabled`. The snippet below
demonstrates executing unit and instrumentation tests with code coverage enabled.

`./gradlew -PtestCoverageEnabled=true library:clean library:jacocoTestReport`

### Troubleshooting Tests

All instrumentation tests should pass when executed locally. If you experience test failures
be sure to check the following:

- Validate that your credentials are setup properly.
- Ensure that your Twilio account has sufficient funds.
- Check that your device is connected to the internet.

If you continue to experience test failures please 
[open an issue](https://github.com/twilio/twilio-video-android/issues).

### Switching Server Environments (Twilions only)
Twilions can execute the tests in different server environments by performing the following steps:

1. Add `ENVIRONMENT` to `local.properties`. Supported values are `dev`, `stage`, or `prod`.
1. Update `library/twilio-video.json` to include dev and stage values.

    ```
    {
      "credentials": {
        "account_sid": "AC00000000000000000000000000000000",
        "api_key": "SK00000000000000000000000000000000",
        "api_key_secret": "00000000000000000000000000000000",
        
        "dev_account_sid": "AC00000000000000000000000000000000",
        "dev_api_key": "SK00000000000000000000000000000000",
        "dev_api_key_secret": "00000000000000000000000000000000",
        
        "stage_account_sid": "AC00000000000000000000000000000000",
        "stage_api_key": "SK00000000000000000000000000000000",
        "stage_api_key_secret": "00000000000000000000000000000000"
      }
    }
    ```

## Setup an Emulator

Perform the following steps to setup an emulator that works with the SDK and application.

1. Open Android Studio and navigate to Tools → Android → AVD Manager.
  <img width="700px" src="images/emulator/emulator_navigate.png"/>
2. Create a virtual device.
  <img width="700px" src="images/emulator/emulator_virtual_device.png"/>
3. Select your desired device.
  <img width="700px" src="images/emulator/emulator_select_hardware.png"/>
4. Select a system image. We recommend either x86 or x86_64 images.
  <img width="700px" src="images/emulator/emulator_select_image.png"/>
5. Click "Show Advanced Settings" and we recommend setting both cameras as "Emulated". Note that other camera configurations will work with the exception of setting both cameras as "webcam()". 
  <img width="700px" src="images/emulator/emulator_avd_settings.png"/>
6. Configure the rest of your device accordingly and click "Finish".

## Library Size

This project contains facilities to determine the size impact of the Video Android SDK in an
application.

### Calculate the Size Report of the Current Project
`./gradlew librarySizeReport`

The `librarySizeReport` task outputs the size footprint the Video Android SDK adds to an
application for each ABI. The `universal` ABI represents applications that include support for all
architectures in one APK.

> Video Android Size Report
>
>| ABI             | APK Size Impact |
>| --------------- | --------------- |
>| universal       | 22.2MB          |
>| armeabi-v7a     | 4.8MB           |
>| arm64-v8a       | 5.8MB           |
>| x86             | 6.2MB           |
>| x86_64          | 5.9MB           |


### Calculate the Size Report of a Specific Version
`./gradlew -PapkScaleVideoAndroidVersion=2.0.0 librarySizeReport`

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
