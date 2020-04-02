# Twilio Video Android App

[![CircleCI](https://circleci.com/gh/twilio/twilio-video-app-android.svg?style=svg)](https://circleci.com/gh/twilio/twilio-video-app-android)

This application demonstrates multi-party voice and video built with [Twilio’s Programmable Video Android SDK](https://www.twilio.com/docs/video).

![video-app-screenshots](https://user-images.githubusercontent.com/1930363/76543029-867ec080-644b-11ea-8145-d15d3fe9f7ea.png)

## Features

- [x] Video conferencing with real-time video and audio
- [x] Enable/disable camera
- [x] Mute/unmute mic
- [x] Switch between front and back camera
- [x] [Dominant speaker](https://www.twilio.com/docs/video/detecting-dominant-speaker) indicator
- [x] [Network quality](https://www.twilio.com/docs/video/using-network-quality-api) indicator

## Requirements

Android Studio Version | Android API Version Min
------------ | -------------
3.5+ | 16

## Getting Started

In order to run this application on an Android device or emulator, complete the following steps.

### Deploy Twilio Access Token Server

The app requires a back-end to generate [Twilio access tokens](https://www.twilio.com/docs/video/tutorials/user-identity-access-tokens). Follow the instructions below to deploy a serverless back-end using [Twilio Functions](https://www.twilio.com/docs/runtime/functions).

1. [Install Twilio CLI](https://www.twilio.com/docs/twilio-cli/quickstart).
1. Run `twilio login` and follow prompts to [login to your Twilio account](https://www.twilio.com/docs/twilio-cli/quickstart#login-to-your-twilio-account).
1. Run `twilio plugins:install @twilio-labs/plugin-rtc`.
1. Run `twilio rtc:apps:video:deploy --authentication passcode`.
1. The passcode that is output will be used later to [sign in to the app](#start-video-conference).

The passcode will expire after one week. To generate a new passcode:

1. Run `twilio rtc:apps:video:delete`.
2. Run `twilio rtc:apps:video:deploy --authentication passcode`.

#### Troubleshooting The Twilio CLI

If any errors occur after running the [Twilio CLI commands](#deploy-twilio-access-token-server), then run the following commands:

1. Run `twilio plugins:update` to update the rtc plugin to the latest version.
1. Run `twilio rtc:apps:video:delete` to delete any problematic authentication servers.
1. Run `twilio rtc:apps:video:deploy --authentication passcode` to deploy a new authentication server.

### Build

Currently there are three product flavors for the application.

1. Internal - The application intended for internal testing and QA at Twilio. _This variant can only be built by Twilions._
1. Twilio - The application intended for every day use at Twilio. _This variant can only be built by Twilions._
1. Community - The application intended for developers interested in using Programmable Video. _This variant can be built by all developers._

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
