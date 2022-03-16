# Change Log

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
