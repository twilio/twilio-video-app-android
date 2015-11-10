#!/bin/bash

set -ex

ANDROID_API=19

# need to specify source files manually to avoid having TwilioClientService in there
JAVADOC_SOURCE_FILES="
	Conversation.java
    ConversationException.java
    ConversationListener.java
    Endpoint.java
    EndpointListener.java
    I420Frame.java
    Invite.java
    LocalMedia.java
    LocalMediaImpl.java
    Media.java
    Participant.java
    TrackOrigin.java
    TwilioRTC.java
    VideoRenderer.java
    VideoRendererObserver.java
    VideoTrack.java
    VideoViewRenderer.java"
TWILIO_HOWTOS="
    QuickStart"
#TWILIO_HELPER_LIBS="
#    twilio-java:java
#    twilio-php:php
#    twilio-python:python
#    twilio-ruby:ruby"


function main {

    mydir=$(dirname $0)
    if [ "${mydir}" = "." ]; then
        mydir=$(pwd)
    elif [ "${mydir:0:1}" != "/" ]; then
        mydir="$(pwd)/${mydir}"
    fi

    twsdkroot=${mydir}/..

    . ${mydir}/functions.sh

    target=debug
    while [ "$1" ]; do
        case "$1" in
            -h|--help)
                print_usage
                exit 0
                ;;
            release)
                target=release
                ;;
            debug)
                target=debug
                ;;
            *)
                echo "Unrecognized option: $1" >&2
                exit 1
                ;;
        esac
        shift
    done

    check_tools
    build_library
    build_testcore
    copy_javadocs
    archive

}

function check_tools {
    check_android_tools $ANDROID_API
 }

function build_library {

    pushd "${twsdkroot}"/sdk

    #rm -f build.xml
    android update lib-project -p "${twsdkroot}"/sdk
    ant clean
    ndk-build clean

    #./mk-sound-assets.sh

    new_sdk_version="$(get_sdk_version ${target})"
    ./mk-version-class.sh ${new_sdk_version}

    # force the requested build type since ndk-build will otherwise look at AndroidManifest
    [ "$target" = "release" ] && ndk_build_opts="NDK_DEBUG=0" || ndk_build_opts="NDK_DEBUG=1"
    ndk-build -j4 $ndk_build_opts

    ant $target

    buildroot="$twsdkroot/output"
    rm -rf $buildroot
    mkdir $buildroot

    tarname="${SDK_NAME_STEM}"

    # save intermediates for later debugging
    linkname="${tarname}_INTERMEDIATES"
    ln -sfn obj "${linkname}"
    tar --version 2>&1 | grep -q GNU && follow_opt="h" || follow_opt="H"
    tar c${follow_opt}vjf "${buildroot}/${linkname}.tar.bz2" "${linkname}"
    rm "${linkname}"

    # move intermediates archive to folder
    intermediates="${buildroot}/intermediates"
    mkdir ${intermediates}
    mv "${buildroot}/${linkname}.tar.bz2" "${intermediates}/${linkname}.tar.bz2"

    tarroot="${buildroot}/${tarname}"
    mkdir "${tarroot}"

    # jar and native libs
    SDK_VERSION=$(sed -n '/android:versionName=/s/.*"\(.*\)"[^"]*/\1/p' AndroidManifest.xml)
    mkdir "${tarroot}/libs"
    cp "${twsdkroot}/sdk/bin/${tarname}.jar" "${tarroot}/libs/${tarname}-${SDK_VERSION}.jar"
    abis=$(sed -ne '/^APP_ABI/s/^APP_ABI :=//p' ${twsdkroot}/sdk/jni/Application.mk)
    for abi in $abis; do
    if [ -d "${twsdkroot}/sdk/libs/${abi}" ]; then
    cp -PR "${twsdkroot}/sdk/libs/${abi}" "${tarroot}/libs"
    fi
    done

}

function build_testcore {
    PROJECT_DIR="${twsdkroot}"/TestCore

    pushd $PROJECT_DIR
    echo "Building project at ${PROJECT_DIR}..."

    rm -f build.xml
    android update project -p ${PROJECT_DIR}

    ant clean
    ant debug

    echo "Project built succesffully."
    popd
}

function copy_javadocs {
    # javadocs
    docdest="${tarroot}/javadoc"
    mkdir "${docdest}"

    for f in $JAVADOC_SOURCE_FILES; do
        jd_source_paths="${jd_source_paths} ${twsdkroot}/sdk/src/com/twilio/signal/$f"
    done


    # # doclint was introduced in Java 8 and fails to build javadoc on any error. Switching it off.
    if check_java_8; then
        DOCLINT_DISABLE="-Xdoclint:none "
    fi

    javadoc \
    -public \
    -sourcepath "${twsdkroot}/sdk/src" \
    -classpath "${ANDROID_SDK_HOME}/platforms/android-${ANDROID_API}/android.jar" \
    -d "${docdest}" \
    -version \
    -top '<h1>Twilio Conversations SDK for Android</h1>' \
    -windowtitle 'Twilio Conversations SDK for Android' \
    -charset UTF-8 \
    -docencoding UTF-8 \
    -linkoffline http://developer.android.com/reference/  "$mydir" \
    ${DOCLINT_DISABLE} ${jd_source_paths}
}


function archive {

    pushd "${twsdkroot}"

    SDK_VERSION=$(sed -n '/android:versionName=/s/.*"\(.*\)"[^"]*/\1/p' sdk/AndroidManifest.xml)
    GIT_COMMIT=$(git rev-parse --short=7 HEAD)

    # CI_BUILD_NUMBER is Jenkins build number
    if [ -z ${CI_BUILD_NUMBER} ]; then
        buildname="${SDK_NAME_STEM}-${SDK_VERSION}-${GIT_COMMIT}"
    else
        buildname="${SDK_NAME_STEM}-${SDK_VERSION}-b${CI_BUILD_NUMBER}-${GIT_COMMIT}"
        # if build is done from Jenkins, write build version string to file, later used when uploading to S3
        echo "${SDK_VERSION}-b${CI_BUILD_NUMBER}-${GIT_COMMIT}" > ci_sdk_version.txt
    fi

    tarname="${SDK_NAME_STEM}"
    #if [ "$target" = "debug" ]; then
    #    # be very clear to ourselves that we just did a debug build
    #    tarname="${buildname}_DEBUG"
    #else
    #    tarname="${buildname}"
    #fi
    popd

    pushd "${tarroot}/.."
    tar cvjf "${buildroot}/${tarname}.tar.bz2" "$(basename ${tarroot})"
    #zip -r9 "${buildroot}/${tarname}.zip" "$(basename ${tarroot})"
    popd

    popd

    echo
    echo "New script SDK built in ${buildroot}"
    echo

}

main "$@"

#copy_docs "@"

#popd >/dev/null
