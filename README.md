# Android Video SDK

- [Getting Started](#getting-started)
- [Project Modules](#project-modules)
- [Video App](#video-app)
- [Tests](#tests)
- [Setup an Emulator](#setup-an-emulator)
- [Library Size](#library-size)
- [Side-By-Side Support](#side-by-side-support)
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
1.  Install [bbe](https://linux.die.net/man/1/bbe)
1.  Download Android NDK r16b. The Android NDK is a set of tools that allow developers to implement
parts of their application or libraries in native code using languages like C and C++. The Video
Android SDK contains native C and C++ code that uses the Twilio Video C++ SDK. The two SDKs interact using the [Java Native Interface (JNI)](https://docs.oracle.com/javase/7/docs/technotes/guides/jni/).
    * Direct download links
        * [Windows 32-bit](https://dl.google.com/android/repository/android-ndk-r16b-windows-x86.zip)
        * [Windows 64-bit](https://dl.google.com/android/repository/android-ndk-r16b-windows-x86_64.zip)
        * [Mac OS X](https://dl.google.com/android/repository/android-ndk-r16b-darwin-x86_64.zip)
        * [Linux 64-bit (x86)](https://dl.google.com/android/repository/android-ndk-r16b-linux-x86_64.zip)
1. Setup Android NDK r16b.
    * Set the environment variable `$ANDROID_NDK_HOME` to the path of where you extracted Android NDK r16b.
    * Set the path in Android Studio by navigating to File → Project Structure → SDK Location → Android NDK location.

        <img width="700px" src="images/community-variant/android-ndk-location.png"/>

1.  **Twilions** download the google-services.json files here:
      * [Internal Debug (default)](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal.debug) - Download to `videoapp/app/src/internal/debug`
      * [Internal Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal) - Download to `videoapp/app/src/internal/release`
      * [Twilio Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app) - Download to `videoapp/app/src/twilio/release`
1.  Setup your Programmable Video credentials. Credentials are required to run the SDK
instrumentation tests and certain app flavors. The credentials in this project are managed
using JSON files. The table below provides a short summary of required credentials:

    Credential | JSON Key | Description
    ---------- | ----------- | -----------
    Twilio Account SID | `account_sid` | Your main Twilio account identifier - [find it on your dashboard](https://www.twilio.com/console).
    API Key | `api_key` | Used to authenticate - [generate one here](https://www.twilio.com/console/video/runtime/api-keys).
    API Secret | `api_key_secret` | Used to authenticate - [just like the above, you'll get one here](https://www.twilio.com/console/video/runtime/api-keys).


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
    #### Setting up Bintray for Twilions
    Building with release candidate or snapshot artifacts requires access to private
    Bintray repositories. Add the following lines to your local.properties file to enable
    downloading from private repositories:

    ```
    BINTRAY_USERNAME=your-bintray-username
    BINTRAY_PASSWORD=your-bintray-apikey
    ```
    #### Setting up Gradle Configuration
    In order to allocate enough memory for the Java heap space used by the Gradle daemon, add the line below to
    your system level gradle.properties file.
    ```
    org.gradle.jvmargs=-Xmx4608m
    ```
    If you don't add this line, you will see Gradle build failures related to the Java heap space.

## Project Modules

* **videoapp-app**: Provides a canonical multi-party voice and video calling application that uses the Android SDK
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

### Description

The Video App demonstrates a multi-party voice and video application built with the Android
SDK. This application is maintained in a [separate repository](https://github.com/twilio/twilio-video-app-android) and
linked to this project as a git subtree. For details on how to get started with the app, reference
the application [README](videoapp).

### Working with the Application Subtree
The following section provides common scenarios working with the application subtree. For more general
information about git subtrees reference the following [article](https://www.atlassian.com/git/tutorials/git-subtree).

#### Add Subtree as Remote
The following command allows the developer to reference the subtree in short form as a remote.

```
git remote add -f twilio-video-app-android git@github.com:twilio/twilio-video-app-android.git
```

#### Updating the Application Subtree

```
git fetch twilio-video-app-android master
git subtree pull --prefix videoapp twilio-video-app-android master --squash
```

#### Contributing Changes

```
git subtree push --prefix=videoapp twilio-video-app-android task/update-app
```

## Tests

The tests are located in the library module and use the AndroidJUnitRunner.
The tests interact with the backend infrastructure and will result in billing activity on your account.

### Setting Test Log Level
By default, the log level is set to `LogLevel.INFO` during test execution. To change the log level
during test execution set project property `testLogLevel` or add `TEST_LOG_LEVEL` entry to
`local.properties`. The value can be set to any value defined in the enum `LogLevel`.

#### Set Log Level with Project Property

`./gradlew -PtestLogLevel=ALL firebaseTestLabCheckLibraryNoStats`

#### Set Log Level in local.properties

`TEST_LOG_LEVEL=ALL`

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

## Side-by-Side Support

The Video SDK can be built alongside another WebRTC based dependency, such as the Voice Android SDK
without Java or native conflicts at build or runtime. Side-by-side support is achieved with two
steps: renaming the WebRTC jar classpath and the native symbols that reference the previous
classpath.

### Renaming the WebRTC Jar Classpath

The Video SDK uses [jarjar links](./library/jarjar) to rename the WebRTC jar package classpath from
`org.webrtc.*` to `tvi.webrtc.*` prior to assembling the SDK.

### Renaming Native Symbols

The Video SDK uses `bbe` to modify the native symbol references from `org_webrtc` to `tvi_webrtc`
in accordance with the classpath rename. CMake performs this action as a post build step of the final
.so file packaged into the aar.

## Code Formatting
This project maintains Google AOSP formatted code. Before submitting a pull request, make sure to run `./gradlew spotlessApply`

Incorrectly formatted code submitted as a pull request will fail during the build phase.


## Generating TwilioException.java
Platform specific error codes are housed in `TwilioException.java`. We use an internal repo to keep track of the error codes across all SDK platforms. To update `TwilioException.java`, simply navigate to the `scripts/` directory and run `./gen_errors.sh`.

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
