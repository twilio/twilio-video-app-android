# Twilio Video Android App
> To make sure your app is ready for **Android 11** please visit [this page](https://github.com/twilio/video-quickstart-android/issues/543).

[![CircleCI](https://circleci.com/gh/twilio/twilio-video-app-android.svg?style=svg)](https://circleci.com/gh/twilio/twilio-video-app-android)

This application demonstrates multi-party voice and video built with [Twilio’s Programmable Video Android SDK](https://www.twilio.com/docs/video).

![App Preview](https://user-images.githubusercontent.com/12685223/94631109-cfca1c80-0284-11eb-8b72-c97276cf34e4.png)

## Features

- [x] Video conferencing with real-time video and audio
- [x] Enable/disable camera
- [x] Mute/unmute mic
- [x] Switch between front and back camera
- [x] [Dominant speaker](https://www.twilio.com/docs/video/detecting-dominant-speaker) indicator
- [x] [Network quality](https://www.twilio.com/docs/video/using-network-quality-api) indicator
- [x] [Bandwidth Profile API](https://www.twilio.com/docs/video/tutorials/using-bandwidth-profile-api)

## Requirements

Android Studio Version | Android API Version Min
------------ | -------------
3.5+ | 16

## Getting Started

In order to run this application on an Android device or emulator, complete the following steps.

### Deploy Twilio Access Token Server

**NOTE:** The Twilio Function that provides access tokens via a passcode should *NOT* be used in a production environment. This token server supports seamlessly getting started with the collaboration app, and while convenient, the passcode is not secure enough for production environments. You should use an authentication provider to securely provide access tokens to your client applications. You can find more information about Programmable Video access tokens [in this tutorial](https://www.twilio.com/docs/video/tutorials/user-identity-access-tokens).

The app requires a back-end to generate [Twilio access tokens](https://www.twilio.com/docs/video/tutorials/user-identity-access-tokens). Follow the instructions below to deploy a serverless back-end using [Twilio Functions](https://www.twilio.com/docs/runtime/functions).

1. [Install Twilio CLI](https://www.twilio.com/docs/twilio-cli/quickstart).
1. Run `twilio login` and follow prompts to [login to your Twilio account](https://www.twilio.com/docs/twilio-cli/quickstart#login-to-your-twilio-account).
1. Run `twilio plugins:install @twilio-labs/plugin-rtc`.
1. Run `twilio rtc:apps:video:deploy --authentication passcode`.
1. The passcode that is output will be used later to [sign in to the app](#start-video-conference).

The passcode will expire after one week. To generate a new passcode, run `twilio rtc:apps:video:deploy --authentication passcode --override`.

#### Troubleshooting The Twilio CLI

If any errors occur after running a [Twilio CLI RTC Plugin](https://github.com/twilio-labs/plugin-rtc) command, then try the following steps.

1. Run `twilio plugins:update` to update the rtc plugin to the latest version.
1. Run `twilio rtc:apps:video:delete` to delete any existing authentication servers.
1. Run `twilio rtc:apps:video:deploy --authentication passcode` to deploy a new authentication server.

#### App Behavior with Different Room Types

**NOTE:** Usage charges will apply for most room types. See [pricing](https://www.twilio.com/video/pricing) for more information.

After running the command [to deploy a Twilio Access Token Server](https://github.com/twilio/twilio-video-app-android#deploy-twilio-access-token-server), the room type will be returned in the command line output. Each room type provides a different video experience. More details about these room types can be found [here](https://www.twilio.com/docs/video/tutorials/understanding-video-rooms). The rest of this section explains how these room types affect the behavior of the video app.

*Group* - The Group room type allows up to fifty participants to join a video room in the app. The Network Quality Level (NQL) indicators and dominant speaker are demonstrated with this room type. Also, the VP8 video codec with simulcast enabled along with a bandwidth profile are set by default in order to provide an optimal group video app experience.

*Small Group* - The Small Group room type provides an identical group video app experience except for a smaller limit of four participants.

*Peer-to-peer* - Although up to ten participants can join a room using the Peer-to-peer (P2P) room type, it is ideal for a one to one video experience. The NQL indicators, bandwidth profiles, and dominant speaker cannot be used with this room type. Thus, they are not demonstrated in the video app. Also, the VP8 video codec with simulcast disabled and 720p minimum video capturing dimensions are also set by default in order to provide an optimal one to one video app experience. If more than ten participants join a room with this room type, then the video app will present an error.

*Go* - The Go room type provides a similar Peer-to-peer video app experience except for a smaller limit of two participants. If more than two participants join a room with this room type, then the video app will present an error.

If the max number of participants is exceeded, then the video app will present an error for all room types.

### Build

Currently there are three product flavors for the application.

1. Internal - The application intended for internal testing and QA at Twilio. _This variant can only be built by Twilions._
3. Community - The application intended for developers interested in using Programmable Video. _This variant can be built by all developers._
   1. debug and release build types are supported.

#### Building the Community Flavor

The community flavor of the application is meant for developers who would like to work with the Video Android SDK in the context of a full-fledged application.

To get started with the community flavor follow these steps:

1. Select the `communityDebug` Build Variant.
1. Run the application.

### Start Video Conference

For each device:

1. [Run](#building-the-community-flavor) the app.
1. Enter any unique name in the `Your name` field.
1. Enter the passcode from [Deploy Twilio Access Token Server](#deploy-twilio-access-token-server) in the `Passcode` field.
1. Tap `Log in`.
1. Enter a room name.
1. Tap `Join`.

The passcode will expire after one week. Follow the steps below to sign in with a new passcode.

1. [Generate a new passcode](#deploy-twilio-access-token-server).
1. In the app tap `Settings > Sign Out`.
1. Repeat the [steps above](#start-video-conference).

## Tests

### Unit Tests

* Android Studio - Right click and run unit tests on package ```app/src/main/java/com/twilio/video/app```
* Terminal - ```./gradlew app:testInternalDebugUnitTest```

### UI Tests

UI tests require credentials that are only available to Twilio employees.

## Related

- [Twilio Video iOS App](https://github.com/twilio/twilio-video-app-ios)
- [Twilio Video React App](https://github.com/twilio/twilio-video-app-react)
- [Twilio CLI RTC Plugin](https://github.com/twilio-labs/plugin-rtc)

## For Twilions

Twilio employees should follow [these instructions](Twilions.md) for internal testing.

## License

Apache 2.0 license. See [LICENSE.txt](LICENSE.txt) for details.
