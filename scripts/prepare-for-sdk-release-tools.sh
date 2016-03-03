#!/bin/bash

BASE_DIR=`dirname $0`
pushd "$BASE_DIR"/.. >/dev/null

# reading the arguments
if [ "$#" -ne 1 ]; then
	echo "Error: Expecting 1 argument: release-version"
	exit 1
fi

RELEASE_VERSION="$1"

PLATFORM_NAME="android"
PRODUCT_NAME="conversations"

# paths
WORKSPACE_ROOT_DIR=`pwd`
ARTIFACT_DIR="$WORKSPACE_ROOT_DIR/target"
ARTIFACT_NAME=twilio-conversations-android.aar
DOCS_DIR="$WORKSPACE_ROOT_DIR/target/javadoc"
RELEASE_VERSION_PATH="$WORKSPACE_ROOT_DIR/dist"

if [ ! -d "$ARTIFACT_DIR" ]; then
	echo "Error: Couldn't find \"Artifact\" folder"
	exit 1
fi

if [ ! -d "$DOCS_DIR" ]; then
	echo "Error: Couldn't find \"Docs\" folder"
	exit 1
fi

echo "${ARTIFACT_DIR}/${ARTIFACT_NAME}"
if [ ! -f "$ARTIFACT_DIR/$ARTIFACT_NAME" ]; then
	echo "Error: Couldn't find the artifact"
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
cp -r "${DOCS_DIR}/" "${RELEASE_VERSION_PATH}/${RELEASE_VERSION}/docs"
cp "${ARTIFACT_DIR}/${ARTIFACT_NAME}" "${RELEASE_VERSION_PATH}/${RELEASE_VERSION}/$ARTIFACT_NAME"

popd >/dev/null
