Twilio Signal SDK for Android
=============================

# Getting Started #
=
**NB:** We will refer $PROJECT_ROOT to project folder where you cloned twiliosignal-android-sdk repo. Whenever you see $PROJECT_ROOT, just replace it with project path. 

1.  Download and install Eclipse, Android SDK and Android NDK.
1.  Clone this repo and `cd $PROJECT_ROOT`
1.  Install the following Ruby gems:
    * 'plist'
    * 'aws-sdk'
1. Install depot tools. Instructions can be found here: https://sites.google.com/a/chromium.org/dev/developers/how-tos/install-depot-tools
   Add environment variable $DEPOTTOOLS which points to depot tools location. Make sure your $PATH variable contains $DEPOTTOOLS.

1.  Pull in dependencies (git submodules) with:
    - `git-update-submodules.sh`
1.  We need to select 
    Select WebRTC build mode:
    * local - follow instructions bellow.
    * remote

    Select WebRTC build type:
    * debug
    * release

    by running script `scripts/select-webrtc-build-mode.sh <mode> <type>`
    
    **remote** mode means pre-build WebRTC libraries will be downloaded instead of building them locally. That is the fastest way to build Twilio Signal SDK in case we don't need to debug WebRTC itself.
    
    **local** mode means source code for WebRTC will be downloaded and compiled locally. It takes more time to setup WebRTC than in 'remote' mode. If you choose this mode, follow instructions bellow.

    **debug** type means that WebRTC libraries will contain debug information.
    
    **release** type means that WebRTC libraries will not contain debug information.
    
1.  import `sdk` project in eclipse
1.  Build `TwilioSDK`.

# Bulding WebRTC locally #
-

**NB:**  WebRTC for Android requires Ubuntu Linux 14.04 in order to build locally. It you are using OSX, you will need to use Vagrant and VirtualBox.

## Mac OSX ##
1. Install Vagrant (you can use brew or mac ports) and VirtualBox. 
1. We need to create case-sensitive volume on Mac. For this task, we can use sparse image.
    * run `$ hdiutil create -type SPARSE -fs 'Case-sensitive Journaled HFS+' -size 40g ~/android.dmg` to create 40g sparse image called android.dmg
    * mount the image with `$ hdiutil attach ~/android.dmg.sparseimage -mountpoint /Volumes/android`
    * Copy all you project files to /Volumes/android (this is your $PROJECT_ROOT from now on).
1. cd into $PROJECT_ROOT
1. run `$ vagrant up` - first time this is run, vagrant will download Ubuntu14.04 x86_64 image (which is ~300Mb). You will also be asked for password, since vagrant needs to setup NFS share with your project folder.
1. Once vagrant is up and running, we need to ssh to it by running `$ vagrant ssh`
1. When you ssh into your linux box, run `$ cd /vagrant`, this is where your project folder resides inside linux box. Any changes done here will reflect your project files in Mac OSX $PROJECT_ROOT.
1. From now on, follow instructions for Linux all the way until the end. Once finished, there are couple of more steps we need to do.
1. Exit your linux box and return to $PROJECT_ROOT (CTRL+D). You can also shutdown vagrant if you want (`$ vagrant halt`), since we don't need it anymore.
1. Once you are back to $PROJECT_ROOT, we need to fix some links, since they are pointing to linux path now. To do this, run `$ ./sdk/thirdparty/webrtc/build-android/scripts/select-webrtc-mode.sh local <type>`, where <type> can be `release` or `debug`.

## Linux (Ubuntu 14.04 x86_64) ##
**NB:** If you are using vagrant, your $PROJECT_ROOT will be /vagrant

1. cd into $PROJECT_ROOT
1. (if you are using vagrant, you can skip this step) run `$ source ./build.sh` and then run `$ install_dependencies`
1. run `$ ./scripts/select-webrtc-build-mode.sh local <type>` where `<type>` can be `release` or `debug`

That's it!

Developing
-

Library Dependencies
-

- [PJSIP](https://code.hq.twilio.com/client/yb-pjproject), which includes the OpenSSL library as git submodule
- [POCO](https://code.hq.twilio.com/client/yb-poco-ios-xcode)
- [OpenSSL](https://code.hq.twilio.com/client/openssl-ios-pod)
- [WebRTC](https://code.hq.twilio.com/client/webrtc-build)

