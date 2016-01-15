#This script is used to pin prod release to latest.

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

echo "Prepping sdk-release-tool..."
if [ ! -d $SDK_RELEASE_TOOLS_PATH ]; then
    echo "Error: $SDK_RELEASE_TOOLS_PATH does not exist"
    exit 1
fi

if [ ! -f "$SDK_RELEASE_TOOLS_PATH/sdk-release-tool/cdn-sdki.prod.json" ]; then
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

rm -rf ${SCRIPTS_DIR}/downloads
mkdir ${SCRIPTS_DIR}/downloads

MAJORMINOR=$(echo $SDK_RELEASE_VERSION| cut -d'.' -f-2)
DIRECT_URL="https://media.twiliocdn.com/sdk/android/conversations/releases/${SDK_RELEASE_VERSION}/twilio-conversations-android-${SDK_RELEASE_VERSION}.tar.bz2"
MAJORMINOR_URL="https://media.twiliocdn.com/sdk/android/conversations/v${MAJORMINOR}/twilio-conversations-android.tar.bz2"
LATEST_URL="https://media.twiliocdn.com/sdk/android/conversations/latest/twilio-conversations-android.tar.bz2"

curl $DIRECT_URL --output ${SCRIPTS_DIR}/downloads/twilio-conversations-direct.tar.bz2

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
curl -L $MAJORMINOR_URL --output ${SCRIPTS_DIR}/downloads/twilio-conversations-majorminor.tar.bz2
curl -L $LATEST_URL --output ${SCRIPTS_DIR}/downloads/twilio-conversations-latest.tar.bz2

md5 ${SCRIPTS_DIR}/downloads/twilio-conversations-direct.tar.bz2 ${SCRIPTS_DIR}/downloads/twilio-conversations-majorminor.tar.bz2 ${SCRIPTS_DIR}/downloads/twilio-conversations-latest.tar.bz2
popd

echo "Major minor version: $MAJORMINOR"
echo "Direct URL: $DIRECT_URL"
echo "Major Minor URL: $MAJORMINOR_URL"
echo "Latest URL: $LATEST_URL"

