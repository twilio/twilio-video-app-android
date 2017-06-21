#!/bin/bash

export LANG=en_US.UTF-8;

PRODUCT_NAME="video"

CDN_JSON_REALM="stage"
SDK_VERSION=`head -3 gradle.properties | sed 's/[^0-9]*//g' | tr '\n' '.' | sed s'/.$//'`
SDK_RC=`sed -n '4p' gradle.properties | sed 's/[^0-9]*//g'`

echo "Prepping for sdk-release-tool..."
if [ ! -d $SDK_RELEASE_TOOL_HOME ]; then
    echo "Error: SDK_RELEASE_TOOL_HOME does not exist"
    exit 1
fi

echo $PATH
if [ ! -d "$SDK_RELEASE_TOOL_HOME/venv" ]; then
    echo "Error: venv not found. Please run \"make\""
    exit 1
fi


echo "Prepping directory structure for release schema..."
./scripts/prepare-for-sdk-release-tools.sh ${SDK_VERSION}-rc${SDK_RC}
if [ "$?" -ne "0" ]; then
    exit 1
fi

echo "sdk-release-tool: uploading..."
PWD=`pwd`
export SDK_PACKAGE_PATH="${PWD}"
pushd "$SDK_RELEASE_TOOL_HOME"
./sdk-release-tool upload --${CDN_JSON_REALM} twilio-${PRODUCT_NAME}-android ${SDK_VERSION}-rc${SDK_RC} ${SDK_PACKAGE_PATH}
popd
if [ "$?" -ne "0" ]; then
    echo "Error: failed to execute sdk-release-tool upload"
    exit 1
fi
