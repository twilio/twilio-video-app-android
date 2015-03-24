#!/bin/bash

set -ex

ANDROID_API=9

# need to specify source files manually to avoid having TwilioClientService in there
#JAVADOC_SOURCE_FILES="
#    Connection.java
#    ConnectionListener.java
#    Device.java
#    DeviceListener.java
#    PresenceEvent.java
#    Twilio.java"
#TWILIO_HOWTOS="
#    BasicPhone"
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
    #copy_resources
    #copy_javadocs
    #copy_docs
    #copy_files
    #setup_examples
    #pull_helper_lib
    #pull_server_code
    #archive

}

function check_tools {

    check_android_tools $ANDROID_API

    #if ! type kramdown &>/dev/null; then
    #    echo "Can't find 'kramdown' in PATH.  Need it for docs generation" >&2
    #    exit 1
    #fi

    #if ! type mustache &>/dev/null; then
    #    echo "Can't find 'mustache' in PATH.  Need it for docs HTML generation." >&2
    #    exit 1
    #fi

    #if ! type lame &>/dev/null; then
    #    echo "Can't find 'lame' in PATH.  Need it for sound asset generation" >&2
    #    exit 1
    #fi

    #if ! type ccache &>/dev/null; then
    #    echo "Can't find ccache in PATH.  Might speed up successive NDK builds..." >&2
    #else
    #    export NDK_CCACHE=ccache
    #fi
}

function build_library {

    twsdkroot=${mydir}/..

    # check our submodules, make sure fetched and up-to-date
    pushd "${twsdkroot}"
    #git submodule init
    #git submodule update
    ./git-update-submodules.sh
    popd

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
    #linkname="${tarname}_INTERMEDIATES"
    #ln -sfn obj "${linkname}"
    #tar --version 2>&1 | grep -q GNU && follow_opt="h" || follow_opt="H"
    #tar c${follow_opt}vjf "${buildroot}/${linkname}.tar.bz2" "${linkname}"
    #rm "${linkname}"

    # move intermediates archive to folder
    #intermediates="${buildroot}/intermediates"
    #mkdir ${intermediates}
    #mv "${buildroot}/${linkname}.tar.bz2" "${intermediates}/${linkname}.tar.bz2"

    #tarroot="${buildroot}/${tarname}"
    #mkdir "${tarroot}"

    # jar and native libs
    #mkdir "${tarroot}/libs"
    #cp "${twsdkroot}/sdk/bin/classes.jar" "${tarroot}/libs/${tarname}.jar"
    #abis=$(sed -ne '/^APP_ABI/s/^APP_ABI :=//p' ${twsdkroot}/sdk/jni/Application.mk)
    #for abi in $abis; do
    #if [ -d "${twsdkroot}/sdk/libs/${abi}" ]; then
    #cp -PR "${twsdkroot}/sdk/libs/${abi}" "${tarroot}/libs"
    #fi
    #done

}

function copy_resources {
    # resources
    mkdir -p "${tarroot}/Resources"
    cp -aH ${twsdkroot}/sdk/res/raw/*.wav "${tarroot}/Resources"
}

function copy_javadocs {
    # javadocs
    docdest="${tarroot}/javadoc"
    mkdir "${docdest}"

    for f in $JAVADOC_SOURCE_FILES; do
        jd_source_paths="${jd_source_paths} ${twsdkroot}/sdk/src/com/twilio/client/$f"
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
    -top '<h1>Twilio Client for Android</h1>' \
    -windowtitle 'Twilio Client for Android' \
    -charset UTF-8 \
    -docencoding UTF-8 \
    -linkoffline http://developer.android.com/reference/  "$mydir" \
    -stylesheetfile "${twsdkroot}/tools/javadoc-style.css" \
    ${DOCLINT_DISABLE} ${jd_source_paths}
}

function copy_files {

    # quick start
    pushd "${twsdkroot}/Quickstart"
    make -j1 clean all
    mkdir -p "${tarroot}/Quickstart"
    cp -a quickstart.html "${tarroot}/"
    # note: we now only ship the bare skeleton of the quickstart source
    # code to avoid developer confusion.
    cp -a HelloMonkey "${tarroot}/Quickstart"
    for i in HelloMonkey; do
    pushd "${tarroot}/Quickstart/${i}"
    rm -rf bin gen libs
    mkdir gen
    ln -sfn ../../libs libs

    # original project depends on "sdk" library project, while copied should depend on "sdk.jar" instead
    sed -i '' -e '/android.library.reference.1=.*/d' ./project.properties

    popd
    done
    popd

    # assets
    pushd "${twsdkroot}/assets"
    mkdir -p "${tarroot}/assets"
    cp -aL * "${tarroot}/assets"
    popd
}

function copy_docs {

    # docs dir
    pushd "${twsdkroot}/docs"
    make -j1 clean all
    cp -a *.html "${tarroot}"
    cp -a acknowledgments.txt "${tarroot}"
    popd

}

function setup_examples {
    pushd "${twsdkroot}/howtos"
    for howto in ${TWILIO_HOWTOS}; do
    cp -a "${howto}" "${tarroot}"
    pushd "${tarroot}/${howto}"
    rm -rf bin gen libs
    mkdir gen
    ln -sfn ../libs libs

    # original project depends on "sdk" library project, while copied should depend on "sdk.jar" instead
    sed -i '' -e '/android.library.reference.1=.*/d' ./project.properties

    popd
    done
    popd
}

function pull_helper_lib {
    # helper libs
    mkdir "${tarroot}/helper-libs"
    pushd "${tarroot}/helper-libs"
    for lib in ${TWILIO_HELPER_LIBS}; do
    name=$(echo "${lib}" | cut -d: -f1)
    dest=$(echo "${lib}" | cut -d: -f2)
    git clone --depth 1 --recursive "git://github.com/twilio/${name}.git" "${dest}"
    rm -rf "${dest}/.git"
    done
    popd
}

function pull_server_code {
    # pull server code
    echo "Pulling down server script"
    mkdir "${tarroot}/Server"
    pushd "${tarroot}"
    git clone --depth 1 https://github.com/twilio/mobile-quickstart.git Server &>/dev/null
    popd

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

    if [ "$target" = "debug" ]; then
        # be very clear to ourselves that we just did a debug build
        tarname="${buildname}_DEBUG"
    else
        tarname="${buildname}"
    fi
    popd

    pushd "${tarroot}/.."
    tar cvjf "${buildroot}/${tarname}.tar.bz2" "$(basename ${tarroot})"
    zip -r9 "${buildroot}/${tarname}.zip" "$(basename ${tarroot})"
    popd

    popd

    echo
    echo "New script SDK built in ${buildroot}"
    echo

}

main "$@"

#copy_docs "@"

#popd >/dev/null
