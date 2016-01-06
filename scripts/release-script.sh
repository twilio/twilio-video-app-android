#!/bin/bash
export LANG=en_US.UTF-8;

echo "Prepping for sdk-release-tool..."
export SDK_RELEASE_TOOLS_PATH="/usr/local/sbin/sdk-release-tools-ng";
if [ ! -d $SDK_RELEASE_TOOLS_PATH ]; then
    echo "Error: $SDK_RELEASE_TOOLS_PATH does not exist"
    exit 1
fi

if [ ! -z "$RELEASE_VERSION" ]; then
    export CDN_JSON_REALM="stage"
else
    export CDN_JSON_REALM="dev"
fi

if [ ! -f "$SDK_RELEASE_TOOLS_PATH/cdn-sdki.$CDN_JSON_REALM.json" ]; then
    echo "Error: $CDN_JSON_REALM creds json not found"
    exit 1
fi

export PATH=$PATH:/usr/local/bin
echo $PATH
if [ ! -d "$SDK_RELEASE_TOOLS_PATH/sdk-release-tool/venv" ]; then
    echo "Error: venv not found. Please run \"make\""
    exit 1
fi

echo "Prepping directory structure for release schema..."
export CI_SOURCE_CODE_REVISION=$(git rev-parse --short HEAD);
export CI_TARBALL_NAME=twilio-conversations-android.tar.bz2
if [ ! -z "$RELEASE_VERSION" ]; then
    if [ -z "$RC_BUILD_NUMBER" ]; then
        echo "Error: missing RC_BUILD_NUMBER"
        exit 1
    fi
    export SDK_RELEASE_VERSION=${RELEASE_VERSION}-rc${RC_BUILD_NUMBER}
else
    export SDK_RELEASE_VERSION=${CI_BUILD_VERSION}-SNAPSHOT+${CI_SOURCE_CODE_REVISION}
fi

./scripts/prepare-for-sdk-release-tools.sh ${SDK_RELEASE_VERSION}

if [ "$?" -ne "0" ]; then
    exit 1
fi

echo "sdk-release-tool: uploading..."
# check out https://code.hq.twilio.com/client/sdk-release-tool for how to use sdk-release-tool
PWD=`pwd`
export SDK_PACKAGE_PATH="${PWD}/output"
pushd "$SDK_RELEASE_TOOLS_PATH/sdk-release-tool/"
./upload --${CDN_JSON_REALM} twilio-conversations-android ${SDK_RELEASE_VERSION} ${SDK_PACKAGE_PATH}
popd
if [ "$?" -ne "0" ]; then
    echo "Error: failed to execute sdk-release-tool upload"
    exit 1
fi

echo "sdk-release-tool: updating..."
pushd "$SDK_RELEASE_TOOLS_PATH/sdk-release-tool/"
./pin -f --${CDN_JSON_REALM} twilio-conversations-android ${SDK_RELEASE_VERSION}
popd
if [ "$?" -ne "0" ]; then
    echo "Error: failed to execute sdk-release-tool update"
    exit 1
fi
