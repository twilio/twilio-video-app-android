#!/bin/bash

BASE_DIR=`dirname $0`
pushd "$BASE_DIR"/.. >/dev/null

# TODO:
MAIN_PROJECT_DIR=`pwd`
#SCRIPT_DIR="$MAIN_PROJECT_DIR"/sdk/thirdparty/webrtc/build-android/scripts
SCRIPT_DIR="$MAIN_PROJECT_DIR"/sdk/external/signal-sdk-core/SDKs/WebRTC/build-android/scripts

if [ "$1" == "local" ]; then
    "$SCRIPT_DIR"/build-webrtc.sh $2
    if [ "$?" -ne "0" ]; then
        echo "ERROR: Failed to download prebuild package."
        popd >/dev/null
        exit 1
    fi
    "$SCRIPT_DIR"/select-webrtc-mode.sh local $2
elif [ "$1" == "remote" ]; then
    "$SCRIPT_DIR"/download-prebuild-package.sh $2
    if [ "$?" -ne "0" ]; then
        echo "ERROR: Failed to download prebuild package."
        popd >/dev/null
        exit 1
    fi
    "$SCRIPT_DIR"/select-webrtc-mode.sh remote $2
elif [ "$1" == "help" ]; then
    echo "Usage: ./select-webrtc-build-mode.sh local|remote [debug|release]"
    popd >/dev/null
    exit 1
else
    echo "Error: mode is not defined. Availabale modes are [local, remote]."
    popd >/dev/null
    exit 1
fi

popd  >/dev/null
