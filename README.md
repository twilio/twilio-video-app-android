# Android Video Ahoy App

This application demonstrates multi-party voice and video built with [Twilio’s Programmable Video Android SDK](https://www.twilio.com/docs/video).

- [Languages](#languages)
- [Getting Started](#getting-started)
    - [Build](#build)
    - [Unit Tests](#unit-tests)
- [Contributing](#contributing)
- [Code of Conduct](#code-of-conduct)
- [License](#license)

# Languages
This application was originally written in Java. However, all new code will be written in Kotlin and we are gradually converting existing Java code to Kotlin.

# Getting Started

In order to run this application on an Android device or emulator, complete the following steps.

## Build
Currently there are three product flavors for the application.

1. Internal - The application intended for internal testing and QA at Twilio. _This variant can only be built by Twilions._
1. Twilio - The application intended for every day use at Twilio. _This variant can only be built by Twilions._
1. Community - The application intended for developers interested in using Programmable Video. _This variant can be built by all developers._

### For Twilions

Twilio employees should follow [these instructions](ForTwilions.md) for building the Internal and Twilio product flavors.

### Building the Community Flavor

The community flavor of the application is meant for developers who would like to work with the Video Android SDK in the context of a full-fledged application.
**This variant uses a hard coded access tokens locally within
the application. This practice is intended for local development and is not encouraged for your
applications. Please follow the
[User Identity and Access Tokens guide](https://www.twilio.com/docs/api/video/identity) for proper
token generation instructions in your application. Putting your Account SID,
API Key, and API Key Secret inside of an Android application will compromise your Twilio API
credentials associated with your Twilio account.**

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

# Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for information on how to help contribute to this app.

## Code of Conduct

This project adheres to the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to the project administrators.

# License

Apache 2.0 license. See [LICENSE.txt](LICENSE.txt) for details.
