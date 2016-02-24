#!/bin/bash

BASE_DIR=`dirname $0`
pushd "$BASE_DIR"/.. >/dev/null

if [ -z "$CI_TARBALL_NAME" ]; then
	echo "CI_TARBALL_NAME not specified. Using \"twilio-conversations-android.tar.bz2\" as artifact filename."
    CI_TARBALL_NAME=twilio-conversations-android.tar.bz2
fi

# reading the arguments
if [ "$#" -ne 1 ]; then
	echo "Error: Expecting 1 argument: release-version"
	exit 1
fi

RELEASE_VERSION="$1"


# paths
WORKSPACE_ROOT_DIR=`pwd`
PACKAGE_DIR="$WORKSPACE_ROOT_DIR/conversations/build/outputs/tar"
DOCS_DIR="$WORKSPACE_ROOT_DIR/conversations/build/docs/javadoc"
PLATFORM_NAME="android"
PRODUCT_NAME="conversations"
RELEASE_VERSION_PATH="$PACKAGE_DIR/dist"
ARTIFACT_NAME="twilio-${PRODUCT_NAME}-${PLATFORM_NAME}.tar.bz2"

if [ ! -d "$PACKAGE_DIR" ]; then
	echo "Error: Couldn't find \"Package\" folder"
	exit 1
fi

# move the tarball and docs folder to the release directory
#if [ ! -d "$DOCS_DIR" ]; then
#	echo "Error: Couldn't find \"docs\" folder"
#	exit 1
#fi
echo "${PACKAGE_DIR}/${CI_TARBALL_NAME}"
if [ ! -f "$PACKAGE_DIR/$CI_TARBALL_NAME" ]; then
	echo "Error: Couldn't find the tarball"
	exit 1
fi
echo $RELEASE_VERSION_PATH
if [ -z "${RELEASE_VERSION_PATH}" ] || [ -z "${RELEASE_VERSION}" ]; then
	echo "Error: incorrect path variables"
	exit 1
fi
if [ ! -d "${RELEASE_VERSION_PATH}/${RELEASE_VERSION}" ]; then
	mkdir -p "${RELEASE_VERSION_PATH}/${RELEASE_VERSION}"
fi
mkdir -p "${RELEASE_VERSION_PATH}/${RELEASE_VERSION}/docs"
#mv "${DOCS_DIR}" "${RELEASE_VERSION_PATH}/${RELEASE_VERSION}"
cp "${PACKAGE_DIR}/${CI_TARBALL_NAME}" "${RELEASE_VERSION_PATH}/${RELEASE_VERSION}/$ARTIFACT_NAME"


popd >/dev/null
