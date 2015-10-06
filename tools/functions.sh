#!/bin/bash

export SDK_NAME_STEM="signal-sdk-android"

get_sdk_version_prefix() {
    version_prefix=$(sed -n '/android:versionName=/s/.*"\(.*\)"[^"]*/\1/p' AndroidManifest.xml)
    [ -n "$version_prefix" ] || return 1
    echo "${version_prefix}"
    return 0
}

get_version_date() {
    date +%Y%m%d.%H%M%S
}

get_sdk_version() {
    [ "$1" != "debug" ] || date_tag=".$(get_version_date)"

    version_prefix=$(get_sdk_version_prefix)
    [ $? -eq 0 ] || return 1

    git_rev=$(git rev-parse --short HEAD)
    [ -n "$git_rev" ] || return 1

    echo "${version_prefix}${date_tag}-${git_rev}"
    return 0
}

function check_android_tools {
    ANDROID_API=$1
    
    if [ -z "${ANDROID_SDK_HOME}" ]; then
        echo "Please set the ANDROID_SDK_HOME env var to the root of the Android SDK." >&2
        exit 1
    fi

    if [ ! -d "${ANDROID_SDK_HOME}" ]; then
        echo "Please ensure that the Android SDK platform tools are installed." >&2
        exit 1
    fi

    if [ ! -d "${ANDROID_SDK_HOME}/platforms/android-${ANDROID_API}" ]; then
        echo "Please install the SDK platform files for Android API level ${ANDROID_API}." >&2
        exit 1
    fi

    if ! type android &>/dev/null; then
        export PATH="${ANDROID_SDK_HOME}/tools:${PATH}"
        if ! type android &>/dev/null; then
            echo "Can't find 'android' in ${ANDROID_SDK_HOME}/platform-tools/." >&2
            exit 1
        fi
    fi

    if [ -z "${ANDROID_NDK_HOME}" ]; then
        echo "Please set the ANDROID_NDK_HOME env var to the root of the Android NDK." >&2
        exit 1
    fi

    if ! type ndk-build &>/dev/null; then
        export PATH="${ANDROID_NDK_HOME}:${PATH}"
        if ! type ndk-build &>/dev/null; then
            echo "Can't find 'ndk-build' in ${ANDROID_NDK_HOME}." >&2
            exit 1
        fi
    fi

    if [ ! -d "${ANDROID_NDK_HOME}/platforms/android-${ANDROID_API}" ]; then
        echo "Your NDK version appears to be too old (missing files for Android API level ${ANDROID_API})." >&2
        exit 1
    fi
}

function check_java_8 {
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [[ "$JAVA_VERSION" < "1.8" ]]; then
        return 1
    else         
        return 0
    fi
}
