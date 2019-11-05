# twilio-video-app-android
This project demonstrates a multi-party voice and video application built with the Twilio Android Video SDK.

# Setup
Before running the app, follow the steps below to provide an access token required to connect to a Twilio room.

1. Ensure you are using the community build variant.
<img width="700px" src="images/community-variant/community-variant.png"/>

2. Login to the [Twilio Console](https://www.twilio.com/login), or create a free account [here](https://twilio.com/try-twilio).

3. Type in an identity and click on "Generate Access Token" from the [Testing Tools Page](https://www.twilio.com/console/video/runtime/testing-tools).
<img width="700px" src="images/community-variant/generate_access_token.png"/>

4. Add the access token string copied from the console to a variable named `TWILIO_ACCESS_TOKEN`
in your **local.properties** file.

```
TWILIO_ACCESS_TOKEN=abcdef0123456789
```
