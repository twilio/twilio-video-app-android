# This script is used to promote an RC to Release. It pulls down the RC from stage CDN and uploads it to prod CDN using the sdk release tool.

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

if [ ! -f "$SDK_RELEASE_TOOLS_PATH/sdk-release-tool/cdn-sdki.stage.json" ] || [ ! -f "$SDK_RELEASE_TOOLS_PATH/sdk-release-tool/cdn-sdki.prod.json" ]; then
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

DIRECT_URL="https://media.twiliocdn.com/sdk/android/conversations/releases/${SDK_RELEASE_VERSION}/twilio-conversations-android-${SDK_RELEASE_VERSION}.aar"

curl $DIRECT_URL --output ${SCRIPTS_DIR}/downloads/twilio-conversations-direct.aar
md5 ${SDK_PACKAGE_PATH}/dist/${SDK_RELEASE_VERSION}/twilio-conversations-android.aar ${SCRIPTS_DIR}/downloads/twilio-conversations-direct.aar

popd
