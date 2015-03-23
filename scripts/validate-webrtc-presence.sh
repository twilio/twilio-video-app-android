#!/bin/bash

BASE_DIR=`dirname $0`
pushd "$BASE_DIR"/.. >/dev/null

MAIN_PROJECT_DIR=`pwd`
WEBRTC_INCLUDE_DIR="$MAIN_PROJECT_DIR"/sdk/external/signal-sdk-core/SDKs/WebRTC/build-android/prebuild/include/webrtc

if [ ! -d "$WEBRTC_INCLUDE_DIR" ]; then
   echo "Error: WebRTC is not setup. Please select mode - local and remote."
   popd  >/dev/null
   exit 1
fi

popd  >/dev/null
