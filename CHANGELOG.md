# Change Log

## 0.173 (Feb 12, 2026)

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.10.1 ([changelog](https://www.twilio.com/docs/video/changelog-twilio-video-android-latest#7101-feb-12-2026))


## 0.172 (Jan 14, 2026)

#### Enhancements

* New min supported Android version is 28 (Android 9/Android Pie)

## 0.171 (Jan 13, 2026)

### Dependency Upgrades

* Updated AudioSwitch version to 1.2.5

## 0.170 (Dec 12, 2025)

### Dependency Upgrades

* Updated Kotlin version to 1.8.22

## 0.169 (Dec 9, 2025)

### Feature Changes

* Added support for selecting virtual backgrounds during video calls.

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.10.0

## 0.167 (Oct 8, 2025)

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.9.1

## 0.166 (Sept 11, 2025)

### Dependency Changes

* Replaced use of `com.facebook.conceal` with `androidx.security:security-crypto` for encryption needs.


## 0.165 (Sept 5, 2025)

### Dependency Upgrades

* Updated to use AGP 8.12.1 and Gradle 8.13

## 0.164 (Aug 7, 2025)

### Feature Changes

* Added support for realtime transcriptions which can be enabled from the internal preferences menu.

## 0.163 (Feb 10, 2025)

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.8.0

## 0.162 (Feb 4, 2025)

### Bug Fixes

* Fixed obfuscation issue causing the internal edition to fail to retrieve a token in release builds.

## 0.161 (Jan 11, 2025)

### Bug Fixes

* Fixed bug with internal releases where users could not login using their gCloud credentials

## 0.160 (Jan 9, 2025)

### Feature Changes

* Audio track options are now set upon audio track construction.

## 0.159 (Dec 10, 2024)

### Bug Fixes

* Fixed bug regarding missing permissions when using android 34+ devices

## 0.156 (Nov 12, 2024)

### Dependency Upgrades

* Updated twilio-android-env to 1.1.1 to support 16k pages

### Feature Changes

* In preparation for the WebRTC-124 upgrade, the following necessary changes were made
   * removed support for globally setting audio channel effects as the necessary class WebRtcAudioUtils is removed from WebRTC-124.
   * removed support for globally setting the usage of SLES audio device as the necessary class WebRtcAudioManager is removed from WebRTC-124.

## 0.155 (Oct 23, 2024)

### Dependency Upgrades

* Updated to target API 35

## 0.149 (Sep 3, 2024)

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.7.1

## 0.135 (Sep 23, 2023)

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.7.0

## 0.134 (Sep 23, 2023)

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.6.4

## 0.132 (July 11, 2023)

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.6.3
* Updated to use AGP 8.0.2

## 0.127 (May 25, 2023)

### Dependency Upgrades

* Updated AudioSwitch version to 1.1.8


## 0.126 (Oct 10, 2022)

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.4.0

## 0.125 (Aug 24, 2022)

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.3.0

## 0.123 (July 6, 2022)

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.1.2

## 0.122 (June 17, 2022)

### Dependency Upgrades

* Updated AudioSwitch version to 1.1.5

## 0.121 (May 17, 2022)

### Bug Fixes

* When app is closed or killed, disconnection from room now happens properly.

## 0.120 (May 11, 2022)

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.1.1

## 0.119 (March 16, 2022)

### Dependency Upgrades

* Updated Twilio Video SDK version to 7.1.0

## 0.118 (January 18, 2022)

### Bug Fixes

* Modified 'internal' auth process to reflect internal changes to Twilio's auth service.

## 0.117 (January 6, 2022)

### Bug Fixes

* Updated e2e test cases such that successful execution is possible

### Dependency Upgrades

* Updated Espresso to 3.5.0-alpha03
* Updated junit/junit-ktx to 1.1.4-alpha03

## 0.116 (January 4, 2022)

### Dependency Upgrades

* Updated Audioswitch to 1.1.4

## 0.115 (December 6, 2021)

### Dependency Upgrades

* Modified circle-ci to always run e2e test cases
* Upgraded firebase-ui-auth to 7.2.0
* Upgraded  android gradle tools to 7.0.3
* Upgraded google-services 4.3.10
* Upgraded kotlin-gradle-plugin to 1.6.0
* Upgrade firebase-crashlytics-graddle to 2.8.1
* Upgraded Gradle to 7.0.2

### Bug Fixes

* Updated build to also include test runner framework application

## 0.114 (December 6, 2021)

### Dependency Upgrades

* This release uses Video Android SDK 7.0.3.

## 0.113 (November 3, 2021)

### Dependency Upgrades

* This release uses Video Android SDK 7.0.2.

## 0.111 (November 2, 2021)

* Updated to properly support Android 12
* Delayed starting of AudioSwitch till after user has granted necessary permissions
* Updated to use AudioSwitch 1.1.3

## 0.109 (October 15, 2021)

### Dependency Upgrades

* This release uses Video Android SDK 7.0.1.

## 0.108 (September 17, 2021)

### Dependency Upgrades

* This release uses Video Android SDK 7.0.0.
* The minimum supported Android API level has been raised to 21.

### 0.107 (September 13, 2021)

### New Feature

- Added support for Client Track Switch Off and Video Content Preferences.
- For more information, please view this [blog post](https://www.twilio.com/blog/improve-efficiency-multi-party-video-experiences) and feature [documentation](https://www.twilio.com/docs/video/tutorials/using-bandwidth-profile-api#understanding-clientTrackSwitchOffControl).

### Dependency Upgrades

- This release uses Video Android SDK 6.4.1


### 0.106 and earlier

**Note:** See git history for other changes since version 0.1.0.


## 0.1.0 (November 8, 2019)

This release marks the first iteration of the Twilio Video Collaboration App: a canonical multi-party collaboration video application built with Programmable Video. This application is intended to demonstrate the capabilities of Programmable Video and serve as a reference to developers building video apps.

This initial release comes with the following features:

- Join rooms with up to 50 participants
- Toggle local media: camera and mic
- Show a Room’s dominant speaker in the primary video view
- Show a participant’s network quality

We intend to iterate on these initial set of features and look forward to collaborating with the community.
