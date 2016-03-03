export LANG=en_US.UTF-8;

CDN_JSON_REALM="stage"
SDK_VERSION=`head -3 gradle.properties | sed 's/[^0-9]*//g' | tr '\n' '.' | sed s'/.$//'`
SDK_RC=`tail -1 gradle.properties | sed 's/[^0-9]*//g'`

echo "Prepping for sdk-release-tool..."
export SDK_RELEASE_TOOLS_PATH="/usr/local/sbin/sdk-release-tools-ng";
if [ ! -d $SDK_RELEASE_TOOLS_PATH ]; then
    echo "Error: $SDK_RELEASE_TOOLS_PATH does not exist"
    exit 1
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
./scripts/prepare-for-sdk-release-tools.sh ${SDK_VERSION}-rc${SDK_RC}
if [ "$?" -ne "0" ]; then
    exit 1
fi

echo "sdk-release-tool: uploading..."
# check out https://code.hq.twilio.com/client/sdk-release-tool for how to use sdk-release-tool
PWD=`pwd`
export SDK_PACKAGE_PATH="${PWD}"
pushd "$SDK_RELEASE_TOOLS_PATH/sdk-release-tool/"
./sdk-release-tool upload --${CDN_JSON_REALM} twilio-conversations-android ${SDK_VERSION}-rc${SDK_RC} ${SDK_PACKAGE_PATH}
popd
if [ "$?" -ne "0" ]; then
    echo "Error: failed to execute sdk-release-tool upload"
    exit 1
fi


echo "sdk-release-tool: pinning..."
pushd "$SDK_RELEASE_TOOLS_PATH/sdk-release-tool/"
./sdk-release-tool pin -f --${CDN_JSON_REALM} twilio-conversations-android ${SDK_VERSION}
popd
if [ "$?" -ne "0" ]; then
    echo "Error: failed to execute sdk-release-tool update"
    exit 1
fi

