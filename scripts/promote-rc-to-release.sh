#This script is used to promote an RC to Release. It pulls down the RC from dev CDN and uploads it to Prod CDN using the sdk release tool.

export LANG=en_US.UTF-8;
export PATH=/usr/local/bin:$PATH
export WORKSPACE_ROOT_DIR=`pwd`
export SCRIPTS_DIR="$WORKSPACE_ROOT_DIR/scripts"
export SDK_RELEASE_TOOLS_PATH="/usr/local/sbin/sdk-release-tools-ng";
export SDK_PACKAGE_PATH="$WORKSPACE_ROOT_DIR/package"

if [ ! -z "$RELEASE_VERSION" ]; then
    export SDK_RELEASE_VERSION=${RELEASE_VERSION}
else
    exit 0;
fi

if [ ! -z "$RC_BUILD_NUMBER" ]; then
    export SDK_RC_BUILD_NUMBER=${RC_BUILD_NUMBER}
else
    exit 0;
fi


echo "Prepping sdk-release-tool..."
if [ ! -d $SDK_RELEASE_TOOLS_PATH ]; then
    echo "Error: $SDK_RELEASE_TOOLS_PATH does not exist"
    exit 1
fi

if [ ! -f "$SDK_RELEASE_TOOLS_PATH/sdk-release-tool/cdn-sdki.dev.json" ] || [ ! -f "$SDK_RELEASE_TOOLS_PATH/sdk-release-tool/cdn-sdki.prod.json" ]; then
    echo "Error: creds json not found"
    exit 1
fi

if [ ! -d "$SDK_RELEASE_TOOLS_PATH/sdk-release-tool/venv" ]; then
    echo "Error: venv not found. Please run \"make\""
    exit 1
fi

rm -rf ${SDK_PACKAGE_PATH}
mkdir -p ${SDK_PACKAGE_PATH}

pushd "$SDK_RELEASE_TOOLS_PATH/sdk-release-tool/"

echo "sdk-release-tool: downloading..."
./sdk-release-tool download --stage twilio-conversations-android ${SDK_RELEASE_VERSION}-rc${SDK_RC_BUILD_NUMBER} ${SDK_PACKAGE_PATH}
if [ "$?" -ne "0" ]; then
    echo "Error: failed to execute sdk-release-tool upload"
    exit 1
fi

echo "copy release candidate to release folder"
if [ ! -d "${SDK_PACKAGE_PATH}/dist/${SDK_RELEASE_VERSION}" ]; then
    mkdir "${SDK_PACKAGE_PATH}/dist/${SDK_RELEASE_VERSION}"
fi
rsync -av "${SDK_PACKAGE_PATH}/dist/${SDK_RELEASE_VERSION}-rc${SDK_RC_BUILD_NUMBER}/" "${SDK_PACKAGE_PATH}/dist/${SDK_RELEASE_VERSION}/"

echo "sdk-release-tool: uploading..."
./sdk-release-tool upload --prod twilio-conversations-android ${SDK_RELEASE_VERSION} ${SDK_PACKAGE_PATH}
if [ "$?" -ne "0" ]; then
    echo "Error: failed to execute sdk-release-tool upload"
    exit 1
fi

rm -rf ${SCRIPTS_DIR}/downloads
mkdir ${SCRIPTS_DIR}/downloads

MAJORMINOR=$(echo $SDK_RELEASE_VERSION| cut -d'.' -f-2)
DIRECT_URL="https://media.twiliocdn.com/sdk/android/conversations/releases/${SDK_RELEASE_VERSION}/twilio-conversations-android-${SDK_RELEASE_VERSION}.tar.bz2"
MAJORMINOR_URL="https://media.twiliocdn.com/sdk/android/conversations/v${MAJORMINOR}/twilio-conversations-android.tar.bz2"
LATEST_URL="https://media.twiliocdn.com/sdk/android/conversations/latest/twilio-conversations-android.tar.bz2"

curl $DIRECT_URL --output ${SCRIPTS_DIR}/downloads/twilio-rtc-conversations-direct.tar.bz2
md5 ${SDK_PACKAGE_PATH}/sdk/rtc/android/conversations/releases/${SDK_RELEASE_VERSION}/twilio-rtc-conversations.tar.bz2 ${SCRIPTS_DIR}/downloads/twilio-rtc-conversations-direct.tar.bz2

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
curl -L $MAJORMINOR_URL --output ${SCRIPTS_DIR}/downloads/twilio-rtc-conversations-majorminor.tar.bz2
curl -L $LATEST_URL --output ${SCRIPTS_DIR}/downloads/twilio-rtc-conversations-latest.tar.bz2

md5 ${SDK_PACKAGE_PATH}/sdk/rtc/android/conversations/releases/${SDK_RELEASE_VERSION}/twilio-rtc-conversations.tar.bz2 ${SCRIPTS_DIR}/downloads/twilio-rtc-conversations-direct.tar.bz2 ${SCRIPTS_DIR}/downloads/twilio-rtc-conversations-majorminor.tar.bz2 ${SCRIPTS_DIR}/downloads/twilio-rtc-conversations-latest.tar.bz2
popd

echo "Major minor version: $MAJORMINOR"
echo "Direct URL: $DIRECT_URL"
echo "Major Minor URL: $MAJORMINOR_URL"
echo "Latest URL: $LATEST_URL"
