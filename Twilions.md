# Twilions

Twilio employees should follow these instructions for building and testing the Twilio and Internal build variants.

## Build

Download the google-services.json files here:
* [Internal Debug (default)](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal.debug) - Download to `app/src/internal/debug`
* [Internal Release](https://console.firebase.google.com/project/video-app-79418/settings/general/android:com.twilio.video.app.internal) - Download to `app/src/internal/release`

After copying the above files the internal variant should build with no errors. However, when building the Twilio flavor, the app signing keystore is required. Please reach out to a developer on the team to get access to it.

## UI Tests

### Credentials

1. Make `Credentials` directory within the ```app/src/androidTest/assets``` directory.
1. Copy `TestCredentials.json.example` to `TestCredentials.json` within the ```Credentials``` directory and insert correct values for `email_sign_in_user`.

### Run

* Android Studio - Right click and run unit tests on package ```app/src/androidTest/java/com/twilio/video/app```
* Terminal - ```./gradlew app:connectedInternalDebugAndroidTest```
