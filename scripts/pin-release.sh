#!/bin/bash

export LANG=en_US.UTF-8;
export PATH=/usr/local/bin:$PATH
export WORKSPACE_ROOT_DIR=`pwd`
export SCRIPTS_DIR="$WORKSPACE_ROOT_DIR/scripts"
export SDK_PACKAGE_PATH="$WORKSPACE_ROOT_DIR/package"

if [ ! -z "$RELEASE_VERSION" ]; then
    export SDK_RELEASE_VERSION=${RELEASE_VERSION}
else
    exit 0;
fi

echo "Prepping sdk-release-tool..."
if [ ! -d $SDK_RELEASE_TOOL_HOME ]; then
    echo "Error: SDK_RELEASE_TOOL_HOME does not exist"
    exit 1
fi

if [ ! -d "$SDK_RELEASE_TOOL_HOME/venv" ]; then
    echo "Error: venv not found. Please run \"make\""
    exit 1
fi

rm -rf ${SDK_PACKAGE_PATH}
mkdir -p ${SDK_PACKAGE_PATH}

pushd "$SDK_RELEASE_TOOL_HOME"

rm -rf ${SCRIPTS_DIR}/downloads
mkdir ${SCRIPTS_DIR}/downloads

MAJORMINOR=$(echo $SDK_RELEASE_VERSION| cut -d'.' -f-2)
DIRECT_URL="https://media.twiliocdn.com/sdk/android/conversations/releases/${SDK_RELEASE_VERSION}/twilio-conversations-android-${SDK_RELEASE_VERSION}.aar"
MAJORMINOR_URL="https://media.twiliocdn.com/sdk/android/conversations/v${MAJORMINOR}/twilio-conversations-android.aar"
LATEST_URL="https://media.twiliocdn.com/sdk/android/conversations/latest/twilio-conversations-android.aar"

curl $DIRECT_URL --output ${SCRIPTS_DIR}/downloads/twilio-conversations-direct.aar

echo "sdk-release-tool: updating..."
./sdk-release-tool pin --prod twilio-conversations-android ${SDK_RELEASE_VERSION}
if [ "$?" -ne "0" ]; then
    echo "Error: failed to execute sdk-release-tool update"
    exit 1
fi

echo "sdk-release-tool: pin latest..."
./sdk-release-tool pin-latest --prod twilio-conversations-android ${SDK_RELEASE_VERSION}
if [ "$?" -ne "0" ]; then
    echo "Error: failed to execute sdk-release-tool pin latest"
    exit 1
fi

echo "Testing whether versions are same..."
curl -L $MAJORMINOR_URL --output ${SCRIPTS_DIR}/downloads/twilio-conversations-majorminor.aar
curl -L $LATEST_URL --output ${SCRIPTS_DIR}/downloads/twilio-conversations-latest.aar

md5sum ${SCRIPTS_DIR}/downloads/twilio-conversations-direct.aar ${SCRIPTS_DIR}/downloads/twilio-conversations-majorminor.aar ${SCRIPTS_DIR}/downloads/twilio-conversations-latest.aar
popd

echo "Major minor version: $MAJORMINOR"
echo "Direct URL: $DIRECT_URL"
echo "Major Minor URL: $MAJORMINOR_URL"
echo "Latest URL: $LATEST_URL"

