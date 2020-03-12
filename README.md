# Twilio Video Android App

[![CircleCI](https://circleci.com/gh/twilio/twilio-video-app-android.svg?style=shield&circle-token=40105862db5fbadac9bdcff973375d5114417b66)](https://circleci.com/gh/twilio/twilio-video-app-android)

This application demonstrates multi-party voice and video built with [Twilio’s Programmable Video Android SDK](https://www.twilio.com/docs/video).

![video-app-screenshots](https://user-images.githubusercontent.com/1930363/76543029-867ec080-644b-11ea-8145-d15d3fe9f7ea.png)

## Features

- [x] Video conferencing with real-time video and audio
- [x] Enable/disable camera
- [x] Mute/unmute mic
- [x] Switch between front and back camera
- [x] [Dominant speaker](https://www.twilio.com/docs/video/detecting-dominant-speaker) indicator
- [x] [Network quality](https://www.twilio.com/docs/video/using-network-quality-api) indicator

## Languages

This application was originally written in Java. However, all new code will be written in Kotlin and we are gradually converting existing Java code to Kotlin.

## Getting Started

In order to run this application on an Android device or emulator, complete the following steps.

### Build

Currently there are three product flavors for the application.

1. Internal - The application intended for internal testing and QA at Twilio. _This variant can only be built by Twilions._
1. Twilio - The application intended for every day use at Twilio. _This variant can only be built by Twilions._
1. Community - The application intended for developers interested in using Programmable Video. _This variant can be built by all developers._

#### Building the Community Flavor

The community flavor of the application is meant for developers who would like to work with the Video Android SDK in the context of a full-fledged application.
**This variant uses a hard coded access tokens locally within
the application. This practice is intended for local development and is not encouraged for your
applications. Please follow the
[User Identity and Access Tokens guide](https://www.twilio.com/docs/api/video/identity) for proper
token generation instructions in your application. Putting your Account SID,
API Key, and API Key Secret inside of an Android application will compromise your Twilio API
credentials associated with your Twilio account.**

***NOTE:*** Twilio access tokens generated with Twilio Console are valid for one hour. Repeat the steps above to refresh an expired Twilio access token.

To get started with the community flavor follow these steps:

1. Follow [these instructions](https://www.twilio.com/docs/video/tutorials/user-identity-access-tokens#generate-in-console) to generate an access token using your Twilio account.

2. Add the access token string copied from the console to a variable named `TWILIO_ACCESS_TOKEN`
in your **local.properties** file.

```
TWILIO_ACCESS_TOKEN=abcdef0123456789
```

3. Select the `communityDebug` Build Variant.

4. Run the application.

### Start video conference

For each device:

1. Repeat steps to generate a Twilio access token and run.
1. Enter a room name.
1. Tap `Join`.

## Unit Tests

* Android Studio - Right click and run unit tests on package ```app/src/main/java/com/twilio/video/app```
* Terminal - ```./gradlew app:testInternalDebugUnitTest```

## Other Platforms

- [Twilio Video iOS App](https://github.com/twilio/twilio-video-app-ios)
- [Twilio Video React App](https://github.com/twilio/twilio-video-app-react)

## For Twilions

Twilio employees should follow [these instructions](Twilions.md) for internal testing.

## License

Apache 2.0 license. See [LICENSE.txt](LICENSE.txt) for details.
