Twilio SDK for Android
=============================

# Getting Started
=
**NB:** We will refer $PROJECT_ROOT to project folder where you cloned twiliosignal-android-sdk repo. Whenever you see $PROJECT_ROOT, just replace it with project path.

1.  Download and install Eclipse, Android SDK and Android NDK.
1.  Install dependency libraries (follow instruction in [Build dependency](##Build-dependency))
	* [twilio-sdk-build-tools](https://code.hq.twilio.com/client/twilio-sdk-build-tools)
1.  Install the following Ruby gems:
    * 'plist'
    * 'aws-sdk'
1.  Clone this repo and `cd $PROJECT_ROOT`

## Build dependency
In order to build native code you will need to build dependency libs first. That includes OpenSSL, POCO, PJSIP, WebRTC and Twilio SDK Core. You can check instructions on building dependency libs in their repo [twilio-sdk-build-tools](https://code.hq.twilio.com/client/twilio-sdk-build-tools). Unfortunatelly, as of this writing dependency libraries for Android can only be built in Linux. They are all installed by default in `/usr/local/twilio-sdk`. If you installed them on another location, you need to set PREFIX variable with that location. For example: `$ export PREFIX=/location/of/buildtools`.

## Build Twilio SDK from eclipse
1.  [Build native code](###Bulid-native-code)
1.  import `sdk` project in eclipse
1.  Build `TwilioSDK`.

### Build native code
If you have all dependency installed, you can proceed on building native sdk code All you need todo is cd into $PROJECT_ROOT/sdk and build native code by executing ndk-build:
	`$ ndk-build`
or in case of debug version
	`$ ndk-build NDK_DEBUG=1`
that's it :-) After this step be sure to refresh sdk project in Eclipse. 


## Build Twilio SDK from command line
If you have all dependency installed, you can proceed on builiding sdk. All you need to do is cd into $PROJECT_ROOT and run:
	`$ ./script/mksdk.sh release` for release version or
    `$ ./script/mksdk.sh debug` for debug version
Once finished you can find library archive in $PROJECT_ROOT/output.


Developing
-


